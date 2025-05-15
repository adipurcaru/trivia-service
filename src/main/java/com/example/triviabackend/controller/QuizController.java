package com.example.triviabackend.controller;

import com.example.triviabackend.model.Question;
import com.example.triviabackend.service.GptService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/quiz")
@CrossOrigin
public class QuizController {

    @Autowired
    private GptService gptService;

    @GetMapping("/questions")
    public List<Question> getQuestions(@RequestParam(defaultValue = "cultura", name = "type") String type) throws JsonProcessingException {
        return gptService.generateQuestions(type);
    }
    @GetMapping("/test")
    public ResponseEntity<String> test(){
        return ResponseEntity.ok("Hello World");
    }

}
