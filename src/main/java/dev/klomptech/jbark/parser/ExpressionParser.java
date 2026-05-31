package dev.klomptech.jbark.parser;

import dev.klomptech.jbark.errors.BarkError;
import dev.klomptech.jbark.lexer.Token;

final class ExpressionParser {

    Expression parse(final Token token) throws BarkError {
        return switch (token.type()) {
            case STRING -> new Expression.StringLiteral(token.value(), token.line());
            case NUMBER -> new Expression.NumberLiteral(
                Double.parseDouble(token.value()), token.line());
            case BOOLEAN -> new Expression.BooleanLiteral(Boolean.parseBoolean(token.value()), token.line());
            case STDIN -> new Expression.StdinLiteral(token.line());
            case IDENTIFIER -> new Expression.Variable(token.value(), token.line());
            default -> throw new BarkError(
                token.line(),
                "\"" + token.value() + "\" isn't something a dog can fetch.");
        };
    }
}
