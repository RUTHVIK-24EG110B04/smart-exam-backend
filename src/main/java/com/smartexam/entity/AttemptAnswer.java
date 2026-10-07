package com.smartexam.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "attempt_answers")
@Getter @Setter @NoArgsConstructor
public class AttemptAnswer {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false) @JoinColumn(name = "attempt_id")
    private Attempt attempt;
    @ManyToOne(optional = false) @JoinColumn(name = "question_id")
    private Question question;
    private String selectedOption;
    @Column(name = "is_correct")
    private boolean correct;
}
