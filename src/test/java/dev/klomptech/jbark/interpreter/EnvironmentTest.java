package dev.klomptech.jbark.interpreter;

import dev.klomptech.jbark.errors.BarkError;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnvironmentTest {

    @Test
    void defineAndGet() throws BarkError {
        Environment env = new Environment();
        env.define("dogName", BarkValue.of("Rex"));
        assertEquals("Rex", env.get("dogName", 1).display());
    }

    @Test
    void hasReturnsFalseForMissingName() {
        assertFalse(new Environment().has("missing"));
    }

    @Test
    void getMissingNameFails() {
        BarkError error = assertThrows(BarkError.class, () -> new Environment().get("missing", 3));
        assertTrue(error.getMessage().contains("missing"));
        assertEquals(3, error.getLine());
    }
}
