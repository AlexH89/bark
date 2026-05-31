package dev.klomptech.jbark.interpreter;

import java.util.Map;

import dev.klomptech.jbark.errors.BarkError;
import dev.klomptech.jbark.parser.ComparisonOp;
import dev.klomptech.jbark.parser.Expression;
import dev.klomptech.jbark.parser.Node;
import dev.klomptech.jbark.parser.Node.Assign;
import dev.klomptech.jbark.parser.Node.Block;
import dev.klomptech.jbark.parser.Node.Print;

public class Interpreter {

    private static final Map<ComparisonOp, Comparator> COMPARATORS = Comparators.create();
    
    public void interpret(final Node.Block ast) throws BarkError {
        Environment globalEnvironment = new Environment();
        for (final Node statement : ast.nodes()) {
            execute(statement, globalEnvironment);
        }
    }

    private void execute(final Node node, final Environment environment) throws BarkError {
        switch (node) {
            case Node.Print print -> executePrint(print, environment);
            case Node.Assign assign -> executeAssign(assign, environment);
            case Node.Block block -> executeBlock(block, environment);
        }
    }

    private void executePrint(final Print print, final Environment environment) throws BarkError {
        BarkValue value = convertToBarkValue(print.value(), environment);
        String output = PrintStyles.format(print.style(), value.display());
        System.out.println(output);
    }

    private void executeAssign(final Assign assign, final Environment environment) throws BarkError {
        BarkValue value = convertToBarkValue(assign.value(), environment);
        environment.define(assign.variable(), value);
    }

    private void executeBlock(final Block block, final Environment environment) throws BarkError {
        for (final Node statement : block.nodes()) {
            execute(statement, environment);
        }
    }

    private BarkValue convertToBarkValue(final Expression expression, final Environment environment) throws BarkError {
        return switch (expression) {
            case Expression.StringLiteral string -> BarkValue.of(string.value());
            case Expression.NumberLiteral number -> BarkValue.of(number.value());
            case Expression.BooleanLiteral booleanValue -> BarkValue.of(booleanValue.value());
            case Expression.NullLiteral _ -> new BarkValue.BarkNull();
            case Expression.Variable variable -> environment.get(variable.name(), variable.line());
            case Expression.Comparison comparison -> compare(comparison, environment);
            case Expression.StdinLiteral stdin -> listenToInput(stdin);
        };
    }

    private BarkValue listenToInput(final Expression.StdinLiteral stdin) throws BarkError {
        System.out.print("> ");
        return StdinReader.read(stdin.line());
    }

    private BarkValue compare(final Expression.Comparison comparison, final Environment environment) throws BarkError {
        BarkValue left = convertToBarkValue(comparison.left(), environment);
        BarkValue right = convertToBarkValue(comparison.right(), environment);
        Comparator comparator = COMPARATORS.get(comparison.op());
        if (comparator == null) {
            throw new BarkError(comparison.line(), "The dogs don't know how to compare things that way: " + comparison.op());
        }
        boolean result = comparator.compare(left, right, comparison.line());
        return BarkValue.of(result ? 1.0 : 0.0);
    }
}
