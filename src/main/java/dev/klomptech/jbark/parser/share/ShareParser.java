package dev.klomptech.jbark.parser.share;

import dev.klomptech.jbark.config.ConfigLoader;
import dev.klomptech.jbark.errors.BarkError;
import dev.klomptech.jbark.lexer.Token;
import dev.klomptech.jbark.lexer.TokenType;
import dev.klomptech.jbark.parser.AstNode;
import dev.klomptech.jbark.parser.Keywords;
import dev.klomptech.jbark.parser.Parser;
import java.util.ArrayList;
import java.util.List;

public class ShareParser {

  private final Parser parser;

  public ShareParser(final Parser parser) {
    this.parser = parser;
  }

  // Pass one toy from dog to a single other dog (like: she passes a toy to the beagle)
  public AstNode parsePassLine() throws BarkError {
    int line = parser.peek().line();
    int passAt = findWordAfter(-1, Keywords.PASSES_STATEMENT_KEYWORDS);
    if (passAt < 0) {
      return null;
    }
    int toAt = findWordAfter(passAt, Keywords.TO_WORDS);
    if (toAt < 0) {
      return null;
    }
    String giver = findDogBefore(passAt);
    if (giver == null) {
      throw new BarkError(line, "Only dogs can pass toys, name who is giving first");
    }
    List<String> recipients = findDogsAfter(toAt + 1);
    if (recipients.isEmpty()) {
      throw new BarkError(line, "Passing needs one friend, give the name of who gets the item");
    }
    if (recipients.size() > 1) {
      throw new BarkError(line, "One pass, one friend. Use shares when more dogs get a slice");
    }
    parser.consumeUntilLineEnd();
    return new AstNode.Pass(giver, recipients.get(0), line);
  }

  // Split a stash with other dogs (e.g. labrador shares her cookie jar with the beagle)
  public AstNode parseShareLine() throws BarkError {
    int line = parser.peek().line();
    int sharesAt = findWordAfter(-1, Keywords.SHARES_STATEMENT_KEYWORDS);
    if (sharesAt < 0) {
      return null;
    }
    int withAt = findWordAfter(sharesAt, Keywords.WITH_WORDS);
    if (withAt < 0) {
      return null;
    }
    String giver = findDogBefore(sharesAt);
    if (giver == null) {
      throw new BarkError(line, "Only dogs can share. Name who is giving first");
    }
    String stash = null;
    if (withAt > sharesAt + 1) {
      int lineStart = parser.cursorIndex();
      stash =
          ConfigLoader.resolveCollectionFromTokens(
              parser.allTokens(), lineStart + sharesAt + 1, lineStart + withAt);
    }
    List<String> recipients = findDogsAfter(withAt + 1);
    if (recipients.isEmpty()) {
      throw new BarkError(
          line, "The dogs need a friend to share with. Name another dog after with");
    }
    parser.consumeUntilLineEnd();
    return new AstNode.Share(giver, stash, recipients, line);
  }

  private int findWordAfter(final int afterOffset, final List<String> words) {
    int start = afterOffset + 1;
    for (int offset = start; !parser.isAtEndOrLineEndOffset(offset); offset++) {
      Token token = parser.peekAt(offset);
      if (token.is(TokenType.IDENTIFIER) && words.contains(parser.normalise(token.value()))) {
        return offset;
      }
    }
    return -1;
  }

  private String findDogBefore(final int beforeOffset) {
    for (int offset = 0; offset < beforeOffset; offset++) {
      Token token = parser.peekAt(offset);
      if (!token.is(TokenType.IDENTIFIER)) {
        continue;
      }
      String word = parser.normalise(token.value());
      if (Keywords.isDogSubjectReference(word, token.value())) {
        return word;
      }
    }
    return null;
  }

  private List<String> findDogsAfter(final int startOffset) throws BarkError {
    int line = parser.peek().line();
    int lineStart = parser.cursorIndex();
    int lineEnd = parser.lineEndIndex();
    List<String> dogs = new ArrayList<>();
    int offset = startOffset;
    while (offset < lineEnd - lineStart) {
      int abs = lineStart + offset;
      Token token = parser.allTokens().get(abs);
      if (token.is(TokenType.COMMA)) {
        offset++;
        continue;
      }
      if (!token.is(TokenType.IDENTIFIER)) {
        offset++;
        continue;
      }
      String word = parser.normalise(token.value());
      if (Keywords.THE_WORDS.contains(word)
          || Keywords.ARTICLE_WORDS.contains(word)
          || Keywords.LIST_AND_WORDS.contains(word)
          || Keywords.WITH_WORDS.contains(word)
          || Keywords.SHARES_STATEMENT_KEYWORDS.contains(word)) {
        offset++;
        continue;
      }
      String breed = ConfigLoader.resolveNameFromTokens(parser.allTokens(), abs, lineEnd);
      if (breed != null
          && (ConfigLoader.isBreed(breed)
              || Keywords.isBreedPronounWord(breed)
              || Keywords.isPetNameWord(breed, token.value()))) {
        dogs.add(breed);
        offset = advancePastNamePhrase(offset, breed);
        continue;
      }
      if (ConfigLoader.isBreed(word)
          || Keywords.isBreedPronounWord(word)
          || Keywords.isPetNameWord(word, token.value())) {
        dogs.add(word);
        offset++;
        continue;
      }
      if (!Keywords.isIgnored(word)) {
        throw new BarkError(line, "\"" + word + "\" is not a dog the pack can share with.");
      }
      offset++;
    }
    return dogs;
  }

  private int advancePastNamePhrase(final int offsetFromLineStart, final String name) {
    int lineStart = parser.cursorIndex();
    int lineEnd = parser.lineEndIndex();
    List<String> words = new ArrayList<>();
    List<Integer> positions = new ArrayList<>();
    for (int abs = lineStart + offsetFromLineStart; abs < lineEnd; abs++) {
      Token token = parser.allTokens().get(abs);
      if (token.is(TokenType.IDENTIFIER)) {
        words.add(parser.normalise(token.value()));
        positions.add(abs);
      }
    }
    for (int length = Math.min(4, words.size()); length >= 1; length--) {
      for (int start = 0; start <= words.size() - length; start++) {
        String phrase = String.join(" ", words.subList(start, start + length));
        if (name.equals(ConfigLoader.resolveNamePhrase(phrase))) {
          return positions.get(start + length - 1) + 1 - lineStart;
        }
      }
    }
    return offsetFromLineStart + 1;
  }
}
