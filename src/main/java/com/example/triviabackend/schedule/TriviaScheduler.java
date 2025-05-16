package com.example.triviabackend.schedule;


import com.example.triviabackend.service.GptService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class TriviaScheduler {

	private final GptService gptService;

	private void refreshQuestions(){

	}
}
