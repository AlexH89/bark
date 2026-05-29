package dev.klomptech.jbark.parser;

import dev.klomptech.jbark.errors.BarkError;
import dev.klomptech.jbark.lexer.Lexer;
import dev.klomptech.jbark.lexer.Token;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ExpressionParserTest {

    private static Expression parse(final String source) throws BarkError {
        List<Token> tokens = new Lexer(source).tokenise();
        return new ExpressionParser().parse(tokens.getFirst());
    }

    @Test
    void stringLiteral() throws BarkError {
        Expression expr = parse("\"hello\"");
        assertInstanceOf(Expression.StringLiteral.class, expr);
        assertEquals("hello", ((Expression.StringLiteral) expr).value());
    }

    @Test
    void numberLiteral() throws BarkError {
        Expression expr = parse("3.14");
        assertInstanceOf(Expression.NumberLiteral.class, expr);
        assertEquals(3.14, ((Expression.NumberLiteral) expr).value());
    }

    @Test
    void booleanLiteral() throws BarkError {
        Expression expr = parse("false");
        assertInstanceOf(Expression.BooleanLiteral.class, expr);
        assertEquals(false, ((Expression.BooleanLiteral) expr).value());
    }

    @Test
    void variable() throws BarkError {
        Expression expr = parse("rover");
        assertInstanceOf(Expression.Variable.class, expr);
        assertEquals("rover", ((Expression.Variable) expr).name());
    }

    @Test
    void stdinLiteral() throws BarkError {
        Expression expr = parse("listen");
        assertInstanceOf(Expression.StdinLiteral.class, expr);
    }

    @Test
    void invalidTokenFails() {
        assertThrows(BarkError.class, () -> parse("="));
    }
}
