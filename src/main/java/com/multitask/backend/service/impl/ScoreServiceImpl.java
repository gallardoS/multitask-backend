package com.multitask.backend.service.impl;

import com.multitask.backend.domain.ScoreDTO;
import com.multitask.backend.entity.Score;
import com.multitask.backend.repository.ScoreRepository;
import com.multitask.backend.security.ScoreSignatureValidator;
import com.multitask.backend.service.ScoreService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.List;

@Service
public class ScoreServiceImpl implements ScoreService {
    private final ScoreSignatureValidator signatureValidator;
    private final ScoreRepository scoreRepository;
    private final ModelMapper modelMapper;

    private static final long MAX_TIMESTAMP_DRIFT_SECONDS = 60 * 60; //one hour

    public ScoreServiceImpl(ScoreRepository scoreRepository, ModelMapper modelMapper, ScoreSignatureValidator signatureValidator) {
        this.scoreRepository = scoreRepository;
        this.modelMapper = modelMapper;
        this.signatureValidator = signatureValidator;
    }

    @Override
    public void saveScore(ScoreDTO dto, String signature) {
        boolean validSignature = signatureValidator.validarFirma(dto.getPlayerName(), dto.getScore(), dto.getTimestamp(), signature);

        if (!validSignature) {
            throw new SecurityException("Invalid signature");
        }

        long now = Instant.now().getEpochSecond();
        if (Math.abs(now - dto.getTimestamp()) > MAX_TIMESTAMP_DRIFT_SECONDS) {
            throw new IllegalArgumentException("Invalid timestamp");
        }

        Score score = convertToEntity(dto);
        scoreRepository.save(score);
    }

    @Override
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
