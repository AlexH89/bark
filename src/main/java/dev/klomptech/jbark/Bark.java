package dev.klomptech.jbark;

import dev.klomptech.jbark.cli.DogArt;
import dev.klomptech.jbark.cli.Version;
import dev.klomptech.jbark.config.ConfigLoader;
import dev.klomptech.jbark.errors.BarkError;
import dev.klomptech.jbark.errors.ExitCode;
import dev.klomptech.jbark.interpreter.Interpreter;
import dev.klomptech.jbark.lexer.Lexer;
import dev.klomptech.jbark.parser.AstNode;
import dev.klomptech.jbark.parser.Parser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Bark {
  public static void main(final String[] args) {
    BarkOptions options = BarkOptions.defaults();
    String scriptPath = null;
    for (String arg : args) {
      if (isHelpFlag(arg)) {
        printHelp();
        System.exit(ExitCode.OK.code);
        return;
      }
      if (isVersionFlag(arg)) {
        Version.print();
        System.exit(ExitCode.OK.code);
        return;
      }
      if ("--strict".equals(arg)) {
        options = new BarkOptions(true, options.quiet());
        continue;
      }
      if ("--quiet".equals(arg)) {
        options = new BarkOptions(options.strict(), true);
        continue;
      }
      if ("--list-breeds".equals(arg)) {
        listNames("Breeds", ConfigLoader.listBreeds());
        System.exit(ExitCode.OK.code);
        return;
      }
      if ("--list-objects".equals(arg)) {
        listNames("Objects", ConfigLoader.listObjects());
        System.exit(ExitCode.OK.code);
        return;
      }
      if (arg.startsWith("-")) {
        System.err.println("Unknown flag: " + arg + ". Try bark --help");
        System.exit(ExitCode.USAGE.code);
        return;
      }
      if (scriptPath != null) {
        System.err.println("Usage: bark [flags] [script.woof]. Try bark --help");
        System.exit(ExitCode.USAGE.code);
        return;
      }
      scriptPath = arg;
    }

    String source;
    try {
      if (scriptPath != null) {
        source = Files.readString(Paths.get(scriptPath));
      } else {
        System.out.print("> ");
        source = new String(System.in.readAllBytes());
      }
    } catch (final IOException e) {
      System.err.println("Couldn't dig up that file: " + e.getMessage());
      System.exit(ExitCode.INVALID_ARGUMENTS.code);
      return;
    }
    System.exit(run(source, options));
  }

  private static void printHelp() {
    System.out.println(
        """
            bark: a dog-themed story programming language

            Usage:
              bark [script.woof]          Run a .woof file
              bark                        Read lines from stdin (Ctrl+D to finish)
              bark --version              Show version and a dog fact
              bark --list-breeds          List registered breeds
              bark --list-objects         List registered objects
              bark --strict script.woof   Warn on story-only lines that do nothing
              bark --quiet script.woof    Suppress startup banner and goodbye

            Docs: docs/AUTHOR.md · docs/MANUAL.md
            """);
  }

  private static void listNames(final String label, final List<String> names) {
    System.out.println(label + ":");
    for (String name : names) {
      System.out.println("  " + name);
    }
  }

  private static boolean isHelpFlag(final String arg) {
    return "--help".equals(arg) || "-h".equals(arg);
  }

  private static boolean isVersionFlag(final String arg) {
    return "--version".equals(arg) || "-version".equals(arg);
  }

  private static int run(final String source, final BarkOptions options) {
    try {
      if (!options.quiet()) {
        DogArt.printBanner();
      }
      Lexer lexer = new Lexer(source);
      Parser parser = new Parser(lexer.tokenise(), options);
      AstNode.Program program = parser.parse();
      new Interpreter().run(program);
      if (!options.quiet()) {
        DogArt.printGoodbye();
      }
      return ExitCode.OK.code;
    } catch (final BarkError e) {
      System.err.println(e);
      return ExitCode.DATA_ERROR.code;
    }
  }
}
