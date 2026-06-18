package dev.klomptech.jbark.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class Version {

  public static final String NAME = "bark";
  public static final String NUMBER = "1.0.0";

  private static final List<String> DOG_FACTS = loadFacts();

  private Version() {}

  public static void print() {
    System.out.println(NAME + " " + NUMBER);
    String fact = randomDogFact();
    if (fact != null) {
      System.out.println(DogArt.FACT_PREFIX + fact);
    }
  }

  public static String randomDogFact() {
    if (DOG_FACTS.isEmpty()) {
      return null;
    }
    return DOG_FACTS.get(ThreadLocalRandom.current().nextInt(DOG_FACTS.size()));
  }

  private static List<String> loadFacts() {
    List<String> facts = new ArrayList<>();
    try (InputStream in = Version.class.getResourceAsStream("/dogfacts.txt")) {
      if (in == null) {
        return facts;
      }
      try (BufferedReader reader =
          new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
        reader.lines().map(String::trim).filter(line -> !line.isEmpty()).forEach(facts::add);
      }
    } catch (IOException ignored) {
      // no facts
    }
    return facts;
  }
}
