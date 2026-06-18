package dev.klomptech.jbark.parser.collection;

import java.util.HashMap;
import java.util.Map;

// Remind me again why I had to overdo this and not just limit to zero to ten?
public final class SmartOrdinalParser {

  private SmartOrdinalParser() {}

  private static final Map<String, Integer> VOCAB = new HashMap<>();

  static {
    // Base numbers and their standard/ordinal variations mapped to single primitives
    String[] ones = {
      "zero",
      "one",
      "two",
      "three",
      "four",
      "five",
      "six",
      "seven",
      "eight",
      "nine",
      "ten",
      "eleven",
      "twelve",
      "thirteen",
      "fourteen",
      "fifteen",
      "sixteen",
      "seventeen",
      "eighteen",
      "nineteen"
    };
    String[] ordinals = {
      "zeroth",
      "first",
      "second",
      "third",
      "fourth",
      "fifth",
      "sixth",
      "seventh",
      "eighth",
      "ninth",
      "tenth",
      "eleventh",
      "twelfth",
      "thirteenth",
      "fourteenth",
      "fifteenth",
      "sixteen-th",
      "seventeenth",
      "eighteenth",
      "nineteenth"
    };
    for (int i = 0; i < ones.length; i++) {
      VOCAB.put(ones[i], i);
      VOCAB.put(ordinals[i], i);
    }
    // Tens rules
    VOCAB.put("twenty", 20);
    VOCAB.put("twentieth", 20);
    VOCAB.put("thirty", 30);
    VOCAB.put("thirtieth", 30);
    VOCAB.put("forty", 40);
    VOCAB.put("fortieth", 40);
    VOCAB.put("fifty", 50);
    VOCAB.put("fiftieth", 50);
    VOCAB.put("sixty", 60);
    VOCAB.put("sixtieth", 60);
    VOCAB.put("seventy", 70);
    VOCAB.put("seventieth", 70);
    VOCAB.put("eighty", 80);
    VOCAB.put("eightieth", 80);
    VOCAB.put("ninety", 90);
    VOCAB.put("ninetieth", 90);

    // Scales
    VOCAB.put("hundred", 100);
    VOCAB.put("hundredth", 100);
    VOCAB.put("thousand", 1000);
    VOCAB.put("thousandth", 1000);
    VOCAB.put("million", 1000000);
    VOCAB.put("millionth", 1000000);
  }

  public static int parseWordToNumber(final String input) {
    if (input == null || input.isBlank()) return 0;
    // Handle custom keywords like last
    String clean = input.trim().toLowerCase();
    if (clean.equals("last")) return -1;
    // Split text by spaces, hyphens, and remove "and" bridges
    String[] tokens = clean.split("[\\s-]+");
    int totalSum = 0;
    int currentSection = 0;
    for (String token : tokens) {
      if (token.equals("and")) continue;
      if (!VOCAB.containsKey(token)) continue; // Ignore unrecognized tokens
      int value = VOCAB.get(token);
      if (value == 100) {
        // Hundreds scale applies to the immediate group
        currentSection *= value;
      } else if (value >= 1000) {
        // Major scales lock in the current segment and multiply it
        currentSection *= value;
        totalSum += currentSection;
        currentSection = 0;
      } else {
        // Add units and tens directly
        currentSection += value;
      }
    }
    return totalSum + currentSection;
  }
}
