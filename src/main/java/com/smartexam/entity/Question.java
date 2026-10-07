package com.smartexam.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "questions")
@Getter @Setter @NoArgsConstructor
public class Question {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    @JoinColumn(name = "topic_id")
    private Topic topic;
    @Column(name = "question_text", length = 1000, nullable = false)
    private String text;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    @Column(length = 1)
    private String correctOption;   // "A".."D"
    private String difficulty;      // EASY / MEDIUM / HARD
}
