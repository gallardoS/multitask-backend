package com.multitask.backend.controller;

import com.multitask.backend.domain.ScoreDTO;
import com.multitask.backend.service.ScoreService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/scores")
@Slf4j
public class ScoreController {

    private final ScoreService scoreService;

    public ScoreController(ScoreService scoreService) {
        this.scoreService = scoreService;
    }

    @PostMapping
    public ResponseEntity<Void> submitScore(
            @RequestBody ScoreDTO request,
            @RequestHeader("X-Signature") String signature) {
        log.info("[SCORE] submitScore from {}", request.getPlayerName());
        scoreService.saveScore(request, signature);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/top10")
    public ResponseEntity<List<ScoreDTO>> getTop10() {
        return ResponseEntity.ok(scoreService.getTop10Scores());
    }

    @GetMapping("/ping")
    public ResponseEntity<String> ping(HttpServletRequest request) {
        log.info("Received ping from {}", request.getRemoteAddr());
        return ResponseEntity.status(HttpStatus.OK).body("pong");
    }
}
