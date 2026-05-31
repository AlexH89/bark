package dev.klomptech.jbark.parser;

import java.util.List;

import dev.klomptech.jbark.interpreter.PrintStyle;

public sealed interface Node permits
    Node.Print,
    Node.Assign,
    Node.Block {

    int line();
    
    /** Print a single expression with a style (bark, woof, growl, etc.) */
    record Print(PrintStyle style, Expression value, int line) implements Node {}

    /** Assign an expression to a variable name */
    record Assign(String variable, Expression value, int line) implements Node {}

    /** An ordered list of statements, used as a body for blocks */
    record Block(List<Node> nodes, int line) implements Node {}
}
