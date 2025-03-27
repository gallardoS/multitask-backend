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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    @Test
    void saveScore_withTimestampExactlyAtLimit_shouldNotThrowException() {
        long offsetSeconds = 60 * 60; // 1 hora
        LocalDateTime oneHourAgo = LocalDateTime.ofInstant(Instant.now().minusSeconds(offsetSeconds), ZoneOffset.UTC);

        ScoreDTO dto = new ScoreDTO();
        dto.setPlayerName("EdgeCase");
        dto.setScore(123);
        dto.setDateTime(oneHourAgo);

        when(signatureValidator.validarFirma(any(), anyInt(), eq(dto.getTimestamp()), any()))
                .thenReturn(true);

        assertDoesNotThrow(() -> scoreService.saveScore(dto, "valid-signature"));
    }

    @Test
    void convertToDto_shouldMapFieldsCorrectly() {
        LocalDateTime now = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC);
        Score score = new Score(1L, "Mapper", 150, now);

        ScoreDTO dto = new ModelMapper().map(score, ScoreDTO.class);

        assertEquals("Mapper", dto.getPlayerName());
        assertEquals(150, dto.getScore());
        assertEquals(now, dto.getDateTime());
        assertEquals(now.toEpochSecond(ZoneOffset.UTC), dto.getTimestamp());
    }

    @Test
    void getTimestamp_withNullDateTime_shouldThrowNullPointerException() {
        ScoreDTO dto = new ScoreDTO();
        dto.setPlayerName("NullTime");
        dto.setScore(100);
        dto.setDateTime(null);

        assertThrows(NullPointerException.class, dto::getTimestamp);
    }
    
    @Test
    void saveScore_concurrentRequests_shouldNotThrowExceptions() throws InterruptedException {
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        LocalDateTime now = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC);

        when(signatureValidator.validarFirma(any(), anyInt(), anyLong(), any()))
                .thenReturn(true);
        when(scoreRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        for (int i = 0; i < threadCount; i++) {
            final int scoreValue = i * 10;
            executor.submit(() -> {
                try {
                    ScoreDTO dto = new ScoreDTO();
                    dto.setPlayerName("Player" + scoreValue);
                    dto.setScore(scoreValue);
                    dto.setDateTime(now);

                    scoreService.saveScore(dto, "valid-signature");
                } catch (Exception e) {
                    fail("Exception occurred during concurrent execution: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        verify(scoreRepository, times(threadCount)).save(any(Score.class));
    }

}
