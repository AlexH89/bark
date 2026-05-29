package dev.klomptech.jbark.lexer;

import dev.klomptech.jbark.errors.BarkError;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LexerTest {

    private static List<Token> lex(final String source) throws BarkError {
        return new Lexer(source).tokenise();
    }

    private static Token first(final String source) throws BarkError {
        return lex(source).getFirst();
    }

    @Test
    void printKeyword() throws BarkError {
        assertEquals(TokenType.PRINT, first("bark").type());
        assertEquals("bark", first("bark").value());
    }

    @Test
    void stdinKeyword() throws BarkError {
        assertEquals(TokenType.STDIN, first("listen").type());
    }

    @Test
    void booleanKeyword() throws BarkError {
        assertEquals(TokenType.BOOLEAN, first("true").type());
    }

    @Test
    void identifier() throws BarkError {
        assertEquals(TokenType.IDENTIFIER, first("dogName").type());
    }

    @Test
    void stringLiteral() throws BarkError {
        Token token = first("\"hello\"");
        assertEquals(TokenType.STRING, token.type());
        assertEquals("hello", token.value());
    }

    @Test
    void numberLiteral() throws BarkError {
        Token token = first("3.14");
        assertEquals(TokenType.NUMBER, token.type());
        assertEquals("3.14", token.value());
    }

    @Test
    void hashInsideStringIsNotAComment() throws BarkError {
        Token token = first("\"color #ff0000\"");
        assertEquals("color #ff0000", token.value());
    }

    @Test
    void lineCommentIsSkipped() throws BarkError {
        boolean hasPrint = lex("// comment\nbark").stream()
            .anyMatch(token -> token.type() == TokenType.PRINT);
        assertTrue(hasPrint);
    }

    @Test
    void invalidCharacterFails() {
        BarkError error = assertThrows(BarkError.class, () -> lex("dog1 = 2"));
        assertEquals(1, error.getLine());
    }
}
