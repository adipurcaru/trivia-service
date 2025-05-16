package com.example.triviabackend.model;

import com.example.triviabackend.dto.QuestionDto;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;


@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@Builder
@Entity
@Table(name = "QUESTION")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;

    private String type;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Answer> answers;

    private String correctAnswer;

    public static QuestionDto toDto(Question question){
        List<String> options = new ArrayList<>();
        question.getAnswers().forEach(answer -> {
            options.add(answer.getDescription());
        });

        return QuestionDto.builder()
                .type(question.getType())
                .question(question.getDescription())
                .correctAnswer(question.getCorrectAnswer())
                .options(options)
                .build();
    }
}
