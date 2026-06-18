package dev.klomptech.jbark.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class DogArt {

  public static final String EMOJI = "\uD83D\uDC15";
  public static final String PAW = "\uD83D\uDC3E";
  public static final String FACT_PREFIX = "Funny fact: ";

  private static final List<String> EMOJIS = List.of("\uD83D\uDC15", "\uD83D\uDC36", "woof.");

  private static final List<String> BANNERS =
      List.of(
          """
                ___    __
              /(. .)\\    )
               (*)_____/|
               /        |
              /    |--\\ |
             (_)(_)   (_)
            """,
          """
              / \\__
             (    @\\___
             /         O
            /   (_____/
           /_____/   U
              woof!
            """,
          """
                      __
         (\\,--------'()'--o
          (_    ___    /~"
           (_)_)  (_)_)
            """);

  private static final List<String> GOODBYES = loadGoodbyes();

  private DogArt() {}

  private static List<String> loadGoodbyes() {
    List<String> lines =
        new ArrayList<>(
            List.of(
                "The dogs are now chilling on your bed.",
                "After dragging the bed through the house, no more energy to play.",
                "That was a good walk, time to relax.",
                "The dogs had enough of playing, they went upstairs."));
    try (InputStream in = DogArt.class.getResourceAsStream("/dogfacts.txt")) {
      if (in != null) {
        try (BufferedReader reader =
            new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
          reader
              .lines()
              .map(String::trim)
              .filter(line -> !line.isEmpty())
              .map(line -> FACT_PREFIX + line)
              .forEach(lines::add);
        }
      }
    } catch (final IOException ignored) {
      // keep defaults
    }
    return List.copyOf(lines);
  }

  public static void printBanner() {
    System.out.println(BANNERS.get(ThreadLocalRandom.current().nextInt(BANNERS.size())));
  }

  public static String randomBareBark() {
    return EMOJIS.get(ThreadLocalRandom.current().nextInt(EMOJIS.size()));
  }

  public static void printGoodbye() {
    System.out.println(GOODBYES.get(ThreadLocalRandom.current().nextInt(GOODBYES.size())));
  }

  public static String pawsArt() {
    return PAW + "   " + PAW + "   " + PAW;
  }

  public static void printPaws() {
    System.out.println(pawsArt());
  }
}
