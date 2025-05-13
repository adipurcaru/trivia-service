package com.example.triviabackend.service;

import com.example.triviabackend.dto.ChatRequestDto;
import com.example.triviabackend.model.Question;
import com.example.triviabackend.utils.ApplicationConstants;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Service
public class GptService {

    @Value("${openai.api.key}")
    private String apiKey;

    private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";

    public List<Question> generateQuestions(String type) throws JsonProcessingException {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(20, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(20, java.util.concurrent.TimeUnit.SECONDS)
                .build();

        String prompt;

        if(ApplicationConstants.QUIZ_CULTURA_GEN.equalsIgnoreCase(type)) {
            prompt = """
                Genereaza 10 intrebari de cultura generala in limba romana de dificultate medie.
                Nu repeta structura sau tema intrebarilor. Variaza domeniile si stilul.
                Nu include intrebari deja folosite in acest quiz sau anterior.
                Fiecare intrebare trebuie sa contina 4 variante de raspuns si un singur raspuns corect.
                Returneaza rezultatul EXCLUSIV ca JSON pur, fara explicatii, fara backticks, fara format Markdown.
                [
                  {
                    "question": "...",
                    "options": ["A", "B", "C", "D"],
                    "correctAnswer": "..."
                  },
                  ...
                ]
                """;
        } else {
            prompt = """
                Genereaza 10 intrebari despre capitalele lumii in limba romana.
                Amesteca intrebarile intre doua formate:
                1. Care este capitala tarii X?
                2. Capitala Y apartine carei tari?
                Returneaza rezultatul EXCLUSIV ca JSON pur, fara explicatii, fara backticks, fara format Markdown.
                [
                  {
                    "question": "...",
                    "options": ["A", "B", "C", "D"],
                    "correctAnswer": "..."
                  }
                ]
                """;
        }



        List<ChatRequestDto.Message> messages = List.of(
                new ChatRequestDto.Message("system", "Ești un generator de quiz-uri de cultură generală. " +
                        "Trebuie sa generezi intrebari variate din toate domeniile pentru a putea pune la incercare cultura generala a utilizatorului. " +
                        "Intrebarile trebuie sa fie variate, nu foarte grele si sa nu se repete. " +
                        "Intrebarile si raspunsurile trebuie sa fie in limba romana dar fara diacritice."),
                new ChatRequestDto.Message("user", prompt)
        );

        ChatRequestDto body = new ChatRequestDto("gpt-3.5-turbo", messages, 0.5, 800);

        String jsonBody = new ObjectMapper().writeValueAsString(body);


        Request request = new Request.Builder()
                .url(OPENAI_URL)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(jsonBody, MediaType.get("application/json")))
                .build();


        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            String responseBody = response.body().string();
            String content = new ObjectMapper().readTree(responseBody)
                    .get("choices").get(0).get("message").get("content").asText();

            return new ObjectMapper().readValue(content, new TypeReference<List<Question>>() {});
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}
