package dev.klomptech.jbark.interpreter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BarkValueTest {

    @Test
    void equalStringsMatch() {
        BarkValue left = BarkValue.of("Rex");
        BarkValue right = BarkValue.of("Rex");
        assertTrue(left.equalTo(right));
    }

    @Test
    void differentTypesDoNotMatch() {
        assertFalse(BarkValue.of("5").equalTo(BarkValue.of(5.0)));
    }

    @Test
    void numberDisplayDropsTrailingZero() {
        assertEquals("5", BarkValue.of(5.0).display());
    }
}
