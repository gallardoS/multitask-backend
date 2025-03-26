package com.multitask.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "scores")
@Data @NoArgsConstructor @AllArgsConstructor
public class Score {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String playerName;

    @Column(nullable = false)
    private int score;

    @Column(nullable = false)
    private LocalDateTime dateTime;
}
