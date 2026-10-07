package com.smartexam.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity @Table(name = "exams")
@Getter @Setter @NoArgsConstructor
public class Exam {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private int durationMinutes;
    @Column(name = "is_practice")
    private boolean practice;
    private Long ownerId; // set only for auto-generated practice tests

    @ManyToMany
    @JoinTable(name = "exam_questions",
            joinColumns = @JoinColumn(name = "exam_id"),
            inverseJoinColumns = @JoinColumn(name = "question_id"))
    private List<Question> questions = new ArrayList<>();
}
