package com.multitask.backend.service;

import com.multitask.backend.domain.ScoreDTO;
import com.multitask.backend.entity.Score;
import com.multitask.backend.repository.ScoreRepository;
import com.multitask.backend.security.ScoreSignatureValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ScoreServiceTest {

    private ScoreSignatureValidator signatureValidator;
    private ScoreRepository scoreRepository;
    private ScoreService scoreService;

    @BeforeEach
    void setUp() {
        scoreRepository = mock(ScoreRepository.class);
        ModelMapper modelMapper = new ModelMapper();
        signatureValidator = mock(ScoreSignatureValidator.class);
        scoreService = new ScoreService(scoreRepository, modelMapper, signatureValidator);
    }

    @Test
    void saveScore_withValidSignature_shouldSaveScore() {
        ScoreDTO dto = new ScoreDTO();
        dto.setPlayerName("Juan");
        dto.setScore(100);
        dto.setTimestamp(Instant.now().getEpochSecond());

        Score fakeSavedScore = new Score();
        fakeSavedScore.setId(1L);
        fakeSavedScore.setPlayerName("Juan");
        fakeSavedScore.setScore(100);
        fakeSavedScore.setSubmittedAt(LocalDateTime.now());

        when(scoreRepository.save(any(Score.class))).thenReturn(fakeSavedScore);

        when(signatureValidator.validarFirma(
                eq("Juan"), eq(100), anyLong(), eq("valid-signature")))
                .thenReturn(true);

        scoreService.saveScore(dto, "valid-signature");

        verify(scoreRepository, times(1)).save(argThat(score ->
                score.getPlayerName().equals("Juan") &&
                        score.getScore() == 100
        ));
    }

    @Test
    void saveScore_withInvalidSignature_shouldThrowSecurityException() {
        ScoreDTO dto = new ScoreDTO();
        dto.setPlayerName("Juan");
        dto.setScore(100);
        dto.setTimestamp(Instant.now().getEpochSecond());

        when(signatureValidator.validarFirma(
                anyString(), anyInt(), anyLong(), anyString()))
                .thenReturn(false);

        assertThrows(SecurityException.class, () ->
                scoreService.saveScore(dto, "invalid-signature")
        );

        verify(scoreRepository, never()).save(any());
    }

    @Test
    void saveScore_withOldTimestamp_shouldThrowIllegalArgumentException() {
        ScoreDTO dto = new ScoreDTO();
        dto.setPlayerName("Juan");
        dto.setScore(100);
        dto.setTimestamp(Instant.now().minusSeconds(2 * 60 * 60).getEpochSecond()); // 2 hours

        when(signatureValidator.validarFirma(anyString(), anyInt(), anyLong(), anyString()))
                .thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
                scoreService.saveScore(dto, "valid-signature")
        );

        verify(scoreRepository, never()).save(any());
    }

    @Test
    void saveScore_withFutureTimestamp_shouldThrowIllegalArgumentException() {
        ScoreDTO dto = new ScoreDTO();
        dto.setPlayerName("Juan");
        dto.setScore(100);
        dto.setTimestamp(Instant.now().plusSeconds(2 * 60 * 60).getEpochSecond());

        when(signatureValidator.validarFirma(anyString(), anyInt(), anyLong(), anyString()))
                .thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
                scoreService.saveScore(dto, "valid-signature")
        );

        verify(scoreRepository, never()).save(any());
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
