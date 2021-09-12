package com.example.edukg_backend.Models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="tab_question")
@Getter
@Setter
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    // private User user;
    @Column(columnDefinition = "varchar(1000)")
    private String questionBody;
    private String answer;
    private String instanceName;
    private String course;
    private boolean isDefault;
}
