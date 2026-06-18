package dev.klomptech.jbark.parser;

import java.util.ArrayList;
import java.util.List;

import dev.klomptech.jbark.BarkOptions;
import dev.klomptech.jbark.errors.BarkError;
import dev.klomptech.jbark.lexer.Token;
import dev.klomptech.jbark.lexer.TokenType;
import dev.klomptech.jbark.parser.assign.AssignParser;
import dev.klomptech.jbark.parser.collection.PileActionParser;
import dev.klomptech.jbark.parser.collection.PileParser;
import dev.klomptech.jbark.parser.collection.StashParser;
import dev.klomptech.jbark.parser.collection.TakeParser;
import dev.klomptech.jbark.parser.controlflow.ForEachParser;
import dev.klomptech.jbark.parser.controlflow.FunctionParser;
import dev.klomptech.jbark.parser.controlflow.IfParser;
import dev.klomptech.jbark.parser.controlflow.LoopControlParser;
import dev.klomptech.jbark.parser.controlflow.UntilParser;
import dev.klomptech.jbark.parser.controlflow.WhileParser;
import dev.klomptech.jbark.parser.input.InputParser;
import dev.klomptech.jbark.parser.printing.PrintParser;
import dev.klomptech.jbark.parser.share.ShareParser;

public class Parser {

  private final List<Token> tokens;
  private final BarkOptions options;
  private int current = 0;

  private final PrintParser printParser = new PrintParser(this);
  private final IfParser ifParser = new IfParser(this);
  private final WhileParser whileParser = new WhileParser(this);
  private final UntilParser untilParser = new UntilParser(this);
  private final ForEachParser forEachParser = new ForEachParser(this);
  private final LoopControlParser loopControlParser = new LoopControlParser(this);
  private final FunctionParser functionParser = new FunctionParser(this);
  private final InputParser inputParser = new InputParser(this);
  private final AssignParser assignParser = new AssignParser(this);
  private final ShareParser shareParser = new ShareParser(this);
  private final StashParser stashParser = new StashParser(this);
  private final PileParser pileParser = new PileParser(this);
  private final PileActionParser pileActionParser = new PileActionParser(this);
  private final TakeParser takeParser = new TakeParser(this);

  public Parser(final List<Token> tokens) {
    this(tokens, BarkOptions.defaults());
  }

  public Parser(final List<Token> tokens, final BarkOptions options) {
    this.tokens = tokens;
    this.options = options == null ? BarkOptions.defaults() : options;
  }

  public AstNode.Program parse() throws BarkError {
    List<AstNode> statements = new ArrayList<>();
    while (!isAtEnd()) {
      if (isNewline()) {
        advance();
        continue;
      }
      int line = peek().line();
      AstNode node = parseLine();
      if (node instanceof AstNode.LineGroup group) {
        statements.addAll(group.statements());
      } else if (node != null) {
        statements.add(node);
      } else {
        consumeUntilLineEnd();
        warnIfUnrecognizedLine(line);
      }
    }
    return new AstNode.Program(statements);
  }

  // Main processing methods
  private AstNode parseLine() throws BarkError {
    Token token = peek();
    return switch (token.type()) {
      case EOF, NEWLINE -> null;
      case NUMBER -> parseNumberLine();
      case IDENTIFIER -> parseWordLine();
      default -> null;
    };
  }

  private AstNode parseNumberLine() throws BarkError {
    return assignParser.parseLeadingObjectLine();
  }

  private AstNode parseWordLine() throws BarkError {
    // Example: "when she has 2 toys then"
    if (lineHasWord(Keywords.IF_START_WORDS)) {
      return ifParser.parseIfLine();
    }
    // Example: "while she sniffs less toys than 8 then"
    if (lineHasWord(Keywords.WHILE_KEYWORDS)) {
      return whileParser.parseWhileLine();
    }
    // Example: "until she has 0 toys then"
    if (lineHasWord(Keywords.UNTIL_KEYWORDS)) {
      return untilParser.parseUntilLine();
    }
    // Example: "for each treat from her cookie jar then"
    if (currentLineStartsWithAny(Keywords.FOR_WORDS)) {
      return forEachParser.parseForEachLine();
    }
    // Example: "heel" stops a while/until/for-each loop early (break)
    AstNode loopBreak = loopControlParser.parseBreakLine();
    if (loopBreak != null) {
      return loopBreak;
    }
    // Example: "again" ends this lap and starts the next loop round (continue)
    AstNode loopContinue = loopControlParser.parseContinueLine();
    if (loopContinue != null) {
      return loopContinue;
    }
    // Example: "add expects a b returns a" ... bury
    AstNode trick = functionParser.parseTrickLine();
    if (trick != null) {
      return trick;
    }
    // Example: "escape" stops the program
    AstNode exit = inputParser.parseExitLine();
    if (exit != null) {
      return exit;
    }
    // Example: "sleep" or "wait 2 seconds"
    AstNode wait = inputParser.parseWaitLine();
    if (wait != null) {
      return wait;
    }
    // Example: "the labrador listens"
    AstNode listen = inputParser.parseListenLine();
    if (listen != null) {
      return listen;
    }
    // Example: "she passes a toy to the beagle" (one count, one recipient)
    AstNode pass = shareParser.parsePassLine();
    if (pass != null) {
      return pass;
    }
    // Example: "she shares a toy with the beagle" (many counts, split across many recipients)
    AstNode share = shareParser.parseShareLine();
    if (share != null) {
      return share;
    }
    // Example: "my labrador holds 3 toys"
    AstNode stash = stashParser.parseStashLine();
    if (stash != null) {
      return stash;
    }
    // Example: "she takes the top item from the laundry basket"
    AstNode pilePop = pileActionParser.parseGrabTopLine();
    if (pilePop != null) {
      return pilePop;
    }
    // Example: "she puts "slipper" into the laundry basket"
    AstNode pilePush = pileActionParser.parsePushIntoLine();
    if (pilePush != null) {
      return pilePush;
    }
    // Example: "she wants the first biscuit from her cookie jar"
    AstNode take = takeParser.parseTakeLine();
    if (take != null) {
      return take;
    }
    // Example: "the laundry basket holds "shirt", "sock""
    AstNode pile = pileParser.parsePileLine();
    if (pile != null) {
      return pile;
    }
    // Example: "she misplaces a toy", "she gains 2 treats"
    AstNode adjust = assignParser.parseAdjustLine();
    if (adjust != null) {
      return adjust;
    }
    AstNode hasAssign = assignParser.parseHasLine();
    if (hasAssign != null) {
      return hasAssign;
    }
    // Example: "ball 5"
    AstNode objectCount = assignParser.parseObjectCountLine();
    if (objectCount != null) {
      return objectCount;
    }
    // Example: "labrador name is Bimba"
    AstNode nameAssign = assignParser.parseNameLine();
    if (nameAssign != null) {
      return nameAssign;
    }
    // Example: "bark my labrador is loud" reads the trait and prints true/false (does not set it)
    if (lineHasWord(Keywords.PRINT_KEYWORDS)) {
      return printParser.parsePrintLine();
    }
    // Example: "my labrador is loud" sets the trait (no print verb on the line)
    AstNode trait = assignParser.parseTraitLine();
    if (trait != null) {
      return trait;
    }
    // Example: "He is 5 years old"
    AstNode yearsOld = assignParser.parseYearsOldLine();
    if (yearsOld != null) {
      return yearsOld;
    }
    // Example: "She is 2"
    AstNode isAge = assignParser.parseIsAgeLine();
    if (isAge != null) {
      return isAge;
    }
    // Example: "labrador 3" on its own
    AstNode bareAge = assignParser.parseBareAgeLine();
    if (bareAge != null) {
      return bareAge;
    }
    // Example: "memory is count of the items in the cookie jar"
    AstNode storyConstant = assignParser.parseStoryConstantAssignLine();
    if (storyConstant != null) {
      return storyConstant;
    }
    // Example: "my labrador treats is count of the items in the cookie jar"
    AstNode attributeExpr = assignParser.parseAttributeExpressionAssignLine();
    if (attributeExpr != null) {
      return attributeExpr;
    }
    // Example: "So, my wife has a labrador" registers the breed on the line
    AstNode register = assignParser.parseRegisterLine();
    if (register != null) {
      return register;
    }
    return null;
  }

  // Any identifier on this line matches one of the words after normalise
  public boolean lineHasWord(final List<String> words) {
    for (int offset = 0; !isAtEndOrLineEndOffset(offset); offset++) {
      Token token = peekAt(offset);
      if (token.is(TokenType.IDENTIFIER) && words.contains(normalise(token.value()))) {
        return true;
      }
    }
    return false;
  }

  // Warn if the line produced no statement
  private void warnIfUnrecognizedLine(final int line) {
    if (options.strict()) {
      System.err.println("Line " + line + ": the dogs didn't catch that line (story-only words?).");
    }
  }

  // Helper methods
  public Token peek() {
    return tokens.get(current);
  }

  public Token peekAt(final int offset) {
    int index = current + offset;
    if (index >= tokens.size()) {
      return tokens.get(tokens.size() - 1);
    }
    return tokens.get(index);
  }

  public Token advance() {
    return tokens.get(current++);
  }

  public void advanceBy(final int count) {
    for (int i = 0; i < count; i++) {
      advance();
    }
  }

  public boolean isAtEnd() {
    return peek().is(TokenType.EOF);
  }

  private boolean isNewline() {
    return peek().is(TokenType.NEWLINE);
  }

  public boolean isAtEndOrLineEndOffset(final int offset) {
    Token token = peekAt(offset);
    return token.is(TokenType.NEWLINE) || token.is(TokenType.EOF);
  }

  // How many tokens from peekAt(startOffsetFromCursor) until newline (newline not included)
  public int countTokensAhead(final int startOffsetFromCursor) {
    int count = 0;
    while (!isAtEndOrLineEndOffset(startOffsetFromCursor + count)) {
      count++;
    }
    return count;
  }

  public void skipNewlines() {
    while (!isAtEnd() && isNewline()) {
      advance();
    }
  }

  // First identifier on the current line (after normalise) equals word
  public boolean currentLineStartsWith(final String word) {
    Token token = peek();
    return token.is(TokenType.IDENTIFIER) && word.equals(normalise(token.value()));
  }

  public boolean currentLineStartsWithOtherwise() {
    return currentLineStartsWithAny(Keywords.OTHERWISE_WORDS)
        || currentLineStartsWithAny(Keywords.ELSE_WORDS);
  }

  // Elif branch: line starts with otherwise and later has when/if on the same line
  // Example: "otherwise when she has 1 toy then". A "otherwise" alone is final else, not this.
  public boolean currentLineIsOtherwiseWhen() {
    if (!currentLineStartsWithOtherwise()) {
      return false;
    }
    for (int offset = 1; !isAtEndOrLineEndOffset(offset); offset++) {
      Token token = peekAt(offset);
      if (token.is(TokenType.IDENTIFIER) && Keywords.isIfStart(normalise(token.value()))) {
        return true;
      }
    }
    return false;
  }

  public boolean currentLineIsBury() {
    return currentLineStartsWithAny(Keywords.BURY_KEYWORDS);
  }

  // Stop lines while reading a multiline then-body
  public boolean currentLineIsMultilineIfStop() {
    return currentLineStartsWithOtherwise() || currentLineIsBury();
  }

  public boolean currentLineStartsWithAny(final List<String> words) {
    Token token = peek();
    if (!token.is(TokenType.IDENTIFIER)) {
      return false;
    }
    return words.contains(normalise(token.value()));
  }

  // Read one line the same way the main parse loop does (if, print, take, and so on)
  public AstNode parseStatement() throws BarkError {
    return parseLine();
  }

  // LineGroup only happens when one line has two actions (example: whimper print + take from jar)
  // This method splits that into two separate steps in the list
  public void parseStatementInto(final List<AstNode> steps) throws BarkError {
    AstNode stmt = parseStatement();
    if (stmt instanceof AstNode.LineGroup group) {
      steps.addAll(group.statements());
    } else if (stmt != null) {
      steps.add(stmt);
    } else {
      consumeUntilLineEnd();
    }
  }

  // Eat a bury (or alternatives) line and move past its newline
  public void consumeBuryLine() {
    if (currentLineIsBury()) {
      consumeUntilLineEnd();
    }
    skipNewlines();
  }

  public void consumeUntilLineEnd() {
    while (!isAtEnd()) {
      advance();
      if (isNewline()) {
        return;
      }
    }
  }

  public String normalise(final String value) {
    return value.trim().toLowerCase();
  }

  // Skip identifier tokens the dogs do not hear (story filler).
  public int skipIgnoredIdentifiers(final int from, final int to) {
    int pos = from;
    while (pos < to) {
      Token token = peekAt(pos);
      if (!token.is(TokenType.IDENTIFIER)) {
        break;
      }
      String word = normalise(token.value());
      if (!Keywords.isIgnored(word)) {
        break;
      }
      pos++;
    }
    return pos;
  }

  public PrintParser getPrintParser() {
    return printParser;
  }

  public int cursorIndex() {
    return current;
  }

  public void setCurrent(final int index) {
    current = index;
  }

  public List<Token> allTokens() {
    return tokens;
  }

  // Token index where the current line ends (newline or EOF)
  public int lineEndIndex() {
    for (int i = current; i < tokens.size(); i++) {
      if (tokens.get(i).is(TokenType.NEWLINE)) {
        return i;
      }
    }
    return tokens.size();
  }
}
