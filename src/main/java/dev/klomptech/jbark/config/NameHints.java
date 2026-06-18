package dev.klomptech.jbark.config;

import java.util.Collection;

// Hints for dog-themed error messages
public final class NameHints {

  private static final int MAX_DISTANCE = 2;

  private NameHints() {}

  public static String suggest(final String name, final Collection<String> candidates) {
    if (name == null || candidates == null || candidates.isEmpty()) {
      return null;
    }
    String normalized = ConfigLoader.normalise(name);
    String phrase = ConfigLoader.resolveNamePhrase(normalized);
    if (phrase != null) {
      normalized = phrase;
    }
    String best = null;
    int bestDistance = MAX_DISTANCE + 1;
    for (String candidate : candidates) {
      int distance = distance(normalized, candidate);
      if (distance < bestDistance) {
        bestDistance = distance;
        best = candidate;
      }
    }
    return bestDistance <= MAX_DISTANCE ? best : null;
  }

  public static String hintPhrase(final String name, final Collection<String> candidates) {
    String suggestion = suggest(name, candidates);
    if (suggestion == null || suggestion.equals(ConfigLoader.normalise(name))) {
      return "";
    }
    String display = suggestion.contains("_") ? suggestion.replace('_', ' ') : suggestion;
    return " Did you mean " + display + "?";
  }

  private static int distance(final String left, final String right) {
    int[][] table = new int[left.length() + 1][right.length() + 1];
    for (int i = 0; i <= left.length(); i++) {
      table[i][0] = i;
    }
    for (int j = 0; j <= right.length(); j++) {
      table[0][j] = j;
    }
    for (int i = 1; i <= left.length(); i++) {
      for (int j = 1; j <= right.length(); j++) {
        int cost = left.charAt(i - 1) == right.charAt(j - 1) ? 0 : 1;
        table[i][j] =
            Math.min(
                Math.min(table[i - 1][j] + 1, table[i][j - 1] + 1), table[i - 1][j - 1] + cost);
      }
    }
    return table[left.length()][right.length()];
  }
}
