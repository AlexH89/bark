package dev.klomptech.jbark.config;

import dev.klomptech.jbark.errors.BarkError;
import dev.klomptech.jbark.lexer.Token;
import dev.klomptech.jbark.lexer.TokenType;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class ConfigLoader {

  // Global story variables
  public static final String MEMORY = "memory";
  public static final String JOURNAL = "journal";

  private static final Map<String, VariableType> INDEX = new HashMap<>();
  private static final Map<String, String> PHRASE_ALIASES = new HashMap<>();

  private ConfigLoader() {
    // prevent instantiation
  }

  static {
    load("/breeds.txt", VariableType.BREED, INDEX);
    load("/objects.txt", VariableType.OBJECT, INDEX);
    load("/stashes.txt", VariableType.STASH, INDEX);
    load("/piles.txt", VariableType.PILE, INDEX);
    INDEX.put(MEMORY, VariableType.STORY_NUMBER);
    INDEX.put(JOURNAL, VariableType.STORY_TEXT);
    buildPhraseAliases();
  }

  private static void load(
      final String path, final VariableType type, final Map<String, VariableType> index) {
    try (InputStream in = ConfigLoader.class.getResourceAsStream(path)) {
      if (in == null) {
        throw new IllegalStateException("Resource not found: " + path);
      }
      try (BufferedReader reader =
          new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
        reader
            .lines()
            .map(String::trim)
            .filter(line -> !line.isEmpty())
            .map(ConfigLoader::normalise)
            .forEach(
                word -> {
                  index.put(word, type);
                });
      }
    } catch (IOException e) {
      throw new IllegalStateException("Failed to load resource: " + path, e);
    }
  }

  // cookie_jar also answers to "cookie jar" in stories
  private static void buildPhraseAliases() {
    for (String name : INDEX.keySet()) {
      PHRASE_ALIASES.put(name, name);
      if (name.contains("_")) {
        PHRASE_ALIASES.put(name.replace('_', ' '), name);
      }
    }
  }

  public static String normalise(final String name) {
    return name.trim().toLowerCase();
  }

  public static Optional<VariableType> typeOf(final String name) {
    return Optional.ofNullable(INDEX.get(normalise(name)));
  }

  public static boolean isValidName(final String name) {
    return typeOf(name).isPresent();
  }

  public static boolean isBreed(final String name) {
    return typeOf(name).orElse(null) == VariableType.BREED;
  }

  public static boolean isStash(final String name) {
    return typeOf(name).orElse(null) == VariableType.STASH;
  }

  public static boolean isPile(final String name) {
    return typeOf(name).orElse(null) == VariableType.PILE;
  }

  public static boolean isStoryNumberConstant(final String name) {
    return typeOf(name).orElse(null) == VariableType.STORY_NUMBER;
  }

  public static boolean isStoryTextConstant(final String name) {
    return typeOf(name).orElse(null) == VariableType.STORY_TEXT;
  }

  public static boolean isStoryConstant(final String name) {
    return isStoryNumberConstant(name);
  }

  public static String memoryName() {
    return MEMORY;
  }

  public static String journalName() {
    return JOURNAL;
  }

  // Token index after a resolved name phrase on a line slice, or -1
  public static int tokenIndexAfterResolvedName(
      final List<Token> tokens, final int from, final int to, final String resolvedName) {
    List<String> words = new ArrayList<>();
    List<Integer> positions = new ArrayList<>();
    for (int i = from; i < to; i++) {
      Token token = tokens.get(i);
      if (token.is(TokenType.IDENTIFIER)) {
        words.add(normalise(token.value()));
        positions.add(i);
      }
    }
    for (int length = Math.min(4, words.size()); length >= 1; length--) {
      for (int start = 0; start <= words.size() - length; start++) {
        String phrase = String.join(" ", words.subList(start, start + length));
        if (resolvedName.equals(resolveNamePhrase(phrase))) {
          return positions.get(start + length - 1) + 1;
        }
      }
    }
    String key = normalise(resolvedName);
    for (int i = from; i < to; i++) {
      Token token = tokens.get(i);
      if (token.is(TokenType.IDENTIFIER) && key.equals(normalise(token.value()))) {
        return i + 1;
      }
    }
    return -1;
  }

  public static List<String> listNames(final VariableType type) {
    return INDEX.entrySet().stream()
        .filter(entry -> entry.getValue() == type)
        .map(Map.Entry::getKey)
        .sorted()
        .toList();
  }

  public static List<String> listBreeds() {
    return listNames(VariableType.BREED);
  }

  public static List<String> listObjects() {
    return listNames(VariableType.OBJECT);
  }

  public static List<String> listStashes() {
    return listNames(VariableType.STASH);
  }

  public static List<String> listPiles() {
    return listNames(VariableType.PILE);
  }

  public static String resolveNamePhrase(final String phrase) {
    return PHRASE_ALIASES.get(normalise(phrase));
  }

  public static String resolveNameFromTokens(
      final List<Token> tokens, final int from, final int to) {
    List<String> words = identifierWords(tokens, from, to);
    skipLeadingStoryNoise(words);
    return matchLongestNamePhrase(words);
  }

  // Longest registered stash or pile name on a line slice
  // Example: her cookie jar holds "biscuit" -> cookie_jar from stashes.txt.
  public static String resolveCollectionFromTokens(
      final List<Token> tokens, final int from, final int to) {
    List<String> words = identifierWords(tokens, from, to);
    skipLeadingStoryNoise(words);
    String best = null;
    int bestLength = 0;
    for (int length = Math.min(4, words.size()); length >= 1; length--) {
      for (int start = 0; start <= words.size() - length; start++) {
        String phrase = String.join(" ", words.subList(start, start + length));
        String resolved = resolveNamePhrase(phrase);
        if (resolved != null && (isStash(resolved) || isPile(resolved)) && length >= bestLength) {
          best = resolved;
          bestLength = length;
        }
      }
    }
    return best;
  }

  // Drop leading story words (her, my, …) until a registered name phrase can start
  private static void skipLeadingStoryNoise(final List<String> words) {
    while (!words.isEmpty()) {
      if (isValidName(words.get(0)) || resolveNamePhrase(words.get(0)) != null) {
        break;
      }
      boolean nameAhead = false;
      for (int length = 1; length <= Math.min(4, words.size()); length++) {
        if (resolveNamePhrase(String.join(" ", words.subList(0, length))) != null) {
          nameAhead = true;
          break;
        }
      }
      if (nameAhead) {
        break;
      }
      words.remove(0);
    }
  }

  private static List<String> identifierWords(
      final List<Token> tokens, final int from, final int to) {
    List<String> words = new ArrayList<>();
    for (int i = from; i < to; i++) {
      Token token = tokens.get(i);
      if (token.is(TokenType.IDENTIFIER)) {
        words.add(normalise(token.value()));
      }
    }
    return words;
  }

  private static String matchLongestNamePhrase(final List<String> words) {
    for (int length = Math.min(4, words.size()); length >= 1; length--) {
      for (int start = 0; start <= words.size() - length; start++) {
        String phrase = String.join(" ", words.subList(start, start + length));
        String resolved = resolveNamePhrase(phrase);
        if (resolved != null) {
          return resolved;
        }
      }
    }
    return null;
  }

  public static void requireStash(final String name, final int line) throws BarkError {
    if (!isStash(name)) {
      throw new BarkError(
          line,
          "'"
              + name
              + "' is not a not something the dogs can get stuff from or bury in."
              + NameHints.hintPhrase(name, listStashes()));
    }
  }

  public static void requirePile(final String name, final int line) throws BarkError {
    if (!isPile(name)) {
      throw new BarkError(
          line,
          "'"
              + name
              + "' is not a registered pile. wrong spot."
              + NameHints.hintPhrase(name, listPiles()));
    }
  }

  public static void requireBreed(final String name, final int line) throws BarkError {
    if (!isBreed(name)) {
      throw new BarkError(
          line,
          "\""
              + name
              + "\" isn't a breed we know about. Garbage bin mix??"
              + NameHints.hintPhrase(name, listBreeds()));
    }
  }
}
