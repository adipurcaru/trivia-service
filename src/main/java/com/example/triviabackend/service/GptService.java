package com.example.triviabackend.service;

import com.example.triviabackend.dto.ChatRequestDto;
import com.example.triviabackend.dto.QuestionDto;
import com.example.triviabackend.model.Answer;
import com.example.triviabackend.model.Question;
import com.example.triviabackend.repository.AnswerRepository;
import com.example.triviabackend.repository.QuestionRepository;
import com.example.triviabackend.utils.ApplicationConstants;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Log4j2
@Service
public class GptService {


	private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";
	private final AnswerRepository answerRepository;
	private final QuestionRepository questionRepository;
	@Value("${openai.api.key}")
	private String apiKey;


	public GptService(AnswerRepository answerRepository, QuestionRepository questionRepository) {
		this.answerRepository = answerRepository;
		this.questionRepository = questionRepository;
	}

	public List<Question> generateQuestions(String type, Integer count) throws IOException {
		OkHttpClient client = new OkHttpClient.Builder()
				.connectTimeout(20, TimeUnit.SECONDS)
				.readTimeout(30, TimeUnit.SECONDS)
				.writeTimeout(20, TimeUnit.SECONDS)
				.build();

		int maxPerRequest = 20;
		int numCalls = (int) Math.ceil(count / (double) maxPerRequest);

		List<Question> allQuestions = new ArrayList<>();
		ObjectMapper mapper = new ObjectMapper();

		log.info("Generating {} questions", count);
		for (int i = 0; i < numCalls; i++) {
			int currentBatch = Math.min(maxPerRequest, count - allQuestions.size());
			log.info("Processing batch number {}.", currentBatch);

			String prompt;
			if (ApplicationConstants.QUIZ_CULTURA_GEN.equalsIgnoreCase(type)) {
				prompt = """
                    Genereaza %d intrebari de cultura generala in limba romana de dificultate medie, dar unele sa fie chiar putin mai grele.
                    Este foarte important sa nu repeti intrebarile deloc.
                    Este foarte important sa variaza domeniile si stilul intrebarii.
                    Fiecare intrebare trebuie sa contina 4 variante de raspuns si un singur raspuns corect.
                    Returneaza rezultatul EXCLUSIV ca JSON pur, fara explicatii, fara backticks, fara format Markdown.
                    [
                      {
                        "question": "...",
                        "options": ["A", "B", "C", "D"],
                        "correctAnswer": "...",
                        "type": "cultura_generala"
                      }
                    ]
                    """.formatted(currentBatch);
			} else {
				prompt = """
                    Genereaza %d intrebari despre capitalele lumii in limba romana.
                    Amesteca intrebarile intre doua formate:
                    1. Care este capitala X?
                    2. Orasul Y apartine carei tari?
                    Returneaza rezultatul EXCLUSIV ca JSON pur, fara explicatii, fara backticks, fara format Markdown.
                    [
                      {
                        "question": "...",
                        "options": ["A", "B", "C", "D"],
                        "correctAnswer": "...",
                        "type": "capitale"
                      }
                    ]
                    """.formatted(currentBatch);
			}

			List<ChatRequestDto.Message> messages = List.of(
					new ChatRequestDto.Message("system", "Esti un generator de quiz-uri de cultura generala. Intrebarile si raspunsurile trebuie sa fie in limba romana dar fara diacritice."),
					new ChatRequestDto.Message("user", prompt)
			);

			ChatRequestDto body = new ChatRequestDto("gpt-3.5-turbo", messages, 0.8, 3000);
			String jsonBody = mapper.writeValueAsString(body);

			Request request = new Request.Builder()
					.url(OPENAI_URL)
					.addHeader("Authorization", "Bearer " + apiKey)
					.addHeader("Content-Type", "application/json")
					.post(RequestBody.create(jsonBody, MediaType.get("application/json")))
					.build();

			try (Response response = client.newCall(request).execute()) {
				if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

				String responseBody = response.body().string();
				String content = mapper.readTree(responseBody)
						.get("choices").get(0).get("message").get("content").asText();

				List<QuestionDto> dtoList = mapper.readValue(content, new TypeReference<>() {});

				for (QuestionDto dto : dtoList) {
					Question q = new Question();
					q.setDescription(dto.getQuestion());
					q.setType(dto.getType());
					q.setCorrectAnswer(dto.getCorrectAnswer());

					q = questionRepository.save(q); // Save question first

					List<Answer> answerList = new ArrayList<>();
					for (String option : dto.getOptions()) {
						Answer a = new Answer();
						a.setDescription(option);
						a.setQuestion(q);
						answerList.add(answerRepository.save(a));
					}

					q.setAnswers(answerList);
					allQuestions.add(q);
				}
			}
		}

		return allQuestions;
	}


	public List<Question> generateQuestionsWithContext(String type, int count) throws IOException {
		OkHttpClient client = new OkHttpClient.Builder()
				.connectTimeout(30, TimeUnit.SECONDS)
				.readTimeout(60, TimeUnit.SECONDS)
				.writeTimeout(30, TimeUnit.SECONDS)
				.build();


		ObjectMapper mapper = new ObjectMapper();
		List<Question> allQuestions = new ArrayList<>();
		List<ChatRequestDto.Message> messages = new ArrayList<>();

		// Mesajul de sistem
		messages.add(new ChatRequestDto.Message("system", """
        Esti un generator de quiz-uri de cultura generala. Raspunsurile trebuie sa fie unice si variate.
        Formatul este JSON, fara explicatii, fara backticks, fara markdown.
        Nu repeta nicio intrebare sau raspuns. Fara diacritice.
    """));

		int maxPerRequest = 20;
		int calls = (int) Math.ceil(count / (double) maxPerRequest);

		log.info("Executing {} requests..", calls );
		for (int i = 0; i < calls; i++) {
			int batchCount = Math.min(maxPerRequest, count - allQuestions.size());

			log.info("Batch {}", i);
			String prompt;

			if(ApplicationConstants.QUIZ_CULTURA_GEN.equalsIgnoreCase(type)) {
				// Promptul pentru batch-ul curent
				prompt = String.format("""
						              Genereaza %d intrebari de cultura generala in limba romana de dificultate medie, dar unele sa fie chiar putin mai grele.
						              Este foarte important sa nu repeti intrebarile deloc.
						              Este foarte important sa variaza domeniile si stilul intrebarii.
						              Intrbarile trebuie sa fie din domeniile urmatoare:
						Istorie: evenimente istorice, personalități marcante, date importante,
						Geografie: țări, capitale, râuri, munți, locații geografice,
						Știință: fizică, chimie, biologie, invenții, descoperiri,
						Cultură generală: informații diverse din multiple domenii,
						Literatură și artă: autori, opere literare, curente artistice, pictori celebri,
						Muzică: genuri muzicale, artiști, albume, instrumente,
						Sport: competiții, sportivi renumiți, recorduri,
						Divertisment: filme, seriale, actori, regizori,
						Tehnologie: inovații, gadgeturi, internet, informatică,
						Curiozități: fapte inedite, recorduri mondiale, informații surprinzătoare.
						              Fiecare intrebare trebuie sa contina 4 variante de raspuns si un singur raspuns corect.
						              Returneaza rezultatul EXCLUSIV ca JSON pur, fara explicatii, fara backticks, fara format Markdown.
						              [
						                {
						                  "question": "...",
						                  "options": ["A", "B", "C", "D"],
						                  "correctAnswer": "...",
						                  "type": "cultura_generala" //asta este o valoarea hardcodata care ramane asa
						                }
						              ]
						              """, batchCount);
			} else {
				prompt = """
                    Genereaza %d intrebari despre capitalele lumii in limba romana.
                    Amesteca intrebarile intre doua formate:
                    1. Care este capitala X?
                    2. Orasul Y apartine carei tari?
                    Returneaza rezultatul EXCLUSIV ca JSON pur, fara explicatii, fara backticks, fara format Markdown.
                    [
                      {
                        "question": "...",
                        "options": ["A", "B", "C", "D"],
                        "correctAnswer": "...",
                        "type": "capitale"
                      }
                    ]
                    """.formatted(batchCount);
			}
			messages.add(new ChatRequestDto.Message("user", prompt));

			// Creezi payload-ul
			ChatRequestDto body = new ChatRequestDto("gpt-3.5-turbo", messages, 0.5, 2000);
			String jsonBody = mapper.writeValueAsString(body);

			Request request = new Request.Builder()
					.url(OPENAI_URL)
					.addHeader("Authorization", "Bearer " + apiKey)
					.addHeader("Content-Type", "application/json")
					.post(RequestBody.create(jsonBody, MediaType.get("application/json")))
					.build();

			try (Response response = client.newCall(request).execute()) {
				if (!response.isSuccessful()) {
					throw new IOException("Unexpected code " + response);
				}

				String responseBody = response.body().string();
				String content = mapper.readTree(responseBody)
						.get("choices").get(0).get("message").get("content").asText();

				// GPT ti-a raspuns → adaugam ca assistant message
				messages.add(new ChatRequestDto.Message("assistant", content));

				// Deserializare
				List<QuestionDto> dtoList = mapper.readValue(content, new TypeReference<>() {});

				for (QuestionDto dto : dtoList) {
					Question q = new Question();
					q.setDescription(dto.getQuestion());
					q.setType(dto.getType());
					q.setCorrectAnswer(dto.getCorrectAnswer());

					q = questionRepository.save(q);

					List<Answer> answerList = new ArrayList<>();
					for (String option : dto.getOptions()) {
						Answer a = new Answer();
						a.setDescription(option);
						a.setQuestion(q);
						answerList.add(answerRepository.save(a));
					}

					q.setAnswers(answerList);
					allQuestions.add(q);
				}
			}
		}

		return allQuestions;
	}


	public List<QuestionDto> getRandomQuestions(String type, Integer count) {
		return questionRepository.findRandomQuestionsByType(type, PageRequest.of(0, count)).stream().map(Question::toDto).toList();
	}
}


