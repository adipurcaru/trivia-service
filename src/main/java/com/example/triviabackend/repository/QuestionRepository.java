package com.example.triviabackend.repository;

import com.example.triviabackend.model.Question;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

	@Query(value = "SELECT * FROM question WHERE type = :type ORDER BY RANDOM() LIMIT :count", nativeQuery = true)
	List<Question> findRandomByType(@Param("type") String type, @Param("count") int count);

	// dacă vrei și pe categorie:
	@Query(value = "SELECT * FROM question WHERE type = :type ORDER BY RANDOM()", nativeQuery = true)
	List<Question> findRandomQuestionsByType(@Param("type") String type, Pageable pageable);

}
