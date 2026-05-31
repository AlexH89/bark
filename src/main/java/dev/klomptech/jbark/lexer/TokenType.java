package dev.klomptech.jbark.lexer;

public enum TokenType {
    // Console & STDIN
    STDIN,

    // Literals
    STRING, NUMBER, BOOLEAN, IDENTIFIER,

    // Printing
    PRINT,

    // Structure
    NEWLINE, EOF,
}
