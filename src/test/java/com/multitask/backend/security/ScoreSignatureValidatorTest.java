import com.multitask.backend.security.ScoreSignatureValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScoreSignatureValidatorTest {

    private ScoreSignatureValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ScoreSignatureValidator("my_secret");
    }

    @Test
    void validarFirma_shouldReturnTrueForCorrectSignature() {
        String playerName = "Alice";
        int score = 1234;
        long timestamp = 1710000000L;

        String firma = validator.generarFirma(playerName, score, timestamp);

        boolean result = validator.validarFirma(playerName, score, timestamp, firma);
        assertTrue(result);
    }

    @Test
    void validarFirma_shouldReturnFalseForTamperedData() {
        String playerName = "Bob";
        int score = 5678;
        long timestamp = 1710000000L;
        String fakeFirma = "abcd1234"; // firma incorrecta

        boolean result = validator.validarFirma(playerName, score, timestamp, fakeFirma);
        assertFalse(result);
    }
}
