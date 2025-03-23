package com.multitask.backend.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

@Component
public class ScoreSignatureValidator {

    private final String secret;

    public ScoreSignatureValidator(@Value("${SCORE_SECRET}") String secret) {
        this.secret = secret;
    }

    public String generarFirma(String username, int score, long timestamp) {
        try {
            String data = username + "|" + score + "|" + timestamp + secret;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error generando firma", e);
        }
    }

    public boolean validarFirma(String username, int score, long timestamp, String firmaRecibida) {
        String firmaEsperada = generarFirma(username, score, timestamp);
        return firmaEsperada.equals(firmaRecibida);
    }
}
