package dev.klomptech.jbark.parser.keywords;

import java.util.List;

public final class LogicKeywordGroups {

  public final List<String> logicalAndWords;
  public final List<String> conditionAndWords;
  public final List<String> logicalOrWords;
  public final List<String> neitherWords;
  public final List<String> norWords;
  public final List<String> comparisonEqualWords;
  public final List<String> comparisonNotEqualWords;
  public final List<String> comparisonGreaterWords;
  public final List<String> comparisonLessWords;
  public final List<String> comparisonAtLeastWords;
  public final List<String> comparisonAtMostWords;
  public final List<String> notWords;
  public final List<String> countsWords;
  public final List<String> lettersWords;
  public final List<String> insideWords;
  public final List<String> plusWords;
  public final List<String> minusWords;
  public final List<String> starWords;
  public final List<String> slashWords;
  public final List<String> booleanValues;
  public final List<String> nullValues;

  public LogicKeywordGroups(final KeywordRegistry registry) {
    logicalAndWords = registry.phraseWords("both", "and", "also");
    conditionAndWords = registry.phraseWords("and");
    logicalOrWords = registry.phraseWords("either");
    neitherWords = registry.phraseWords("neither");
    norWords = registry.phraseWords("nor");
    comparisonEqualWords = registry.phraseWords("matches", "equals", "matching");
    comparisonNotEqualWords = registry.phraseWords("different", "unlike", "differs");
    comparisonGreaterWords =
        registry.phraseWords("louder", "bigger", "more", "over", "above", "exceeds");
    comparisonLessWords =
        registry.phraseWords("less", "fewer", "smaller", "under", "below", "quieter");
    comparisonAtLeastWords = registry.phraseWords("least");
    comparisonAtMostWords = registry.phraseWords("most");
    notWords = registry.heardWords("not", "never");
    countsWords = registry.heardWords("counts");
    lettersWords = registry.heardWords("letters", "length", "characters");
    insideWords = registry.heardWords("inside", "within", "through");
    plusWords = registry.heardWords("plus", "add");
    minusWords = registry.heardWords("minus", "spends");
    starWords =
        registry.phraseWords(
            "times",
            "multiply",
            "multiplies",
            "multiplied",
            "birthed",
            "births",
            "whelps",
            "whelped",
            "double",
            "doubles",
            "duplicates",
            "clones");
    slashWords = registry.heardWords("broke", "divide", "divides", "halves");
    booleanValues = registry.heardWords("true", "false", "yes", "no", "yeah", "nope", "yep");
    nullValues =
        registry.heardWords(
            "null",
            "nothing",
            "notreat",
            "emptybowl",
            "no-bone-here",
            "nobodyhome",
            "hungry",
            "starving",
            "empty",
            "barebowl",
            "emptykennel",
            "allgone",
            "vanished",
            "missing",
            "nowhere",
            "treatless",
            "boneless",
            "squeakless",
            "nofetch");
  }
}
