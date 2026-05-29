package dev.klomptech.jbark.interpreter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PrintStylesTest {

    @Test
    void barkIsPlain() {
        assertEquals("hello", PrintStyles.format(PrintStyle.BARK, "hello"));
    }

    @Test
    void woofAddsPrefix() {
        assertEquals("Woof! hey", PrintStyles.format(PrintStyle.WOOF, "hey"));
    }

    @Test
    void growlUppercases() {
        assertEquals("LOUD", PrintStyles.format(PrintStyle.GROWL, "loud"));
    }

    @Test
    void whineWrapsInParentheses() {
        assertEquals("(soft)", PrintStyles.format(PrintStyle.WHINE, "soft"));
    }
}
