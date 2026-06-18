package dev.klomptech.jbark.parser.keywords;

import java.util.ArrayList;
import java.util.List;

public final class AttributeKeywordGroups {

  public final List<String> traitWords;
  public final List<String> incrementWords;
  public final List<String> foodIncrementWords;
  public final List<String> itemIncrementWords;
  public final List<String> decrementWords;
  public final List<String> foodDecrementWords;
  public final List<String> itemDecrementWords;
  public final List<String> typeNumberWords;
  public final List<String> typeWordsWords;
  public final List<String> typeNothingWords;
  public final List<String> sniffWords;
  public final List<String> growlWords;
  public final List<String> sameWords;
  public final List<String> thanWords;
  public final List<String> atWords;
  public final List<String> listAndWords;
  public final List<String> theWords;
  public final List<String> ofWords;
  public final List<String> itemsWords;
  public final List<String> inWords;
  public final List<String> asWords;
  public final List<String> byWords;
  public final List<String> howWords;
  public final List<String> manyWords;
  public final List<String> yearsWords;
  public final List<String> oldWords;
  public final List<String> togetherWords;
  public final List<String> orWords;

  public AttributeKeywordGroups(final KeywordRegistry registry) {
    traitWords =
        registry.heardWords("loud", "greedy", "lazy", "wet", "fetchy", "chaser", "playful");
    foodIncrementWords =
        registry.heardWords("feed", "feeds", "refills", "steals", "pinch", "pinches");
    itemIncrementWords =
        registry.heardWords(
            "grow",
            "grows",
            "grew",
            "gets",
            "receives",
            "earns",
            "gains",
            "collects",
            "snags",
            "nabs",
            "inherits",
            "scores",
            "find",
            "finds",
            "fetch",
            "fetches",
            "discovers",
            "unearths");
    incrementWords = concat(foodIncrementWords, itemIncrementWords);
    foodDecrementWords =
        registry.heardWords(
            "gulp", "gulps", "eat", "eats", "ate", "devour", "devours", "chews", "munches",
            "scarfs", "wolfs", "nibbles");
    itemDecrementWords =
        registry.heardWords(
            "buried",
            "lost",
            "misplaces",
            "breaks",
            "destroys",
            "hides",
            "buries",
            "forgets",
            "tosses",
            "abandons");
    decrementWords = concat(foodDecrementWords, itemDecrementWords);
    typeNumberWords = registry.heardWords("number", "digit", "digits");
    typeWordsWords = registry.heardWords("words", "text");
    typeNothingWords = registry.heardWords("nothing");
    sniffWords = registry.heardWords("sniffs", "smells", "scents", "checks", "peeks");
    growlWords = registry.heardWords("growls", "roars", "bellows");
    sameWords = registry.phraseWords("same");
    thanWords = registry.phraseWords("than");
    atWords = registry.phraseWords("at");
    listAndWords = registry.phraseWords("and");
    theWords = registry.phraseWords("the");
    ofWords = registry.phraseWords("of");
    itemsWords = registry.phraseWords("items");
    inWords = registry.phraseWords("in", "into");
    asWords = registry.phraseWords("as");
    byWords = registry.phraseWords("by");
    howWords = registry.phraseWords("how");
    manyWords = registry.phraseWords("many");
    yearsWords = registry.phraseWords("years", "year");
    oldWords = registry.phraseWords("old");
    togetherWords = registry.phraseWords("together");
    orWords = registry.phraseWords("or");
  }

  private static List<String> concat(final List<String> first, final List<String> second) {
    List<String> merged = new ArrayList<>(first);
    merged.addAll(second);
    return List.copyOf(merged);
  }
}
