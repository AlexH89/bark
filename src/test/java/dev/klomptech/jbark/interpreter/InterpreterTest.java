package dev.klomptech.jbark.interpreter;

import dev.klomptech.jbark.errors.BarkError;
import dev.klomptech.jbark.lexer.Lexer;
import dev.klomptech.jbark.parser.Node;
import dev.klomptech.jbark.parser.Parser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InterpreterTest {

    private final PrintStream originalOut = System.out;
    private final InputStream originalIn = System.in;
    private ByteArrayOutputStream output;

    @BeforeEach
    void captureOutput() {
        output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
    }

    @AfterEach
    void restoreStreams() {
        System.setOut(originalOut);
        System.setIn(originalIn);
    }

    private void interpret(final String source) throws BarkError {
        interpret(source, "");
    }

    private void interpret(final String source, final String stdin) throws BarkError {
        System.setIn(new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8)));
        Node.Block program = new Parser(new Lexer(source).tokenise()).parse();
        new Interpreter().interpret(program);
    }

    private String printed() {
        return output.toString(StandardCharsets.UTF_8);
    }

    @Test
    void printString() throws BarkError {
        interpret("bark \"hello\"");
        assertEquals("hello\n", printed());
    }

    @Test
    void printUsesWoofStyle() throws BarkError {
        interpret("woof \"hey\"");
        assertEquals("Woof! hey\n", printed());
    }

    @Test
    void assignThenPrintVariable() throws BarkError {
        interpret("bones 5\ndogName bones\nbark dogName");
        assertEquals("5\n", printed());
    }

    @Test
    void assignFromStdin() throws BarkError {
        interpret("dogName listen\nbark dogName", "Rex");
        assertEquals("> Rex\n", printed());
    }

    @Test
    void undefinedVariableFails() {
        BarkError error = assertThrows(BarkError.class, () -> interpret("bark missingDog"));
        assertTrue(error.getMessage().contains("missingDog"));
    }
}
