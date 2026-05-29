package dev.klomptech.jbark.parser;

import dev.klomptech.jbark.errors.BarkError;
import dev.klomptech.jbark.interpreter.PrintStyle;
import dev.klomptech.jbark.lexer.Lexer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParserTest {

    private static Node.Block parse(final String source) throws BarkError {
        return new Parser(new Lexer(source).tokenise()).parse();
    }

    @Test
    void printStatement() throws BarkError {
        Node.Block block = parse("bark \"hi\"");
        assertEquals(1, block.nodes().size());
        Node.Print print = (Node.Print) block.nodes().getFirst();
        assertEquals(PrintStyle.BARK, print.style());
        assertInstanceOf(Expression.StringLiteral.class, print.value());
    }

    @Test
    void assignStatement() throws BarkError {
        Node.Block block = parse("dog1 42");
        Node.Assign assign = (Node.Assign) block.nodes().getFirst();
        assertEquals("dog1", assign.variable());
        assertInstanceOf(Expression.NumberLiteral.class, assign.value());
    }

    @Test
    void assignFromStdin() throws BarkError {
        Node.Block block = parse("dogName listen");
        Node.Assign assign = (Node.Assign) block.nodes().getFirst();
        assertEquals("dogName", assign.variable());
        assertInstanceOf(Expression.StdinLiteral.class, assign.value());
    }

    @Test
    void tooManyTokensOnLineFails() {
        assertThrows(BarkError.class, () -> parse("bark \"hi\" extra"));
    }

    @Test
    void bareListenFails() {
        BarkError error = assertThrows(BarkError.class, () -> parse("listen"));
        assertTrue(error.getMessage().contains("listen"));
    }

    @Test
    void unknownStatementFails() {
        assertThrows(BarkError.class, () -> parse("42"));
    }
}
