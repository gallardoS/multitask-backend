package com.multitask.backend.service;

import com.multitask.backend.domain.ScoreDTO;
import java.util.List;

public interface ScoreService {
    void saveScore(ScoreDTO dto, String signature);
    List<ScoreDTO> getTop10Scores();
}