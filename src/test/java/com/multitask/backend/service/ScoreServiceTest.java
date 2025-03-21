package com.multitask.backend.service;

import com.multitask.backend.domain.ScoreDTO;
import com.multitask.backend.entity.Score;
import com.multitask.backend.repository.ScoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ScoreServiceTest {

    private ScoreRepository scoreRepository;
    private ModelMapper modelMapper;
    private ScoreService scoreService;

    @BeforeEach
    void setUp() {
        scoreRepository = mock(ScoreRepository.class);
        modelMapper = new ModelMapper();
        scoreService = new ScoreService(scoreRepository, modelMapper);
    }

    @Test
    void saveScore_shouldMapAndSaveScore() {
        ScoreDTO dto = new ScoreDTO();
        dto.setPlayerName("Juan");
        dto.setScore(100);

        scoreService.saveScore(dto);

        verify(scoreRepository, times(1)).save(argThat(score ->
                score.getPlayerName().equals("Juan") &&
                        score.getScore() == 100 &&
                        score.getSubmittedAt() != null
        ));
    }

    @Test
    void getTop10Scores_shouldReturnMappedDTOs() {
        Score score1 = new Score();
        score1.setId(1L);
        score1.setPlayerName("Ana");
        score1.setScore(200);
        score1.setSubmittedAt(LocalDateTime.now());

        Score score2 = new Score();
        score2.setId(2L);
        score2.setPlayerName("Luis");
        score2.setScore(180);
        score2.setSubmittedAt(LocalDateTime.now());

        when(scoreRepository.findTop10ByOrderByScoreDesc())
                .thenReturn(List.of(score1, score2));

        List<ScoreDTO> result = scoreService.getTop10Scores();

        assertEquals(2, result.size());
        assertEquals("Ana", result.get(0).getPlayerName());
        assertEquals(200, result.get(0).getScore());
        assertEquals("Luis", result.get(1).getPlayerName());
        assertEquals(180, result.get(1).getScore());
    }
}
