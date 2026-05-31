package dev.klomptech.jbark.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import dev.klomptech.jbark.errors.BarkError;
import dev.klomptech.jbark.interpreter.PrintStyle;
import dev.klomptech.jbark.lexer.Keywords;
import dev.klomptech.jbark.lexer.Token;
import dev.klomptech.jbark.lexer.TokenType;

public class Parser {

    private final List<Token> tokens;
    private final Map<TokenType, StatementParser> statementParsers;
    private final ExpressionParser expressions;
    private int current = 0;

    public Parser(final List<Token> tokens) {
        this.tokens = tokens;
        this.statementParsers = StatementParsers.create(this);
        this.expressions = new ExpressionParser();
    }

    public Node.Block parse() throws BarkError {
        List<Node> statements = new ArrayList<>();
        skipNewlines();
        while (!isAtEnd()) {
            statements.add(parseStatement());
            skipNewlines();
        }
        return new Node.Block(statements, 1);
    }

    private Node parseStatement() throws BarkError {
        Token token = peek();
        if (token.is(TokenType.STDIN)) {
            throw new BarkError(
                token.line(),
                "A dog can't " + token.value() + " at nothing. Point those ears at a name first, like dogName "
                    + token.value() + ".");
        }
        StatementParser statementParser = statementParsers.get(token.type());
        if (statementParser != null) {
            return statementParser.parse();
        }
        throw new BarkError(token.line(), "\"" + token.value() + "\"? The dogs don't know that trick yet.");
    }

    Node parsePrint() throws BarkError {
        Token keyword = advance();
        PrintStyle style = Keywords.PRINT_STYLES.get(keyword.value().toLowerCase());
        if (style == null) {
            throw new BarkError(keyword.line(), "Never heard a dog go \"" + keyword.value() + "\". Is this a cat?");
        }
        Expression value = parseLineExpression(keyword.line(), "\"" + keyword.value() + "\"");
        return new Node.Print(style, value, keyword.line());
    }

    Node parseAssign() throws BarkError {
        Token variable = advance();
        Expression value = parseLineExpression(variable.line(), "\"" + variable.value() + "\"");
        return new Node.Assign(variable.value(), value, variable.line());
    }

    private Expression parseLineExpression(final int line, final String context) throws BarkError {
        if (atLineEnd()) {
            throw new BarkError(line, context + " called but brought nothing to play and no treat?.");
        }
        Expression value = expressions.parse(advance());
        if (!atLineEnd()) {
            Token extra = peek();
            throw new BarkError(
                line,
                "Too many treats on one line: " + context + " can only handle one thing, not \"" + extra.value() + "\".");
        }
        return value;
    }

    Token peek() {
        return tokens.get(current);
    }

    Token advance() {
        Token token = tokens.get(current);
        if (!isAtEnd()) {
            current++;
        }
        return token;
    }

    boolean isAtEnd() {
        return peek().is(TokenType.EOF);
    }

    boolean check(final TokenType type) {
        return peek().is(type);
    }

    boolean atLineEnd() {
        return isAtEnd() || check(TokenType.NEWLINE);
    }

    private void skipNewlines() {
        while (check(TokenType.NEWLINE)) {
            advance();
        }
    }
}
