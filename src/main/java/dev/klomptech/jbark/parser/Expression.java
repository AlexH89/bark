package dev.klomptech.jbark.parser;

public sealed interface Expression permits
    Expression.StringLiteral,
    Expression.NumberLiteral,
    Expression.BooleanLiteral,
    Expression.StdinLiteral,
    Expression.NullLiteral,
    Expression.Variable,
    Expression.Comparison {

    int line();

    record StringLiteral(String value, int line) implements Expression {}
    record NumberLiteral(double value, int line) implements Expression {}
    record BooleanLiteral(boolean value, int line) implements Expression {}
    record StdinLiteral(int line) implements Expression {}
    record NullLiteral(int line) implements Expression {}
    record Variable(String name, int line) implements Expression {}
    record Comparison(Expression left, ComparisonOp op, Expression right, int line) implements Expression {}
}
