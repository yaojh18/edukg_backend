package com.example.edukg_backend.Repositories;

import com.example.edukg_backend.Models.Question;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface QuestionRepository extends CrudRepository<Question, Long> {
    @Query("SELECT c FROM Question c WHERE c.isDefault = true")
    List<Question> findAllDefault();
}
