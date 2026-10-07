package com.smartexam.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "topics")
@Getter @Setter @NoArgsConstructor
public class Topic {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String name;
}
