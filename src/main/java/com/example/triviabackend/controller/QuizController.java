package com.example.triviabackend.controller;

import com.example.triviabackend.dto.QuestionDto;
import com.example.triviabackend.model.Question;
import com.example.triviabackend.service.GptService;
import com.example.triviabackend.utils.ApplicationConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/quiz")
@CrossOrigin
public class QuizController {

	@Autowired
	private GptService gptService;

	@PostMapping("/questions/generate")
	public List<Question> generateQuestions(@RequestParam(defaultValue = "cultura", name = "type") String type,
									   @RequestParam(name = "count") Integer count) throws IOException {
		return gptService.generateQuestionsWithContext(type, count);
	}

	@GetMapping("/questions")
	public List<QuestionDto> getQuestions(@RequestParam(defaultValue = "cultura_generala", name = "type") String type,
										  @RequestParam(name = "count") Integer count) throws IOException {
		return gptService.getRandomQuestions(type, count);
	}

	@GetMapping("/test")
	public ResponseEntity<String> test() {
		return ResponseEntity.ok("Hello World");
	}

}
