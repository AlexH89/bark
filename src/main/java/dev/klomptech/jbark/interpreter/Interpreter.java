package dev.klomptech.jbark.interpreter;

import dev.klomptech.jbark.cli.DogArt;
import dev.klomptech.jbark.config.ConfigLoader;
import dev.klomptech.jbark.config.DogTopics;
import dev.klomptech.jbark.config.NameHints;
import dev.klomptech.jbark.config.TraitLoader;
import dev.klomptech.jbark.config.VariableType;
import dev.klomptech.jbark.errors.BarkError;
import dev.klomptech.jbark.parser.AstNode;
import dev.klomptech.jbark.parser.Keywords;
import dev.klomptech.jbark.parser.ParseExpression;
import dev.klomptech.jbark.parser.collection.StashSpots;
import dev.klomptech.jbark.parser.expression.ComparisonOp;
import dev.klomptech.jbark.print.PrintStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

// Runs a parsed .woof program.
public final class Interpreter {

  private final Map<String, BarkValue> values = new HashMap<>();
  private final Map<String, Dog> dogs = new HashMap<>();
  private final Map<String, Prop> props = new HashMap<>();
  // Named lists from stashes.txt
  private final Map<String, List<BarkValue>> stashes = new HashMap<>();
  // Stacks from piles.txt
  private final Map<String, List<BarkValue>> piles = new HashMap<>();
  // Tricks defined with "name takes … gives back … bury"
  private final Map<String, AstNode.FunctionDef> tricks = new HashMap<>();
  private final Map<ComparisonOp, Comparator> comparators = Comparators.create();
  // Last dog and last stash/pile/object. Pronouns resolve separately.
  private String lastBreed;
  private String lastFeminineBreed;
  private String lastMasculineBreed;
  private String lastObject;
  private final Map<String, Set<String>> breedTraits = TraitLoader.defaultTraits();
  private final Map<String, Integer> greedyUses = new HashMap<>();
  // name is "Bimba" then Bimba maps to the breed key
  private final Map<String, String> petNames = new HashMap<>();
  // The program-wide story variables (ConfigLoader.MEMORY, ConfigLoader.JOURNAL)
  private short memory;
  private String journal = "";
  private String lastPrintedText;

  private static final int GREEDY_THRESHOLD = TraitLoader.GREEDY_FEEDS_BEFORE_LOSS;

  public void run(final AstNode.Program program) throws BarkError {
    for (AstNode statement : program.statements()) {
      runStatement(statement);
    }
  }

  private void runSteps(final List<AstNode> steps) throws BarkError {
    for (AstNode step : steps) {
      runStatement(step);
    }
  }

  private void runStatement(final AstNode node) throws BarkError {
    switch (node) {
      case AstNode.Print n -> runPrint(n);
      case AstNode.Listen n -> runListen(n);
      case AstNode.Wait n -> runWait(n);
      case AstNode.Assign n -> runAssign(n);
      case AstNode.SetAttribute n -> runSetAttribute(n);
      case AstNode.IfChain n -> runIfChain(n);
      case AstNode.WhileLoop n -> runWhile(n);
      case AstNode.UntilLoop n -> runUntil(n);
      case AstNode.ForEach n -> runForEach(n);
      case AstNode.FunctionDef n -> tricks.put(ConfigLoader.normalise(n.name()), n);
      case AstNode.Break _ -> throw new LoopSignal(LoopSignal.Kind.BREAK);
      case AstNode.Continue _ -> throw new LoopSignal(LoopSignal.Kind.CONTINUE);
      case AstNode.StashInit n -> runStashInit(n);
      case AstNode.StashAppend n -> runStashAppend(n);
      case AstNode.StashSet n -> runStashSet(n);
      case AstNode.StashRemove n -> runStashRemove(n);
      case AstNode.StashClear n -> runStashClear(n);
      case AstNode.PileInit n -> runPileInit(n);
      case AstNode.PilePush n -> runPilePush(n);
      case AstNode.PilePop n -> runPilePop(n);
      case AstNode.PileClear n -> runPileClear(n);
      case AstNode.Share n -> runShare(n);
      case AstNode.Pass n -> runPass(n);
      case AstNode.SetTrait n -> runSetTrait(n);
      case AstNode.TakeFromStash n -> runTakeFromStash(n);
      case AstNode.LineGroup n -> runSteps(n.statements());
      case AstNode.Exit _ -> exitProgram();
      case AstNode.Program n ->
          throw new BarkError(n.line(), "Nested programs? The pack only runs one story at a time.");
    }
  }

  private void runPrint(final AstNode.Print print) throws BarkError {
    if (print.pawPrint()) {
      DogArt.printPaws();
      return;
    }
    PrintStyle style = print.style();
    String speaker = null;
    if (print.dogSpeaker() != null) {
      speaker = resolveSubjectForMood(print.dogSpeaker());
      if (speaker != null) {
        if (style == PrintStyle.BARK && hasTrait(speaker, "loud")) {
          style = PrintStyle.GROWL;
        } else if (style == PrintStyle.BARK) {
          double items = asNumber(lookupField(speaker, DogTopics.ITEMS, 0), 0);
          if (items <= 0) {
            style = PrintStyle.WHINE;
          }
        }
      }
    }
    boolean bare = print.values().stream().allMatch(ParseExpression.Empty.class::isInstance);
    if (bare) {
      emitPrint(style, PrintStyles.bareText(style), speaker);
      return;
    }
    List<String> parts = new ArrayList<>();
    for (ParseExpression value : print.values()) {
      parts.add(BarkValue.describe(eval(value, print.line())));
    }
    emitPrint(style, String.join(" ", parts), speaker);
  }

  private void emitPrint(final PrintStyle style, final String text, final String speaker) {
    PrintStyle resolvedStyle = style != null ? style : PrintStyle.BARK;
    String output = text != null ? text : "";
    if (speaker != null && hasTrait(speaker, "playful")) {
      output = new StringBuilder(output).reverse().toString();
    }
    String formatted;
    if ((resolvedStyle == PrintStyle.WHINE || resolvedStyle == PrintStyle.WHIMPER)
        && lastBreed != null
        && hasTrait(lastBreed, "wet")) {
      formatted =
          (PrintStyles.format(resolvedStyle, output)
                  + " "
                  + PrintStyles.format(resolvedStyle, output))
              .trim();
    } else {
      formatted = PrintStyles.format(resolvedStyle, output);
    }
    if (lastBreed != null
        && hasTrait(lastBreed, "fetchy")
        && Objects.equals(formatted, lastPrintedText)) {
      System.out.println("*tail THUMP THUMP, same stick, still the best stick*");
    }
    lastPrintedText = formatted;
    System.out.println(formatted);
  }

  private void runAssign(final AstNode.Assign assign) throws BarkError {
    String key = ConfigLoader.normalise(assign.variable());
    if (ConfigLoader.isStoryNumberConstant(key)) {
      memory = toMemory(eval(assign.value(), assign.line()), assign.line());
      return;
    }
    if (ConfigLoader.isStoryTextConstant(key)) {
      journal = toJournal(eval(assign.value(), assign.line()), assign.line());
      return;
    }
    noteSubject(assign.variable());
    BarkValue value = eval(assign.value(), assign.line());
    if (ConfigLoader.typeOf(key).orElse(null) == VariableType.OBJECT) {
      props.put(key, new Prop(value));
    } else {
      values.put(key, value);
    }
  }

  private void runSetAttribute(final AstNode.SetAttribute node) throws BarkError {
    String who = resolveSubject(node.subject(), node.line());
    ConfigLoader.requireBreed(who, node.line());
    bindPronounSubject(node.subject(), who);
    String field = DogTopics.resolve(node.topic());
    if (field == null) {
      throw new BarkError(
          node.line(),
          "The dogs don't track \"" + node.topic() + "\". Only " + DogTopics.CANONICAL + ".");
    }
    dog(who).set(field, eval(node.value(), node.line()), node.line());
    if (DogTopics.NAME.equals(field)) {
      registerPetName(who, dog(who).get(field, node.line()));
    }
    if (node.value() instanceof ParseExpression.Binary binary) {
      syncInventoryAdjust(who, node.topic(), binary);
      if (DogTopics.isFoodField(field)) {
        tickGreedy(who);
      }
    }
  }

  private void syncInventoryAdjust(
      final String who, final String topicWord, final ParseExpression.Binary binary)
      throws BarkError {
    String object = inventoryObjectFromTopic(topicWord);
    if (object == null) {
      return;
    }
    Dog dog = dog(who);
    if (binary.op() == ParseExpression.BinaryOp.PLUS) {
      dog.addHeldObject(object);
    } else if (binary.op() == ParseExpression.BinaryOp.MINUS) {
      dog.removeHeldObject(object);
    }
  }

  private static String inventoryObjectFromTopic(final String topicWord) {
    String object = Keywords.resolveObjectName(topicWord);
    if (object != null) {
      return object;
    }
    String normalized = ConfigLoader.normalise(topicWord);
    if ("toy".equals(normalized) || "toys".equals(normalized)) {
      return "toy";
    }
    return null;
  }

  private void runSetTrait(final AstNode.SetTrait node) throws BarkError {
    String who = resolveSubject(node.subject(), node.line());
    ConfigLoader.requireBreed(who, node.line());
    String trait = ConfigLoader.normalise(node.trait());
    if (node.enabled()) {
      breedTraits.computeIfAbsent(who, _ -> new HashSet<>()).add(trait);
    } else {
      Set<String> traits = breedTraits.get(who);
      if (traits != null) {
        traits.remove(trait);
      }
    }
  }

  private void runShare(final AstNode.Share share) throws BarkError {
    String giver = resolveSubject(share.giver(), share.line());
    ConfigLoader.requireBreed(giver, share.line());

    List<String> participants = new ArrayList<>();
    participants.add(giver);
    for (String recipient : share.recipients()) {
      String who = resolveSubject(recipient, share.line());
      ConfigLoader.requireBreed(who, share.line());
      if (!participants.contains(who)) {
        participants.add(who);
      }
    }

    if (share.stash() != null) {
      shareStash(share.stash(), participants, share.line());
    } else {
      shareItems(giver, participants, share.line());
    }
  }

  private void runPass(final AstNode.Pass pass) throws BarkError {
    String giver = resolveSubject(pass.giver(), pass.line());
    String recipient = resolveSubject(pass.recipient(), pass.line());
    bindPronounSubject(pass.giver(), giver);
    ConfigLoader.requireBreed(giver, pass.line());
    ConfigLoader.requireBreed(recipient, pass.line());
    double giverItems = asNumber(lookupField(giver, DogTopics.ITEMS, pass.line()), pass.line());
    if (giverItems <= 0) {
      return;
    }
    double recipientItems =
        asNumber(lookupField(recipient, DogTopics.ITEMS, pass.line()), pass.line());
    setNumberField(giver, DogTopics.ITEMS, giverItems - 1, pass.line());
    setNumberField(recipient, DogTopics.ITEMS, recipientItems + 1, pass.line());
    dog(giver).removeAnyHeldObject();
    dog(recipient).addHeldObject("toy");
  }

  private void shareItems(final String giver, final List<String> participants, final int line)
      throws BarkError {
    double total = asNumber(lookupField(giver, DogTopics.ITEMS, line), line);
    int count = participants.size();
    if (count == 0 || total <= 0) {
      return;
    }
    int perDog = (int) (total / count);
    int remainder = (int) total % count;
    String giverKey = ConfigLoader.normalise(giver);
    for (String who : participants) {
      int toys = perDog;
      if (ConfigLoader.normalise(who).equals(giverKey)) {
        toys += remainder;
      }
      setNumberField(who, DogTopics.ITEMS, toys, line);
    }
  }

  private void shareStash(final String stashName, final List<String> participants, final int line)
      throws BarkError {
    List<BarkValue> contents = new ArrayList<>(stashItems(stashName, line));
    stashes.put(ConfigLoader.normalise(stashName), new ArrayList<>());
    for (int i = 0; i < contents.size(); i++) {
      String who = participants.get(i % participants.size());
      double add = contents.get(i) instanceof BarkValue.BarkNumber n ? n.value() : 1;
      double current = asNumber(lookupField(who, DogTopics.FOOD, line), line);
      setNumberField(who, DogTopics.FOOD, current + add, line);
      if (!(contents.get(i) instanceof BarkValue.BarkNumber)) {
        String label = BarkValue.describe(contents.get(i)).replace("\"", "").trim();
        String object = Keywords.resolveObjectName(label);
        if (object != null) {
          dog(who).addHeldObject(object);
        }
      }
    }
  }

  private void setNumberField(
      final String breed, final String field, final double amount, final int line)
      throws BarkError {
    if (!DogTopics.isNumericField(field)) {
      throw new BarkError(line, "Only numeric dog fields accept a count. not \"" + field + "\".");
    }
    dog(ConfigLoader.normalise(breed)).set(field, BarkValue.of(amount), line);
  }

  private boolean hasTrait(final String breed, final String trait) {
    Set<String> traits = breedTraits.get(ConfigLoader.normalise(breed));
    return traits != null && traits.contains(trait);
  }

  private void tickGreedy(final String breed) throws BarkError {
    if (breed == null || !hasTrait(breed, "greedy")) {
      return;
    }
    String who = ConfigLoader.normalise(breed);
    int uses = greedyUses.merge(who, 1, Integer::sum);
    if (uses >= GREEDY_THRESHOLD) {
      greedyUses.put(who, 0);
      double food = asNumber(lookupField(who, DogTopics.FOOD, 0), 0);
      if (food > 0) {
        setNumberField(who, DogTopics.FOOD, food - 1, 0);
      }
    }
  }

  private void tickChaser() throws BarkError {
    if (lastBreed == null || !hasTrait(lastBreed, "chaser")) {
      return;
    }
    String who = ConfigLoader.normalise(lastBreed);
    double items = asNumber(lookupField(who, DogTopics.ITEMS, 0), 0);
    setNumberField(who, DogTopics.ITEMS, items + 1, 0);
  }

  private void runListen(final AstNode.Listen listen) throws BarkError {
    System.out.print("> ");
    String who = resolveSubject(listen.dog(), listen.line());
    ConfigLoader.requireBreed(who, listen.line());
    values.put(who, StdinReader.read(listen.line()));
  }

  private void runWait(final AstNode.Wait wait) throws BarkError {
    if (wait.seconds() < 0) {
      throw new BarkError(wait.line(), "Even dogs can't rest for negative time.");
    }
    double seconds = wait.seconds();
    if (lastBreed != null && hasTrait(lastBreed, "lazy")) {
      seconds = seconds * 2;
    }
    try {
      Thread.sleep(Math.round(seconds * 1000));
    } catch (InterruptedException _) {
      Thread.currentThread().interrupt();
      throw new BarkError(wait.line(), "Something woke the dog up early.");
    }
  }

  private void runIfChain(final AstNode.IfChain chain) throws BarkError {
    for (AstNode.IfBranch branch : chain.branches()) {
      if (truthy(eval(branch.condition(), chain.line()))) {
        runSteps(branch.steps());
        return;
      }
    }
    if (chain.elseSteps() != null) {
      runSteps(chain.elseSteps());
    }
  }

  private void runWhile(final AstNode.WhileLoop loop) throws BarkError {
    while (truthy(eval(loop.condition(), loop.line()))) {
      tickChaser();
      try {
        runSteps(loop.steps());
      } catch (LoopSignal signal) {
        if (signal.kind() == LoopSignal.Kind.BREAK) {
          break;
        }
      }
    }
  }

  private void runUntil(final AstNode.UntilLoop loop) throws BarkError {
    while (!truthy(eval(loop.condition(), loop.line()))) {
      tickChaser();
      try {
        runSteps(loop.steps());
      } catch (LoopSignal signal) {
        if (signal.kind() == LoopSignal.Kind.BREAK) {
          break;
        }
      }
    }
  }

  private void runForEach(final AstNode.ForEach loop) throws BarkError {
    List<BarkValue> items = new ArrayList<>(stashItems(loop.stash(), loop.line()));
    for (BarkValue item : items) {
      tickChaser();
      values.put(ConfigLoader.normalise(loop.variable()), item);
      try {
        runSteps(loop.steps());
      } catch (LoopSignal signal) {
        if (signal.kind() == LoopSignal.Kind.BREAK) {
          break;
        }
      }
    }
  }

  private void runStashInit(final AstNode.StashInit init) throws BarkError {
    noteSubject(init.stash());
    List<BarkValue> items = new ArrayList<>();
    for (ParseExpression item : init.items()) {
      items.add(eval(item, init.line()));
    }
    stashes.put(ConfigLoader.normalise(init.stash()), items);
  }

  private void runStashAppend(final AstNode.StashAppend append) throws BarkError {
    noteSubject(append.stash());
    stashItems(append.stash(), append.line()).add(eval(append.item(), append.line()));
  }

  private void runStashSet(final AstNode.StashSet set) throws BarkError {
    noteSubject(set.stash());
    List<BarkValue> items = stashItems(set.stash(), set.line());
    int index = StashSpots.resolveIndex(items.size(), set.which(), set.line());
    items.set(index, eval(set.value(), set.line()));
  }

  private void runStashRemove(final AstNode.StashRemove remove) throws BarkError {
    noteSubject(remove.stash());
    List<BarkValue> items = stashItems(remove.stash(), remove.line());
    int index = StashSpots.resolveIndex(items.size(), remove.which(), remove.line());
    items.remove(index);
  }

  private void runTakeFromStash(final AstNode.TakeFromStash take) throws BarkError {
    String who = resolveSubject(take.subject(), take.line());
    ConfigLoader.requireBreed(who, take.line());
    bindPronounSubject(take.subject(), who);
    noteSubject(who);
    List<BarkValue> items = stashItems(take.stash(), take.line());
    int index = resolveStashTakeIndex(items, take.which(), take.line());
    items.remove(index);
    double current = asNumber(lookupField(who, DogTopics.FOOD, take.line()), take.line());
    setNumberField(who, DogTopics.FOOD, current + 1, take.line());
    String object = inventoryObjectFromTopic("cookie");
    if (object != null) {
      dog(who).addHeldObject(object);
    }
    tickGreedy(who);
  }

  private int resolveStashTakeIndex(final List<BarkValue> items, final String which, final int line)
      throws BarkError {
    if (StashSpots.isStashSpot(which)) {
      return StashSpots.resolveIndex(items.size(), which, line);
    }
    String target = ConfigLoader.normalise(which);
    for (int i = 0; i < items.size(); i++) {
      if (ConfigLoader.normalise(BarkValue.describe(items.get(i))).equals(target)) {
        return i;
      }
    }
    throw new BarkError(line, "The jar doesn't have \"" + which + "\" for the dog to take.");
  }

  private void runStashClear(final AstNode.StashClear clear) {
    noteSubject(clear.stash());
    stashes.put(ConfigLoader.normalise(clear.stash()), new ArrayList<>());
  }

  private void runPileInit(final AstNode.PileInit init) throws BarkError {
    noteSubject(init.pile());
    List<BarkValue> items = new ArrayList<>();
    for (ParseExpression item : init.items()) {
      items.add(eval(item, init.line()));
    }
    piles.put(ConfigLoader.normalise(init.pile()), items);
  }

  private void runPilePush(final AstNode.PilePush push) throws BarkError {
    noteSubject(push.pile());
    pileItems(push.pile(), push.line()).add(eval(push.item(), push.line()));
  }

  private void runPilePop(final AstNode.PilePop pop) throws BarkError {
    noteSubject(pop.pile());
    List<BarkValue> items = pileItems(pop.pile(), pop.line());
    if (items.isEmpty()) {
      throw new BarkError(pop.line(), "The pile is flat. Nothing to dig up.");
    }
    items.remove(items.size() - 1);
  }

  private void runPileClear(final AstNode.PileClear clear) {
    noteSubject(clear.pile());
    piles.put(ConfigLoader.normalise(clear.pile()), new ArrayList<>());
  }

  private void exitProgram() {
    System.out.println("The dogs are already looking through the windows to wait for your return!");
    System.exit(0);
  }

  private BarkValue eval(final ParseExpression expression, final int line) throws BarkError {
    return switch (expression) {
      case ParseExpression.StringLiteral s -> BarkValue.of(s.value());
      case ParseExpression.NumberLiteral n -> BarkValue.of(n.value());
      case ParseExpression.BooleanLiteral b -> BarkValue.of(b.value());
      case ParseExpression.NullLiteral _ -> new BarkValue.BarkNull();
      case ParseExpression.PeriodLiteral _ -> BarkValue.of("");
      case ParseExpression.Empty _ -> BarkValue.of("");
      case ParseExpression.Variable v -> lookupVariable(v.value(), line);
      case ParseExpression.Field f -> lookupField(f.subject(), f.topic(), line);
      case ParseExpression.Binary b -> evalBinary(b, line);
      case ParseExpression.Not n -> BarkValue.of(!truthy(eval(n.operand(), line)));
      case ParseExpression.Comparison c -> evalComparison(c, line);
      case ParseExpression.HasExact h -> evalHasExact(h, line);
      case ParseExpression.HasTrait h -> evalHasTrait(h, line);
      case ParseExpression.Logical l -> evalLogical(l, line);
      case ParseExpression.StashAccess s -> evalStashAccess(s, line);
      case ParseExpression.PileAccess p -> evalPileAccess(p, line);
      case ParseExpression.Join j -> evalJoin(j, line);
      case ParseExpression.FunctionCall c -> callTrick(c, line);
      case ParseExpression.Length l -> evalLength(l, line);
      case ParseExpression.Contains c -> evalContains(c, line);
      case ParseExpression.TypeCheck t -> evalTypeCheck(t, line);
    };
  }

  private BarkValue evalComparison(final ParseExpression.Comparison c, final int line)
      throws BarkError {
    BarkValue left = eval(c.left(), line);
    BarkValue right = eval(c.right(), line);
    Comparator comparator = comparators.get(c.op());
    if (comparator == null) {
      throw new BarkError(line, "The dogs don't know how to compare that.");
    }
    return BarkValue.of(comparator.compare(left, right, line));
  }

  private BarkValue evalHasExact(final ParseExpression.HasExact has, final int line)
      throws BarkError {
    String subject = resolveSubject(has.subject(), line);
    double actual = asNumber(lookupField(subject, has.topic(), line), line);
    return BarkValue.of(actual == has.amount());
  }

  private BarkValue evalHasTrait(final ParseExpression.HasTrait has, final int line)
      throws BarkError {
    String who = resolveSubject(has.subject(), line);
    ConfigLoader.requireBreed(who, line);
    return BarkValue.of(hasTrait(who, has.trait()));
  }

  private BarkValue evalLogical(final ParseExpression.Logical logical, final int line)
      throws BarkError {
    BarkValue left = eval(logical.left(), line);
    BarkValue right = eval(logical.right(), line);
    boolean result =
        switch (logical.op()) {
          case AND -> truthy(left) && truthy(right);
          case OR -> truthy(left) || truthy(right);
          case NOR -> !truthy(left) && !truthy(right);
        };
    return BarkValue.of(result);
  }

  private BarkValue evalBinary(final ParseExpression.Binary binary, final int line)
      throws BarkError {
    BarkValue left = eval(binary.left(), line);
    BarkValue right = eval(binary.right(), line);
    return switch (binary.op()) {
      case PLUS -> {
        if (left instanceof BarkValue.BarkNumber ln && right instanceof BarkValue.BarkNumber rn) {
          yield BarkValue.of(ln.value() + rn.value());
        }
        yield BarkValue.of(BarkValue.describe(left) + BarkValue.describe(right));
      }
      case MINUS -> BarkValue.of(asNumber(left, line) - asNumber(right, line));
      case STAR -> BarkValue.of(asNumber(left, line) * asNumber(right, line));
      case SLASH -> {
        double divisor = asNumber(right, line);
        if (divisor == 0) {
          throw new BarkError(line, "Even dogs know you can't divide by zero.");
        }
        yield BarkValue.of(asNumber(left, line) / divisor);
      }
    };
  }

  private BarkValue evalStashAccess(final ParseExpression.StashAccess access, final int line)
      throws BarkError {
    List<BarkValue> items = stashItems(access.stash(), line);
    return switch (access.part()) {
      case ALL -> BarkValue.of(formatList(items));
      case COUNT -> BarkValue.of(items.size());
      case ELEMENT -> items.get(StashSpots.resolveIndex(items.size(), access.which(), line));
    };
  }

  private BarkValue evalPileAccess(final ParseExpression.PileAccess access, final int line)
      throws BarkError {
    List<BarkValue> items = pileItems(access.pile(), line);
    return switch (access.part()) {
      case ALL -> BarkValue.of(formatList(items));
      case COUNT -> BarkValue.of(items.size());
      case TOP -> {
        if (items.isEmpty()) {
          throw new BarkError(line, "The pile is flat. Nothing on top.");
        }
        yield items.get(items.size() - 1);
      }
    };
  }

  private BarkValue evalJoin(final ParseExpression.Join join, final int line) throws BarkError {
    List<BarkValue> items = stashItems(join.stash(), line);
    BarkValue delimiter = eval(join.delimiter(), line);
    if (!(delimiter instanceof BarkValue.BarkString delim)) {
      throw new BarkError(line, "Join needs a word to stick between items. like \" and \".");
    }
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < items.size(); i++) {
      if (i > 0) {
        sb.append(delim.value());
      }
      sb.append(BarkValue.describe(items.get(i)));
    }
    return BarkValue.of(sb.toString());
  }

  private BarkValue callTrick(final ParseExpression.FunctionCall call, final int line)
      throws BarkError {
    AstNode.FunctionDef trick = tricks.get(ConfigLoader.normalise(call.name()));
    if (trick == null) {
      throw new BarkError(line, "No trick named \"" + call.name() + "\" in the pack.");
    }
    if (call.args().size() != trick.params().size()) {
      throw new BarkError(
          line,
          "Trick \""
              + call.name()
              + "\" wants "
              + trick.params().size()
              + " treats, not "
              + call.args().size()
              + ".");
    }
    Map<String, BarkValue> saved = new HashMap<>(values);
    for (int i = 0; i < trick.params().size(); i++) {
      values.put(trick.params().get(i), eval(call.args().get(i), line));
    }
    runSteps(trick.steps());
    BarkValue result = eval(trick.returnExpression(), line);
    values.clear();
    values.putAll(saved);
    return result;
  }

  private BarkValue evalLength(final ParseExpression.Length length, final int line)
      throws BarkError {
    BarkValue value = eval(length.value(), line);
    if (value instanceof BarkValue.BarkString s) {
      return BarkValue.of(s.value().length());
    }
    throw new BarkError(line, "Letters only works on words. not bone counts.");
  }

  private BarkValue evalContains(final ParseExpression.Contains contains, final int line)
      throws BarkError {
    BarkValue haystack = eval(contains.haystack(), line);
    BarkValue needle = eval(contains.needle(), line);
    if (haystack instanceof BarkValue.BarkString h && needle instanceof BarkValue.BarkString n) {
      return BarkValue.of(h.value().contains(n.value()));
    }
    throw new BarkError(line, "Sniffs inside only works on words. not numbers.");
  }

  private BarkValue evalTypeCheck(final ParseExpression.TypeCheck typeCheck, final int line)
      throws BarkError {
    BarkValue value = eval(typeCheck.value(), line);
    boolean result =
        switch (typeCheck.type()) {
          case NUMBER -> value instanceof BarkValue.BarkNumber;
          case WORDS -> value instanceof BarkValue.BarkString;
          case NOTHING -> value instanceof BarkValue.BarkNull;
        };
    return BarkValue.of(result);
  }

  private BarkValue lookupVariable(final String name, final int line) throws BarkError {
    String key = ConfigLoader.normalise(name);
    if (ConfigLoader.isStoryNumberConstant(key)) {
      return BarkValue.of(memory);
    }
    if (ConfigLoader.isStoryTextConstant(key)) {
      return BarkValue.of(journal);
    }
    BarkValue value = values.get(key);
    if (value != null) {
      return value;
    }
    Prop prop = props.get(key);
    if (prop != null) {
      return prop.value();
    }
    throw new BarkError(
        line,
        "No scent of \""
            + name
            + "\" anywhere."
            + NameHints.hintPhrase(name, ConfigLoader.listObjects()));
  }

  private static short toMemory(final BarkValue value, final int line) throws BarkError {
    if (!(value instanceof BarkValue.BarkNumber number)) {
      throw new BarkError(line, "memory only holds a whole number. not words or true/false.");
    }
    double amount = number.value();
    if (Double.isNaN(amount)
        || Double.isInfinite(amount)
        || Math.floor(amount) != amount
        || amount < Short.MIN_VALUE
        || amount > Short.MAX_VALUE) {
      throw new BarkError(
          line,
          "memory must be a whole number between "
              + Short.MIN_VALUE
              + " and "
              + Short.MAX_VALUE
              + ".");
    }
    return (short) amount;
  }

  private static String toJournal(final BarkValue value, final int line) throws BarkError {
    if (value instanceof BarkValue.BarkBoolean) {
      throw new BarkError(line, "journal only holds words. not true/false.");
    }
    if (value instanceof BarkValue.BarkNull) {
      return "";
    }
    return BarkValue.describe(value);
  }

  private BarkValue lookupField(final String subject, final String topic, final int line)
      throws BarkError {
    String who = resolveSubject(subject, line);
    String field = DogTopics.resolve(topic);
    if (field == null) {
      String object = Keywords.resolveObjectName(topic);
      if (object != null) {
        return BarkValue.of(dog(who).countObject(object));
      }
      throw new BarkError(line, "No scent of \"" + subject + " " + topic + "\" anywhere.");
    }
    BarkValue value = dog(who).get(field, line);
    if (field.equals(DogTopics.NAME)
        && value instanceof BarkValue.BarkString s
        && s.value().isEmpty()) {
      BarkValue direct = values.get(who);
      if (direct != null) {
        return direct;
      }
    }
    return value;
  }

  private Dog dog(final String breed) {
    return dogs.computeIfAbsent(ConfigLoader.normalise(breed), _ -> Dog.fresh());
  }

  // Breed for print mood. Unresolved pronouns return null instead of throwing.
  private String resolveSubjectForMood(final String name) {
    String normalized = ConfigLoader.normalise(name);
    if (Keywords.isFeminineBreedPronoun(normalized)) {
      return lastFeminineBreed != null ? lastFeminineBreed : lastBreed;
    }
    if (Keywords.isMasculineBreedPronoun(normalized)) {
      return lastMasculineBreed != null ? lastMasculineBreed : lastBreed;
    }
    if (Keywords.isBreedPronounWord(normalized)) {
      return lastBreed;
    }
    if (Keywords.isObjectPronounWord(normalized)) {
      return null;
    }
    if (ConfigLoader.typeOf(normalized).orElse(null) == VariableType.BREED) {
      return normalized;
    }
    String byName = petNames.get(normalized);
    if (byName != null) {
      return byName;
    }
    return null;
  }

  // Pronouns resolve to the last dog of that gender, then lastBreed
  private String resolveSubject(final String name, final int line) throws BarkError {
    String normalized = ConfigLoader.normalise(name);
    if (Keywords.isFeminineBreedPronoun(normalized)) {
      if (lastFeminineBreed != null) {
        return lastFeminineBreed;
      }
      if (lastBreed != null) {
        return lastBreed;
      }
      throw new BarkError(
          line, "The dogs don't know who \"" + name + "\" is yet. Mention a dog first.");
    }
    if (Keywords.isMasculineBreedPronoun(normalized)) {
      if (lastMasculineBreed != null) {
        return lastMasculineBreed;
      }
      if (lastBreed != null) {
        return lastBreed;
      }
      throw new BarkError(
          line, "The dogs don't know who \"" + name + "\" is yet. Mention a dog first.");
    }
    if (Keywords.isBreedPronounWord(normalized)) {
      if (lastBreed == null) {
        throw new BarkError(
            line, "The dogs don't know who \"" + name + "\" is yet. Mention a dog first.");
      }
      return lastBreed;
    }
    if (Keywords.isObjectPronounWord(normalized)) {
      if (lastObject == null) {
        throw new BarkError(
            line,
            "Nothing has been pointed at yet. Mention a stash, pile, or object before \""
                + name
                + "\".");
      }
      return lastObject;
    }
    String byName = petNames.get(normalized);
    if (byName != null) {
      noteSubject(byName);
      bindPronounSubject(name, byName);
      return byName;
    }
    if (Keywords.isPetNameWord(normalized) && !petNames.containsKey(normalized)) {
      throw new BarkError(
          line, "No dog named \"" + name + "\" yet. introduce a breed and set her name first.");
    }
    noteSubject(normalized);
    return normalized;
  }

  private void registerPetName(final String breed, final BarkValue value) {
    if (!(value instanceof BarkValue.BarkString named)) {
      return;
    }
    String key = ConfigLoader.normalise(breed);
    petNames.entrySet().removeIf(entry -> entry.getValue().equals(key));
    String label = ConfigLoader.normalise(named.value());
    if (label.isEmpty()) {
      return;
    }
    petNames.put(label, key);
  }

  private void bindPronounSubject(final String subject, final String resolved) {
    String normalized = ConfigLoader.normalise(subject);
    if (Keywords.isFeminineBreedPronoun(normalized)) {
      lastFeminineBreed = resolved;
    }
    if (Keywords.isMasculineBreedPronoun(normalized)) {
      lastMasculineBreed = resolved;
    }
  }

  private void noteSubject(final String name) {
    String normalized = ConfigLoader.normalise(name);
    VariableType type = ConfigLoader.typeOf(normalized).orElse(null);
    if (type == VariableType.STORY_NUMBER || type == VariableType.STORY_TEXT) {
      return;
    }
    if (type == VariableType.BREED) {
      lastBreed = normalized;
      dog(normalized);
    } else if (type == VariableType.OBJECT
        || type == VariableType.STASH
        || type == VariableType.PILE) {
      lastObject = normalized;
    }
  }

  private List<BarkValue> stashItems(final String name, final int line) throws BarkError {
    ConfigLoader.requireStash(name, line);
    return stashes.computeIfAbsent(ConfigLoader.normalise(name), _ -> new ArrayList<>());
  }

  private List<BarkValue> pileItems(final String name, final int line) throws BarkError {
    ConfigLoader.requirePile(name, line);
    return piles.computeIfAbsent(ConfigLoader.normalise(name), _ -> new ArrayList<>());
  }

  private static String formatList(final List<BarkValue> items) {
    if (items.isEmpty()) {
      return "nothing";
    }
    return String.join(", ", items.stream().map(BarkValue::describe).toList());
  }

  private static double asNumber(final BarkValue value, final int line) throws BarkError {
    if (value instanceof BarkValue.BarkNumber n) {
      return n.value();
    }
    throw new BarkError(
        line,
        "The dogs can't count that. expected a number, got " + BarkValue.describe(value) + ".");
  }

  private static boolean truthy(final BarkValue value) {
    if (value == null) {
      return false;
    }
    return switch (value) {
      case BarkValue.BarkBoolean b -> b.value();
      case BarkValue.BarkNumber n -> n.value() != 0;
      case BarkValue.BarkNull _ -> false;
      case BarkValue.BarkString s -> !s.value().isEmpty();
    };
  }
}
