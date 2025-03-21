package com.multitask.backend.service;

import com.multitask.backend.domain.ScoreDTO;
import com.multitask.backend.entity.Score;
import com.multitask.backend.repository.ScoreRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ScoreService {

    private final ScoreRepository scoreRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public ScoreService(ScoreRepository scoreRepository, ModelMapper modelMapper) {
        this.scoreRepository = scoreRepository;
        this.modelMapper = modelMapper;
    }

    public void saveScore(ScoreDTO dto) {
        Score score = modelMapper.map(dto, Score.class);
        score.setSubmittedAt(LocalDateTime.now());
        scoreRepository.save(score);
    }

    public List<ScoreDTO> getTop10Scores() {
        return scoreRepository.findTop10ByOrderByScoreDesc().stream()
                .map(this::convertToDto)
                .toList();
    }

    private ScoreDTO convertToDto(Score score) {
        return modelMapper.map(score, ScoreDTO.class);
    }

    private Score convertToEntity(ScoreDTO dto) {
        return modelMapper.map(dto, Score.class);
    }
}
