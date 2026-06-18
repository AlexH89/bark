package dev.klomptech.jbark.parser;

import dev.klomptech.jbark.config.ConfigLoader;
import dev.klomptech.jbark.config.DogTopics;
import dev.klomptech.jbark.config.VariableType;
import dev.klomptech.jbark.lexer.Token;
import dev.klomptech.jbark.lexer.TokenType;
import dev.klomptech.jbark.parser.keywords.AttributeKeywordGroups;
import dev.klomptech.jbark.parser.keywords.CollectionKeywordGroups;
import dev.klomptech.jbark.parser.keywords.ControlFlowKeywordGroups;
import dev.klomptech.jbark.parser.keywords.KeywordRegistry;
import dev.klomptech.jbark.parser.keywords.LogicKeywordGroups;
import dev.klomptech.jbark.parser.keywords.PrintKeywordGroups;
import dev.klomptech.jbark.print.PrintStyle;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class Keywords {

  private Keywords() {}

  public static final List<String> PRINT_KEYWORDS;
  public static final Map<String, PrintStyle> PRINT_STYLES;
  public static final List<String> BOOLEAN_VALUES;
  public static final List<String> NULL_VALUES;
  public static final List<String> IF_START_WORDS;
  public static final List<String> OTHERWISE_WORDS;
  public static final List<String> ELSE_WORDS;
  public static final List<String> WHILE_KEYWORDS;
  public static final List<String> FOR_WORDS;
  public static final List<String> EACH_WORDS;
  public static final List<String> FROM_WORDS;
  public static final List<String> UNTIL_KEYWORDS;
  public static final List<String> THEN_KEYWORDS;
  public static final List<String> BURY_KEYWORDS;
  public static final List<String> BREAK_KEYWORDS;
  public static final List<String> CONTINUE_KEYWORDS;
  public static final List<String> EXPECTS_WORDS;
  public static final List<String> TRICK_RETURNS_WORDS;
  public static final List<String> HOLDS_WORDS;
  public static final List<String> FINDS_WORDS;
  public static final List<String> STASH_ADD_WORDS;
  public static final List<String> PILE_GRAB_FROM_WORDS;
  public static final List<String> PILE_PUSH_INTO_WORDS;
  public static final List<String> DROPS_WORDS;
  public static final List<String> DIG_WORDS;
  public static final List<String> COUNT_WORDS;
  public static final List<String> JOIN_WORDS;
  public static final List<String> TAKES_WORDS;
  public static final List<String> GIVES_WORDS;
  public static final List<String> BACK_WORDS;
  public static final List<String> WITH_WORDS;
  public static final List<String> TOP_WORDS;
  public static final List<String> STASH_SPOT_WORDS;
  public static final List<String> LETTERS_WORDS;
  public static final List<String> INSIDE_WORDS;
  public static final List<String> COUNTS_WORDS;
  public static final List<String> LOGICAL_AND_WORDS;
  public static final List<String> CONDITION_AND_WORDS;
  public static final List<String> LOGICAL_OR_WORDS;
  public static final List<String> NEITHER_WORDS;
  public static final List<String> NOR_WORDS;
  public static final List<String> COMPARISON_EQUAL_WORDS;
  public static final List<String> COMPARISON_NOT_EQUAL_WORDS;
  public static final List<String> COMPARISON_GREATER_WORDS;
  public static final List<String> COMPARISON_LESS_WORDS;
  public static final List<String> COMPARISON_AT_LEAST_WORDS;
  public static final List<String> COMPARISON_AT_MOST_WORDS;
  public static final List<String> EXIT_KEYWORDS;
  public static final List<String> STDIN_KEYWORDS;
  public static final List<String> WAIT_KEYWORDS;
  public static final List<String> PLUS_KEYWORDS;
  public static final List<String> MINUS_KEYWORDS;
  public static final List<String> STAR_KEYWORDS;
  public static final List<String> SLASH_KEYWORDS;
  public static final List<String> SHARES_STATEMENT_KEYWORDS;
  public static final List<String> PASSES_STATEMENT_KEYWORDS;
  public static final List<String> TO_WORDS;
  public static final List<String> TRAIT_KEYWORDS;
  public static final List<String> NOT_KEYWORDS;
  public static final List<String> LIST_AND_WORDS;
  public static final List<String> THE_WORDS;
  public static final List<String> OF_WORDS;
  public static final List<String> ITEMS_WORDS;
  public static final List<String> IN_WORDS;
  public static final List<String> AS_WORDS;
  public static final List<String> BY_WORDS;
  public static final List<String> HOW_WORDS;
  public static final List<String> MANY_WORDS;
  public static final List<String> YEARS_WORDS;
  public static final List<String> OLD_WORDS;
  public static final List<String> TYPE_NUMBER_WORDS;
  public static final List<String> TYPE_WORDS_WORDS;
  public static final List<String> TYPE_NOTHING_WORDS;
  public static final List<String> SNIFFS_WORDS;
  public static final List<String> GROWLS_WORDS;
  public static final List<String> SAME_WORDS;
  public static final List<String> THAN_WORDS;
  public static final List<String> AT_WORDS;
  public static final List<String> TOGETHER_WORDS;
  public static final List<String> OR_WORDS;
  public static final List<String> BREED_PRONOUN_WORDS;
  public static final List<String> FEMININE_BREED_PRONOUNS;
  public static final List<String> MASCULINE_BREED_PRONOUNS;
  public static final List<String> OBJECT_PRONOUN_WORDS;
  public static final List<String> ARTICLE_WORDS;
  public static final List<String> ATTRIBUTE_INCREMENT_WORDS;
  public static final List<String> ATTRIBUTE_DECREMENT_WORDS;
  public static final List<String> FOOD_INCREMENT_WORDS;
  public static final List<String> FOOD_DECREMENT_WORDS;
  public static final List<String> ITEM_INCREMENT_WORDS;
  public static final List<String> ITEM_DECREMENT_WORDS;
  public static final List<String> TAKE_FROM_STASH_WORDS;
  public static final List<List<String>> HOW_MANY_ITEMS_IN_PHRASE_STEPS;
  public static final List<List<String>> HOW_MANY_PHRASE_STEPS;
  public static final List<List<String>> EQUAL_COMPARISON_PHRASE_STEPS;
  public static final List<List<String>> NOT_EQUAL_COMPARISON_PHRASE_STEPS;
  public static final List<List<String>> GREATER_COMPARISON_PHRASE_STEPS;
  public static final List<List<String>> LESS_COMPARISON_PHRASE_STEPS;
  public static final List<List<String>> AT_LEAST_COMPARISON_PHRASE_STEPS;
  public static final List<List<String>> AT_MOST_COMPARISON_PHRASE_STEPS;
  public static final List<List<String>> OR_ELSE_PHRASE_STEPS;
  public static final List<List<String>> TOGETHER_WITH_PHRASE_STEPS;
  public static final List<List<String>> SNIFFS_INSIDE_PHRASE_STEPS;
  public static final Map<String, String> OBJECT_ALIASES;

  public enum TypeCheckKind {
    NUMBER,
    WORDS,
    NOTHING
  }

  private static final Set<String> KEYWORD_SET = new HashSet<>();
  private static final Map<String, TypeCheckKind> TYPE_CHECK_KINDS;

  // Synonyms after "counts as", e.g. name counts as words
  private static Map<String, TypeCheckKind> buildTypeCheckKinds() {
    Map<String, TypeCheckKind> map = new HashMap<>();
    for (String word : TYPE_NUMBER_WORDS) {
      map.put(word, TypeCheckKind.NUMBER);
    }
    for (String word : TYPE_WORDS_WORDS) {
      map.put(word, TypeCheckKind.WORDS);
    }
    for (String word : TYPE_NOTHING_WORDS) {
      map.put(word, TypeCheckKind.NOTHING);
    }
    return Map.copyOf(map);
  }

  static {
    KeywordRegistry registry = new KeywordRegistry();
    PrintKeywordGroups print = new PrintKeywordGroups(registry);
    ControlFlowKeywordGroups control = new ControlFlowKeywordGroups(registry);
    CollectionKeywordGroups collection = new CollectionKeywordGroups(registry);
    LogicKeywordGroups logic = new LogicKeywordGroups(registry);
    AttributeKeywordGroups attribute = new AttributeKeywordGroups(registry);

    PRINT_KEYWORDS = print.printWords;
    EXIT_KEYWORDS = print.exitWords;
    STDIN_KEYWORDS = print.stdinWords;
    WAIT_KEYWORDS = print.waitWords;

    BOOLEAN_VALUES = logic.booleanValues;
    NULL_VALUES = logic.nullValues;

    IF_START_WORDS = control.ifStartWords;
    WHILE_KEYWORDS = control.whileWords;
    UNTIL_KEYWORDS = control.untilWords;
    FOR_WORDS = control.forWords;
    EACH_WORDS = control.eachWords;
    FROM_WORDS = control.fromWords;
    THEN_KEYWORDS = control.thenWords;
    OTHERWISE_WORDS = control.otherwiseWords;
    ELSE_WORDS = control.elseWords;
    BURY_KEYWORDS = control.buryWords;
    BREAK_KEYWORDS = control.breakWords;
    CONTINUE_KEYWORDS = control.continueWords;
    EXPECTS_WORDS = control.expectsWords;
    TRICK_RETURNS_WORDS = control.trickReturnsWords;

    HOLDS_WORDS = collection.holdsWords;
    FINDS_WORDS = collection.findsWords;
    STASH_ADD_WORDS = collection.stashAddWords;
    PILE_GRAB_FROM_WORDS = collection.pileGrabFromWords;
    PILE_PUSH_INTO_WORDS = collection.pilePushIntoWords;
    DROPS_WORDS = collection.dropsWords;
    DIG_WORDS = collection.digWords;
    COUNT_WORDS = collection.countWords;
    JOIN_WORDS = collection.joinWords;
    TAKES_WORDS = collection.takesWords;
    TAKE_FROM_STASH_WORDS = collection.takeFromStashWords;
    GIVES_WORDS = collection.givesWords;
    BACK_WORDS = collection.backWords;
    WITH_WORDS = collection.withWords;
    TOP_WORDS = collection.topWords;
    STASH_SPOT_WORDS = collection.stashSpotWords;
    SHARES_STATEMENT_KEYWORDS = collection.sharesWords;
    PASSES_STATEMENT_KEYWORDS = collection.passesWords;
    TO_WORDS = collection.toWords;

    LETTERS_WORDS = logic.lettersWords;
    INSIDE_WORDS = logic.insideWords;
    COUNTS_WORDS = logic.countsWords;
    LOGICAL_AND_WORDS = logic.logicalAndWords;
    CONDITION_AND_WORDS = logic.conditionAndWords;
    LOGICAL_OR_WORDS = logic.logicalOrWords;
    NEITHER_WORDS = logic.neitherWords;
    NOR_WORDS = logic.norWords;
    COMPARISON_EQUAL_WORDS = logic.comparisonEqualWords;
    COMPARISON_NOT_EQUAL_WORDS = logic.comparisonNotEqualWords;
    COMPARISON_GREATER_WORDS = logic.comparisonGreaterWords;
    COMPARISON_LESS_WORDS = logic.comparisonLessWords;
    COMPARISON_AT_LEAST_WORDS = logic.comparisonAtLeastWords;
    COMPARISON_AT_MOST_WORDS = logic.comparisonAtMostWords;

    PLUS_KEYWORDS = logic.plusWords;
    MINUS_KEYWORDS = logic.minusWords;
    STAR_KEYWORDS = logic.starWords;
    SLASH_KEYWORDS = logic.slashWords;
    TRAIT_KEYWORDS = attribute.traitWords;
    NOT_KEYWORDS = logic.notWords;

    LIST_AND_WORDS = attribute.listAndWords;
    THE_WORDS = attribute.theWords;
    OF_WORDS = attribute.ofWords;
    ITEMS_WORDS = attribute.itemsWords;
    IN_WORDS = attribute.inWords;
    AS_WORDS = attribute.asWords;
    BY_WORDS = attribute.byWords;
    HOW_WORDS = attribute.howWords;
    MANY_WORDS = attribute.manyWords;
    YEARS_WORDS = attribute.yearsWords;
    OLD_WORDS = attribute.oldWords;
    TYPE_NUMBER_WORDS = attribute.typeNumberWords;
    TYPE_WORDS_WORDS = attribute.typeWordsWords;
    TYPE_NOTHING_WORDS = attribute.typeNothingWords;
    SNIFFS_WORDS = attribute.sniffWords;
    GROWLS_WORDS = attribute.growlWords;
    SAME_WORDS = attribute.sameWords;
    THAN_WORDS = attribute.thanWords;
    AT_WORDS = attribute.atWords;
    TOGETHER_WORDS = attribute.togetherWords;
    OR_WORDS = attribute.orWords;

    BREED_PRONOUN_WORDS = List.of("he", "she", "him", "her", "his");
    FEMININE_BREED_PRONOUNS = List.of("she", "her");
    MASCULINE_BREED_PRONOUNS = List.of("he", "him", "his");
    OBJECT_PRONOUN_WORDS = List.of("it");
    ARTICLE_WORDS = List.of("a", "an");
    ATTRIBUTE_INCREMENT_WORDS = attribute.incrementWords;
    ATTRIBUTE_DECREMENT_WORDS = attribute.decrementWords;
    FOOD_INCREMENT_WORDS = attribute.foodIncrementWords;
    FOOD_DECREMENT_WORDS = attribute.foodDecrementWords;
    ITEM_INCREMENT_WORDS = attribute.itemIncrementWords;
    ITEM_DECREMENT_WORDS = attribute.itemDecrementWords;

    HOW_MANY_PHRASE_STEPS = List.of(HOW_WORDS, MANY_WORDS);
    HOW_MANY_ITEMS_IN_PHRASE_STEPS = List.of(HOW_WORDS, MANY_WORDS, ITEMS_WORDS, IN_WORDS);
    EQUAL_COMPARISON_PHRASE_STEPS = List.of(SNIFFS_WORDS, SAME_WORDS, AS_WORDS);
    NOT_EQUAL_COMPARISON_PHRASE_STEPS =
        List.of(SNIFFS_WORDS, COMPARISON_NOT_EQUAL_WORDS, FROM_WORDS);
    GREATER_COMPARISON_PHRASE_STEPS = List.of(GROWLS_WORDS, COMPARISON_GREATER_WORDS, THAN_WORDS);
    LESS_COMPARISON_PHRASE_STEPS = List.of(SNIFFS_WORDS, COMPARISON_LESS_WORDS, THAN_WORDS);
    AT_LEAST_COMPARISON_PHRASE_STEPS = List.of(SNIFFS_WORDS, AT_WORDS, COMPARISON_AT_LEAST_WORDS);
    AT_MOST_COMPARISON_PHRASE_STEPS = List.of(SNIFFS_WORDS, AT_WORDS, COMPARISON_AT_MOST_WORDS);
    OR_ELSE_PHRASE_STEPS = List.of(OR_WORDS, ELSE_WORDS);
    TOGETHER_WITH_PHRASE_STEPS = List.of(TOGETHER_WORDS, WITH_WORDS);
    SNIFFS_INSIDE_PHRASE_STEPS = List.of(SNIFFS_WORDS, INSIDE_WORDS);

    TYPE_CHECK_KINDS = buildTypeCheckKinds();

    OBJECT_ALIASES =
        Map.ofEntries(
            Map.entry("balls", "ball"),
            Map.entry("bones", "bone"),
            Map.entry("bowls", "bowl"),
            Map.entry("frisbees", "frisbee"),
            Map.entry("leashes", "leash"),
            Map.entry("sticks", "stick"),
            Map.entry("slippers", "slippers"),
            Map.entry("tennis_balls", "tennis_ball"),
            Map.entry("squeaky_toys", "squeaky_toy"),
            Map.entry("toy_box", "toy_box"),
            Map.entry("treat", "treat"),
            Map.entry("biscuit", "biscuit"),
            Map.entry("biscuits", "biscuit"),
            Map.entry("cookie", "cookie"),
            Map.entry("cookies", "cookie"),
            Map.entry("snacks", "snack"));

    PRINT_STYLES =
        Map.ofEntries(
            Map.entry("bark", PrintStyle.BARK),
            Map.entry("barks", PrintStyle.BARK),
            Map.entry("shout", PrintStyle.GROWL),
            Map.entry("shouts", PrintStyle.GROWL),
            Map.entry("say", PrintStyle.BARK),
            Map.entry("says", PrintStyle.BARK),
            Map.entry("mention", PrintStyle.BARK),
            Map.entry("mentions", PrintStyle.BARK),
            Map.entry("tell", PrintStyle.BARK),
            Map.entry("tells", PrintStyle.BARK),
            Map.entry("announce", PrintStyle.BARK),
            Map.entry("announces", PrintStyle.BARK),
            Map.entry("declare", PrintStyle.BARK),
            Map.entry("declares", PrintStyle.BARK),
            Map.entry("command", PrintStyle.BARK),
            Map.entry("commands", PrintStyle.BARK),
            Map.entry("pant", PrintStyle.BARK),
            Map.entry("pants", PrintStyle.BARK),
            Map.entry("growl", PrintStyle.GROWL),
            Map.entry("growls", PrintStyle.GROWL),
            Map.entry("whimper", PrintStyle.WHIMPER),
            Map.entry("whimpers", PrintStyle.WHIMPER),
            Map.entry("whisper", PrintStyle.WHIMPER),
            Map.entry("whispers", PrintStyle.WHIMPER),
            Map.entry("howl", PrintStyle.HOWL),
            Map.entry("howls", PrintStyle.HOWL),
            Map.entry("woof", PrintStyle.WOOF),
            Map.entry("woofs", PrintStyle.WOOF),
            Map.entry("whine", PrintStyle.WHINE),
            Map.entry("whines", PrintStyle.WHINE),
            Map.entry("whining", PrintStyle.WHINING),
            Map.entry("mutter", PrintStyle.WHINING),
            Map.entry("mutters", PrintStyle.WHINING),
            Map.entry("yapping", PrintStyle.YAPPING),
            Map.entry("yapps", PrintStyle.YAPPING),
            Map.entry("yap", PrintStyle.YAPPING),
            Map.entry("yaps", PrintStyle.YAPPING),
            Map.entry("squeak", PrintStyle.YAPPING),
            Map.entry("squeaks", PrintStyle.YAPPING));

    registry.fillKeywordSet(KEYWORD_SET);
  }

  public static boolean isKeyword(final String word) {
    return KEYWORD_SET.contains(word.trim().toLowerCase());
  }

  // Words the dog reacts to: keywords, registered names, attributes, literals, pronouns
  public static boolean isHeard(final String word) {
    String normalized = word.trim().toLowerCase();
    return isKeyword(normalized)
        || ConfigLoader.isValidName(normalized)
        || isPronounWord(normalized)
        || isAttributeKeyword(normalized)
        || isTraitKeyword(normalized)
        || BOOLEAN_VALUES.contains(normalized)
        || NULL_VALUES.contains(normalized)
        || resolveObjectName(normalized) != null;
  }

  // Anything not heard is story glue. No separate ignore list.
  public static boolean isIgnored(final String word) {
    return !isHeard(word);
  }

  public static boolean isIfStart(final String word) {
    return IF_START_WORDS.contains(word.trim().toLowerCase());
  }

  public static boolean isThenWord(final String word) {
    return THEN_KEYWORDS.contains(word);
  }

  public static boolean isOtherwiseWord(final String word) {
    String normalized = word.trim().toLowerCase();
    return OTHERWISE_WORDS.contains(normalized) || ELSE_WORDS.contains(normalized);
  }

  public static boolean isBlockEndWord(final String word) {
    return BURY_KEYWORDS.contains(word.trim().toLowerCase());
  }

  public static boolean isNullClearWord(final String word) {
    return NULL_VALUES.contains(word);
  }

  public static boolean isForWord(final String word) {
    return FOR_WORDS.contains(word.trim().toLowerCase());
  }

  public static Optional<Boolean> parseBoolean(final String word) {
    if (!BOOLEAN_VALUES.contains(word.trim().toLowerCase())) {
      return Optional.empty();
    }
    return Optional.of(
        switch (word.trim().toLowerCase()) {
          case "true", "yes", "yeah", "yep" -> true;
          case "false", "no", "nope" -> false;
          default -> Boolean.valueOf(word);
        });
  }

  public static boolean isPronounWord(final String word) {
    return BREED_PRONOUN_WORDS.contains(word.trim().toLowerCase())
        || OBJECT_PRONOUN_WORDS.contains(word.trim().toLowerCase());
  }

  public static boolean isBreedPronounWord(final String word) {
    return BREED_PRONOUN_WORDS.contains(word.trim().toLowerCase());
  }

  public static boolean isFeminineBreedPronoun(final String word) {
    return FEMININE_BREED_PRONOUNS.contains(word.trim().toLowerCase());
  }

  public static boolean isMasculineBreedPronoun(final String word) {
    return MASCULINE_BREED_PRONOUNS.contains(word.trim().toLowerCase());
  }

  public static boolean isObjectPronounWord(final String word) {
    return OBJECT_PRONOUN_WORDS.contains(word.trim().toLowerCase());
  }

  // Registered breed/object name, or a pronoun that stands in for the last one
  public static boolean isSubjectReference(final String word) {
    String normalized = word.trim().toLowerCase();
    return ConfigLoader.isValidName(normalized) || isPronounWord(normalized);
  }

  // Breed, pronoun, or a capitalized pet name. name is "Bimba" then Bimba works as subject.
  public static boolean isDogSubjectReference(final String word, final String original) {
    String normalized = word.trim().toLowerCase();
    return ConfigLoader.isBreed(normalized)
        || isBreedPronounWord(normalized)
        || isPetNameWord(normalized, original);
  }

  public static boolean isDogSubjectReference(final String word) {
    return isDogSubjectReference(word, word);
  }

  // Capitalized unknown word. Runtime maps the normalized form after name is "…".
  public static boolean isPetNameWord(final String normalized, final String original) {
    if (ConfigLoader.isValidName(normalized)) {
      return false;
    }
    if (THE_WORDS.contains(normalized) || ARTICLE_WORDS.contains(normalized)) {
      return false;
    }
    if (isHeard(normalized)) {
      return false;
    }
    return original != null
        && !original.isEmpty()
        && Character.isUpperCase(original.codePointAt(0));
  }

  public static boolean isPetNameWord(final String word) {
    return isPetNameWord(word.trim().toLowerCase(), word);
  }

  public static boolean isArticleWord(final String word) {
    return ARTICLE_WORDS.contains(word.trim().toLowerCase());
  }

  public static Optional<TypeCheckKind> resolveTypeCheck(final String word) {
    return Optional.ofNullable(TYPE_CHECK_KINDS.get(word.trim().toLowerCase()));
  }

  // Words between assign subject and value: is, has, ...
  public static final List<String> ASSIGN_SUBJECT_GLUE_WORDS =
      List.of("is", "are", "has", "have", "was", "were", "becomes", "became");

  public static boolean isAssignSubjectGlue(final String word) {
    return ASSIGN_SUBJECT_GLUE_WORDS.contains(word.trim().toLowerCase());
  }

  public static boolean isAttributeKeyword(final String word) {
    return DogTopics.isFieldWord(word);
  }

  public static boolean isTraitKeyword(final String word) {
    return TRAIT_KEYWORDS.contains(word.trim().toLowerCase());
  }

  public static boolean isMathAddKeyword(final String word) {
    String normalized = word.trim().toLowerCase();
    return PLUS_KEYWORDS.contains(normalized) || ATTRIBUTE_INCREMENT_WORDS.contains(normalized);
  }

  public static boolean isMathSubtractKeyword(final String word) {
    String normalized = word.trim().toLowerCase();
    return MINUS_KEYWORDS.contains(normalized) || ATTRIBUTE_DECREMENT_WORDS.contains(normalized);
  }

  // Words after a print verb that mean "parse the rest as a LineExpressions value"
  public static boolean isPrintExpressionCue(final String word) {
    String normalized = word.trim().toLowerCase();
    return WITH_WORDS.contains(normalized)
        || isMathAddKeyword(normalized)
        || isMathSubtractKeyword(normalized)
        || STAR_KEYWORDS.contains(normalized)
        || SLASH_KEYWORDS.contains(normalized)
        || NOT_KEYWORDS.contains(normalized)
        || LETTERS_WORDS.contains(normalized)
        || COUNT_WORDS.contains(normalized);
  }

  public static boolean isAttributeAdjustIncrement(final String word) {
    return ATTRIBUTE_INCREMENT_WORDS.contains(word);
  }

  public static boolean isAttributeAdjustDecrement(final String word) {
    return ATTRIBUTE_DECREMENT_WORDS.contains(word);
  }

  public static boolean isFoodAdjustIncrement(final String word) {
    return FOOD_INCREMENT_WORDS.contains(word);
  }

  public static boolean isItemAdjustIncrement(final String word) {
    return ITEM_INCREMENT_WORDS.contains(word);
  }

  public static boolean isFoodAdjustDecrement(final String word) {
    return FOOD_DECREMENT_WORDS.contains(word);
  }

  public static boolean isItemAdjustDecrement(final String word) {
    return ITEM_DECREMENT_WORDS.contains(word);
  }

  // Topic word only when the source token is an attribute or alias. No default fallback.
  public static String explicitAttributeTopic(final String word) {
    return DogTopics.resolve(word);
  }

  public static String resolveAttributeTopic(final String word) {
    String explicit = explicitAttributeTopic(word);
    return explicit != null ? explicit : DogTopics.defaultAdjustTopic();
  }

  // "pinches a treat" -> food. "finds a toy" with no extra word -> items
  public static String resolveAdjustTopic(final String optionalTopicWord, final String verb) {
    if (optionalTopicWord != null && !optionalTopicWord.isBlank()) {
      String fromWord = explicitAttributeTopic(optionalTopicWord);
      if (fromWord != null) {
        return fromWord;
      }
    }
    return defaultAdjustTopicForVerb(verb);
  }

  public static String defaultAdjustTopicForVerb(final String verb) {
    if (isFoodAdjustIncrement(verb) || isFoodAdjustDecrement(verb)) {
      return DogTopics.defaultFoodAdjustTopic();
    }
    return DogTopics.defaultAdjustTopic();
  }

  // Words after a number mean age in years, "5 years", "5 years old". Two synonym lists because
  // order matters.
  public static boolean isYearsOldSuffix(final Iterable<String> followingWords) {
    boolean sawYears = false;
    for (String raw : followingWords) {
      String word = raw.trim().toLowerCase();
      if (isIgnored(word)) {
        continue;
      }
      if (YEARS_WORDS.contains(word)) {
        sawYears = true;
        continue;
      }
      if (OLD_WORDS.contains(word) && sawYears) {
        return true;
      }
      break;
    }
    return sawYears;
  }

  public static boolean isListSeparator(final Token token) {
    if (token.is(TokenType.COMMA)) {
      return true;
    }
    return token.is(TokenType.IDENTIFIER)
        && LIST_AND_WORDS.contains(token.value().trim().toLowerCase());
  }

  public static String resolveObjectName(final String word) {
    String normalized = word.trim().toLowerCase();
    if (ConfigLoader.typeOf(normalized).orElse(null) == VariableType.OBJECT) {
      return normalized;
    }
    String singular = OBJECT_ALIASES.get(normalized);
    if (singular != null && ConfigLoader.typeOf(singular).orElse(null) == VariableType.OBJECT) {
      return singular;
    }
    return null;
  }
}
