package com.multitask.backend.domain;

import lombok.Data;

@Data
public class ScoreDTO {
    private String playerName;
    private int score;
    private long timestamp;
}
