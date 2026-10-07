package com.smartexam.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "attempts")
@Getter @Setter @NoArgsConstructor
public class Attempt {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false) @JoinColumn(name = "user_id")
    private User user;
    @ManyToOne(optional = false) @JoinColumn(name = "exam_id")
    private Exam exam;
    private Integer score;
    private Integer total;
    private LocalDateTime startedAt;
    private LocalDateTime submittedAt;
}
