package com.example.edukg_backend.Models;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

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
    private String questionBody;
    private String answer;
    private String instanceName;
    private String course;
    private boolean isDefault;
}
