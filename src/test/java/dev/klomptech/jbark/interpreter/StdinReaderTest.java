package dev.klomptech.jbark.interpreter;

import dev.klomptech.jbark.errors.BarkError;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class StdinReaderTest {

    private final InputStream originalIn = System.in;

    @AfterEach
    void restoreStdin() {
        System.setIn(originalIn);
    }

    @Test
    void readsLineFromStdin() throws BarkError {
        System.setIn(new ByteArrayInputStream("Rex\n".getBytes(StandardCharsets.UTF_8)));
        assertEquals("Rex", StdinReader.read(1).display());
    }

    @Test
    void emptyStreamBecomesNull() throws BarkError {
        System.setIn(new ByteArrayInputStream(new byte[0]));
        assertInstanceOf(BarkValue.BarkNull.class, StdinReader.read(1));
    }
}
