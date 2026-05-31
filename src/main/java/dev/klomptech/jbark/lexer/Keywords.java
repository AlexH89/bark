package dev.klomptech.jbark.lexer;

import java.util.Map;

import dev.klomptech.jbark.interpreter.PrintStyle;

public final class Keywords {
    
    public static final Map<String, TokenType> TYPES = Map.ofEntries(
        // booleans
        Map.entry("true", TokenType.BOOLEAN),
        Map.entry("false", TokenType.BOOLEAN),

        // Input
        Map.entry("listen", TokenType.STDIN),
        Map.entry("sniff", TokenType.STDIN),
        Map.entry("perk", TokenType.STDIN),
        Map.entry("hear", TokenType.STDIN),
        Map.entry("eavesdrop", TokenType.STDIN),
        Map.entry("wait", TokenType.STDIN),

        // print aliases
        Map.entry("bark", TokenType.PRINT),
        Map.entry("whimper", TokenType.PRINT),
        Map.entry("growl", TokenType.PRINT),
        Map.entry("howl", TokenType.PRINT),
        Map.entry("woof", TokenType.PRINT),
        Map.entry("whine", TokenType.PRINT),
        Map.entry("yapping", TokenType.PRINT),
        Map.entry("whining", TokenType.PRINT)
    );

    public static final Map<String, PrintStyle> PRINT_STYLES = Map.ofEntries(
        Map.entry("bark", PrintStyle.BARK),
        Map.entry("growl", PrintStyle.GROWL),
        Map.entry("whimper", PrintStyle.WHIMPER),
        Map.entry("howl", PrintStyle.HOWL),
        Map.entry("woof", PrintStyle.WOOF),
        Map.entry("whine", PrintStyle.WHINE),
        Map.entry("yapping", PrintStyle.YAPPING),
        Map.entry("whining", PrintStyle.WHINING)
    );

    private Keywords() {}
}
