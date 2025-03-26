package com.multitask.backend.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Data
public class ScoreDTO {
    private String playerName;
    private int score;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dateTime;

    @JsonIgnore
    public long getTimestamp() {
        return dateTime.toEpochSecond(ZoneOffset.UTC);
    }
}
