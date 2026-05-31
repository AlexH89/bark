package dev.klomptech.jbark;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import dev.klomptech.jbark.errors.BarkError;
import dev.klomptech.jbark.errors.ExitCode;
import dev.klomptech.jbark.interpreter.Interpreter;
import dev.klomptech.jbark.lexer.Lexer;
import dev.klomptech.jbark.lexer.Token;
import dev.klomptech.jbark.parser.Node;
import dev.klomptech.jbark.parser.Parser;

public class Bark {
    public static void main(final String[] args) {
        if (args.length == 1 && isVersionFlag(args[0])) {
            Version.print();
            System.exit(ExitCode.OK.code);
            return;
        }

        String source;
        try {
            if (args.length > 1) {
                System.out.println("Usage: bark [script]   (try bark --version)");
                System.exit(ExitCode.USAGE.code);
                return;
            } else if (args.length == 1) {
                source = Files.readString(Paths.get(args[0]));
            } else {
                System.out.print("> ");
                source = new String(System.in.readAllBytes());
            }
        } catch (final IOException e) {
            System.err.println("Couldn't dig up that file: " + e.getMessage());
            System.exit(ExitCode.INVALID_ARGUMENTS.code);
            return;
        }
        System.exit(run(source));
    }

    private static boolean isVersionFlag(final String arg) {
        return "--version".equals(arg) || "-version".equals(arg);
    }

    private static int run(final String source) {
        try {
            Lexer lexer = new Lexer(source);
            List<Token> tokens = lexer.tokenise();

            Parser parser = new Parser(tokens);
            Node.Block ast = parser.parse();

            Interpreter interpreter = new Interpreter();
            interpreter.interpret(ast);

            return ExitCode.OK.code;
        } catch (final BarkError e) {
            System.err.println(e);
            return ExitCode.DATA_ERROR.code;
        }
    }
}
