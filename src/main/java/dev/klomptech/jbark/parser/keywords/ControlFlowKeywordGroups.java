package dev.klomptech.jbark.parser.keywords;

import java.util.List;

public final class ControlFlowKeywordGroups {

  public final List<String> ifStartWords;
  public final List<String> whileWords;
  public final List<String> untilWords;
  public final List<String> forWords;
  public final List<String> eachWords;
  public final List<String> fromWords;
  public final List<String> thenWords;
  public final List<String> otherwiseWords;
  public final List<String> elseWords;
  public final List<String> buryWords;
  public final List<String> breakWords;
  public final List<String> continueWords;
  public final List<String> expectsWords;
  public final List<String> trickReturnsWords;

  public ControlFlowKeywordGroups(final KeywordRegistry registry) {
    ifStartWords = registry.phraseWords("if", "when", "whenever", "should");
    whileWords = registry.phraseWords("while", "during");
    untilWords = registry.phraseWords("until", "till");
    forWords = registry.phraseWords("for");
    eachWords = registry.phraseWords("each", "every");
    fromWords = registry.phraseWords("from");
    thenWords = registry.phraseWords("then", "do");
    otherwiseWords = registry.phraseWords("otherwise", "instead");
    elseWords = registry.phraseWords("else");
    buryWords =
        registry.phraseWords(
            "bury",
            "end",
            "goodnight",
            "enough",
            "bedtime",
            "lightsout",
            "hush",
            "done",
            "finished");
    breakWords = registry.phraseWords("heel", "stop", "stay", "halt");
    continueWords = registry.phraseWords("again", "repeat", "resume", "onward");
    expectsWords = registry.phraseWords("expects", "expect");
    trickReturnsWords = registry.phraseWords("returns", "return");
  }
}
