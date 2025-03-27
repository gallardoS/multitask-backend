package com.multitask.backend.service;

import com.multitask.backend.domain.ScoreDTO;
import com.multitask.backend.entity.Score;
import com.multitask.backend.repository.ScoreRepository;
import com.multitask.backend.security.ScoreSignatureValidator;
import com.multitask.backend.service.impl.ScoreServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ScoreServiceImplTest {

    private ScoreRepository scoreRepository;
    private ScoreSignatureValidator signatureValidator;
    private ScoreService scoreService;

    @BeforeEach
    void setUp() {
        scoreRepository = mock(ScoreRepository.class);
        signatureValidator = mock(ScoreSignatureValidator.class);
        scoreService = new ScoreServiceImpl(scoreRepository, new ModelMapper(), signatureValidator);
    }

    @Test
    void saveScore_withValidSignature_shouldSaveScore() {
        LocalDateTime now = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC);

        ScoreDTO dto = new ScoreDTO();
        dto.setPlayerName("Juan");
        dto.setScore(100);
        dto.setDateTime(now);

        long timestamp = dto.getTimestamp();

        when(signatureValidator.validarFirma("Juan", 100, timestamp, "valid-signature"))
                .thenReturn(true);
        when(scoreRepository.save(any(Score.class))).thenReturn(new Score(1L, "Juan", 100, now));

        scoreService.saveScore(dto, "valid-signature");

        verify(scoreRepository, times(1)).save(argThat(score ->
                score.getPlayerName().equals("Juan") &&
                        score.getScore() == 100 &&
                        score.getDateTime().equals(now)
        ));
    }

    @Test
    void saveScore_withInvalidSignature_shouldThrowSecurityException() {
        LocalDateTime now = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC);

        ScoreDTO dto = new ScoreDTO();
        dto.setPlayerName("Juan");
        dto.setScore(100);
        dto.setDateTime(now);

        when(signatureValidator.validarFirma(any(), anyInt(), eq(dto.getTimestamp()), any()))
                .thenReturn(false);

        assertThrows(SecurityException.class, () ->
                scoreService.saveScore(dto, "invalid-signature")
        );

        verify(scoreRepository, never()).save(any());
    }

    @Test
    void saveScore_withOldTimestamp_shouldThrowIllegalArgumentException() {
        LocalDateTime oldTime = LocalDateTime.ofInstant(Instant.now().minusSeconds(2 * 3600), ZoneOffset.UTC);

        ScoreDTO dto = new ScoreDTO();
        dto.setPlayerName("Juan");
        dto.setScore(100);
        dto.setDateTime(oldTime);

        when(signatureValidator.validarFirma(any(), anyInt(), eq(dto.getTimestamp()), any()))
                .thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
                scoreService.saveScore(dto, "valid-signature")
        );

        verify(scoreRepository, never()).save(any());
    }

    @Test
    void saveScore_withFutureTimestamp_shouldThrowIllegalArgumentException() {
        LocalDateTime futureTime = LocalDateTime.ofInstant(Instant.now().plusSeconds(2 * 3600), ZoneOffset.UTC);

        ScoreDTO dto = new ScoreDTO();
        dto.setPlayerName("Juan");
        dto.setScore(100);
        dto.setDateTime(futureTime);

        when(signatureValidator.validarFirma(any(), anyInt(), eq(dto.getTimestamp()), any()))
                .thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
                scoreService.saveScore(dto, "valid-signature")
        );

        verify(scoreRepository, never()).save(any());
    }

    @Test
    void getTop10Scores_shouldReturnMappedDTOs() {
        LocalDateTime now = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC);

        Score score1 = new Score(1L, "Ana", 200, now);
        Score score2 = new Score(2L, "Luis", 180, now);

        when(scoreRepository.findTop10ByOrderByScoreDesc())
                .thenReturn(List.of(score1, score2));

        List<ScoreDTO> result = scoreService.getTop10Scores();

        assertEquals(2, result.size());

        ScoreDTO dto1 = result.get(0);
        ScoreDTO dto2 = result.get(1);

        assertEquals("Ana", dto1.getPlayerName());
        assertEquals(200, dto1.getScore());
        assertEquals(now, dto1.getDateTime());

        assertEquals("Luis", dto2.getPlayerName());
        assertEquals(180, dto2.getScore());
        assertEquals(now, dto2.getDateTime());
    }
}
