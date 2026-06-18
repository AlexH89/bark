from __future__ import annotations

import math
import sys
import time
from typing import Any

from pbark.cli import dog_art as DogArt
from pbark.config.config_loader import ConfigLoader
from pbark.config.dog_topics import DogTopics
from pbark.config.name_hints import NameHints
from pbark.config.trait_loader import TraitLoader
from pbark.config.variable_type import VariableType
from pbark.errors import BarkError
from pbark.interpreter import bark_value as bv
from pbark.interpreter.comparators import Comparators
from pbark.interpreter.dog import Dog
from pbark.interpreter.loop_signal import LoopSignal
from pbark.interpreter.print_styles import PrintStyles
from pbark.interpreter.prop import Prop
from pbark.interpreter.stdin_reader import StdinReader
from pbark.parser import ast_node as AstNode
from pbark.parser import keywords as Keywords
from pbark.parser.collection import stash_spots as StashSpots
from pbark.parser.expression.comparison_op import ComparisonOp
from pbark.parser.parse_expression import (
    Binary,
    BinaryOp,
    BooleanLiteral,
    Comparison,
    Contains,
    Empty,
    Field,
    FunctionCall,
    HasExact,
    HasTrait,
    Join,
    Length,
    Logical,
    LogicalOp,
    Not,
    NullLiteral,
    NumberLiteral,
    ParseExpression,
    PeriodLiteral,
    PileAccess,
    PilePart,
    StashAccess,
    StashPart,
    StringLiteral,
    TypeCheck,
    ValueType,
    Variable,
)
from pbark.print_style import PrintStyle

_GREEDY_THRESHOLD = TraitLoader.GREEDY_FEEDS_BEFORE_LOSS


class Interpreter:
    def __init__(self) -> None:
        self._values: dict[str, bv.BarkValue] = {}
        self._dogs: dict[str, Dog] = {}
        self._props: dict[str, Prop] = {}
        self._stashes: dict[str, list[bv.BarkValue]] = {}
        self._piles: dict[str, list[bv.BarkValue]] = {}
        self._tricks: dict[str, AstNode.FunctionDef] = {}
        self._comparators = Comparators.create()
        self._last_breed: str | None = None
        self._last_feminine_breed: str | None = None
        self._last_masculine_breed: str | None = None
        self._last_object: str | None = None
        self._breed_traits = TraitLoader.default_traits()
        self._greedy_uses: dict[str, int] = {}
        self._pet_names: dict[str, str] = {}
        self._memory = 0
        self._journal = ""
        self._last_printed_text: str | None = None

    def run(self, program: AstNode.Program) -> None:
        for statement in program.statements:
            self._run_statement(statement)

    def _run_steps(self, steps: list[Any]) -> None:
        for step in steps:
            self._run_statement(step)

    def _run_statement(self, node: Any) -> None:
        if isinstance(node, AstNode.Print):
            self._run_print(node)
        elif isinstance(node, AstNode.Listen):
            self._run_listen(node)
        elif isinstance(node, AstNode.Wait):
            self._run_wait(node)
        elif isinstance(node, AstNode.Assign):
            self._run_assign(node)
        elif isinstance(node, AstNode.SetAttribute):
            self._run_set_attribute(node)
        elif isinstance(node, AstNode.IfChain):
            self._run_if_chain(node)
        elif isinstance(node, AstNode.WhileLoop):
            self._run_while(node)
        elif isinstance(node, AstNode.UntilLoop):
            self._run_until(node)
        elif isinstance(node, AstNode.ForEach):
            self._run_for_each(node)
        elif isinstance(node, AstNode.FunctionDef):
            self._tricks[ConfigLoader.normalise(node.name)] = node
        elif isinstance(node, AstNode.Break):
            raise LoopSignal(LoopSignal.Kind.BREAK)
        elif isinstance(node, AstNode.Continue):
            raise LoopSignal(LoopSignal.Kind.CONTINUE)
        elif isinstance(node, AstNode.StashInit):
            self._run_stash_init(node)
        elif isinstance(node, AstNode.StashAppend):
            self._run_stash_append(node)
        elif isinstance(node, AstNode.StashSet):
            self._run_stash_set(node)
        elif isinstance(node, AstNode.StashRemove):
            self._run_stash_remove(node)
        elif isinstance(node, AstNode.StashClear):
            self._run_stash_clear(node)
        elif isinstance(node, AstNode.PileInit):
            self._run_pile_init(node)
        elif isinstance(node, AstNode.PilePush):
            self._run_pile_push(node)
        elif isinstance(node, AstNode.PilePop):
            self._run_pile_pop(node)
        elif isinstance(node, AstNode.PileClear):
            self._run_pile_clear(node)
        elif isinstance(node, AstNode.Share):
            self._run_share(node)
        elif isinstance(node, AstNode.Pass):
            self._run_pass(node)
        elif isinstance(node, AstNode.SetTrait):
            self._run_set_trait(node)
        elif isinstance(node, AstNode.TakeFromStash):
            self._run_take_from_stash(node)
        elif isinstance(node, AstNode.LineGroup):
            self._run_steps(node.statements)
        elif isinstance(node, AstNode.Exit):
            self._exit_program()
        elif isinstance(node, AstNode.Program):
            raise BarkError(node.line, "Nested programs? The pack only runs one story at a time.")

    def _run_print(self, print_node: AstNode.Print) -> None:
        if print_node.paw_print:
            DogArt.print_paws()
            return
        style = print_node.style
        speaker = None
        if print_node.dog_speaker is not None:
            speaker = self._resolve_subject_for_mood(print_node.dog_speaker)
            if speaker is not None:
                if style == PrintStyle.BARK and self._has_trait(speaker, "loud"):
                    style = PrintStyle.GROWL
                elif style == PrintStyle.BARK:
                    items = self._as_number(self._lookup_field(speaker, DogTopics.ITEMS, 0), 0)
                    if items <= 0:
                        style = PrintStyle.WHINE
        bare = all(isinstance(v, Empty) for v in print_node.values)
        if bare:
            self._emit_print(style, PrintStyles.bare_text(style), speaker)
            return
        parts = [bv.describe(self._eval(v, print_node.line)) for v in print_node.values]
        self._emit_print(style, " ".join(parts), speaker)

    def _emit_print(self, style: PrintStyle | None, text: str | None, speaker: str | None) -> None:
        resolved_style = style if style is not None else PrintStyle.BARK
        output = text if text is not None else ""
        if speaker is not None and self._has_trait(speaker, "playful"):
            output = output[::-1]
        if (
            resolved_style in (PrintStyle.WHINE, PrintStyle.WHIMPER)
            and self._last_breed is not None
            and self._has_trait(self._last_breed, "wet")
        ):
            formatted = (
                PrintStyles.format(resolved_style, output)
                + " "
                + PrintStyles.format(resolved_style, output)
            ).strip()
        else:
            formatted = PrintStyles.format(resolved_style, output)
        if (
            self._last_breed is not None
            and self._has_trait(self._last_breed, "fetchy")
            and formatted == self._last_printed_text
        ):
            print("*tail THUMP THUMP, same stick, still the best stick*")
        self._last_printed_text = formatted
        print(formatted)

    def _run_assign(self, assign: AstNode.Assign) -> None:
        key = ConfigLoader.normalise(assign.variable)
        if ConfigLoader.is_story_number_constant(key):
            self._memory = self._to_memory(self._eval(assign.value, assign.line), assign.line)
            return
        if ConfigLoader.is_story_text_constant(key):
            self._journal = self._to_journal(self._eval(assign.value, assign.line), assign.line)
            return
        self._note_subject(assign.variable)
        value = self._eval(assign.value, assign.line)
        if ConfigLoader.type_of(key) == VariableType.OBJECT:
            self._props[key] = Prop(value)
        else:
            self._values[key] = value

    def _run_set_attribute(self, node: AstNode.SetAttribute) -> None:
        who = self._resolve_subject(node.subject, node.line)
        ConfigLoader.require_breed(who, node.line)
        self._bind_pronoun_subject(node.subject, who)
        field = DogTopics.resolve(node.topic)
        if field is None:
            raise BarkError(
                node.line,
                f'The dogs don\'t track "{node.topic}". Only {DogTopics.CANONICAL}.',
            )
        self._dog(who).set(field, self._eval(node.value, node.line), node.line)
        if field == DogTopics.NAME:
            self._register_pet_name(who, self._dog(who).get(field, node.line))
        if isinstance(node.value, Binary):
            self._sync_inventory_adjust(who, node.topic, node.value)
            if DogTopics.is_food_field(field):
                self._tick_greedy(who)

    def _sync_inventory_adjust(self, who: str, topic_word: str, binary: Binary) -> None:
        obj = self._inventory_object_from_topic(topic_word)
        if obj is None:
            return
        dog = self._dog(who)
        if binary.op is BinaryOp.PLUS:
            dog.add_held_object(obj)
        elif binary.op is BinaryOp.MINUS:
            dog.remove_held_object(obj)

    @staticmethod
    def _inventory_object_from_topic(topic_word: str) -> str | None:
        obj = Keywords.resolve_object_name(topic_word)
        if obj is not None:
            return obj
        normalized = ConfigLoader.normalise(topic_word)
        if normalized in ("toy", "toys"):
            return "toy"
        return None

    def _run_set_trait(self, node: AstNode.SetTrait) -> None:
        who = self._resolve_subject(node.subject, node.line)
        ConfigLoader.require_breed(who, node.line)
        trait = ConfigLoader.normalise(node.trait)
        if node.enabled:
            self._breed_traits.setdefault(who, set()).add(trait)
        else:
            traits = self._breed_traits.get(who)
            if traits is not None:
                traits.discard(trait)

    def _run_share(self, share: AstNode.Share) -> None:
        giver = self._resolve_subject(share.giver, share.line)
        ConfigLoader.require_breed(giver, share.line)
        participants = [giver]
        for recipient in share.recipients:
            who = self._resolve_subject(recipient, share.line)
            ConfigLoader.require_breed(who, share.line)
            if who not in participants:
                participants.append(who)
        if share.stash is not None:
            self._share_stash(share.stash, participants, share.line)
        else:
            self._share_items(giver, participants, share.line)

    def _run_pass(self, pass_node: AstNode.Pass) -> None:
        giver = self._resolve_subject(pass_node.giver, pass_node.line)
        recipient = self._resolve_subject(pass_node.recipient, pass_node.line)
        self._bind_pronoun_subject(pass_node.giver, giver)
        ConfigLoader.require_breed(giver, pass_node.line)
        ConfigLoader.require_breed(recipient, pass_node.line)
        giver_items = self._as_number(self._lookup_field(giver, DogTopics.ITEMS, pass_node.line), pass_node.line)
        if giver_items <= 0:
            return
        recipient_items = self._as_number(
            self._lookup_field(recipient, DogTopics.ITEMS, pass_node.line), pass_node.line
        )
        self._set_number_field(giver, DogTopics.ITEMS, giver_items - 1, pass_node.line)
        self._set_number_field(recipient, DogTopics.ITEMS, recipient_items + 1, pass_node.line)
        self._dog(giver).remove_any_held_object()
        self._dog(recipient).add_held_object("toy")

    def _share_items(self, giver: str, participants: list[str], line: int) -> None:
        total = self._as_number(self._lookup_field(giver, DogTopics.ITEMS, line), line)
        count = len(participants)
        if count == 0 or total <= 0:
            return
        per_dog = int(total / count)
        remainder = int(total) % count
        giver_key = ConfigLoader.normalise(giver)
        for who in participants:
            toys = per_dog + (remainder if ConfigLoader.normalise(who) == giver_key else 0)
            self._set_number_field(who, DogTopics.ITEMS, toys, line)

    def _share_stash(self, stash_name: str, participants: list[str], line: int) -> None:
        contents = list(self._stash_items(stash_name, line))
        self._stashes[ConfigLoader.normalise(stash_name)] = []
        for i, item in enumerate(contents):
            who = participants[i % len(participants)]
            add = item.value if isinstance(item, bv.BarkNumber) else 1
            current = self._as_number(self._lookup_field(who, DogTopics.FOOD, line), line)
            self._set_number_field(who, DogTopics.FOOD, current + add, line)
            if not isinstance(item, bv.BarkNumber):
                label = bv.describe(item).replace('"', "").strip()
                obj = Keywords.resolve_object_name(label)
                if obj is not None:
                    self._dog(who).add_held_object(obj)

    def _set_number_field(self, breed: str, field: str, amount: float, line: int) -> None:
        if not DogTopics.is_numeric_field(field):
            raise BarkError(line, f'Only numeric dog fields accept a count. not "{field}".')
        self._dog(ConfigLoader.normalise(breed)).set(field, bv.of(amount), line)

    def _has_trait(self, breed: str, trait: str) -> bool:
        traits = self._breed_traits.get(ConfigLoader.normalise(breed))
        return traits is not None and trait in traits

    def _tick_greedy(self, breed: str) -> None:
        if breed is None or not self._has_trait(breed, "greedy"):
            return
        who = ConfigLoader.normalise(breed)
        uses = self._greedy_uses.get(who, 0) + 1
        self._greedy_uses[who] = uses
        if uses >= _GREEDY_THRESHOLD:
            self._greedy_uses[who] = 0
            food = self._as_number(self._lookup_field(who, DogTopics.FOOD, 0), 0)
            if food > 0:
                self._set_number_field(who, DogTopics.FOOD, food - 1, 0)

    def _tick_chaser(self) -> None:
        if self._last_breed is None or not self._has_trait(self._last_breed, "chaser"):
            return
        who = ConfigLoader.normalise(self._last_breed)
        items = self._as_number(self._lookup_field(who, DogTopics.ITEMS, 0), 0)
        self._set_number_field(who, DogTopics.ITEMS, items + 1, 0)

    def _run_listen(self, listen: AstNode.Listen) -> None:
        print("> ", end="")
        who = self._resolve_subject(listen.dog, listen.line)
        ConfigLoader.require_breed(who, listen.line)
        self._values[who] = StdinReader.read(listen.line)

    def _run_wait(self, wait: AstNode.Wait) -> None:
        if wait.seconds < 0:
            raise BarkError(wait.line, "Even dogs can't rest for negative time.")
        seconds = wait.seconds
        if self._last_breed is not None and self._has_trait(self._last_breed, "lazy"):
            seconds *= 2
        try:
            time.sleep(round(seconds * 1000) / 1000)
        except InterruptedError:
            raise BarkError(wait.line, "Something woke the dog up early.") from None

    def _run_if_chain(self, chain: AstNode.IfChain) -> None:
        for branch in chain.branches:
            if self._truthy(self._eval(branch.condition, chain.line)):
                self._run_steps(branch.steps)
                return
        if chain.else_steps is not None:
            self._run_steps(chain.else_steps)

    def _run_while(self, loop: AstNode.WhileLoop) -> None:
        while self._truthy(self._eval(loop.condition, loop.line)):
            self._tick_chaser()
            try:
                self._run_steps(loop.steps)
            except LoopSignal as signal:
                if signal.kind is LoopSignal.Kind.BREAK:
                    break

    def _run_until(self, loop: AstNode.UntilLoop) -> None:
        while not self._truthy(self._eval(loop.condition, loop.line)):
            self._tick_chaser()
            try:
                self._run_steps(loop.steps)
            except LoopSignal as signal:
                if signal.kind is LoopSignal.Kind.BREAK:
                    break

    def _run_for_each(self, loop: AstNode.ForEach) -> None:
        items = list(self._stash_items(loop.stash, loop.line))
        for item in items:
            self._tick_chaser()
            self._values[ConfigLoader.normalise(loop.variable)] = item
            try:
                self._run_steps(loop.steps)
            except LoopSignal as signal:
                if signal.kind is LoopSignal.Kind.BREAK:
                    break

    def _run_stash_init(self, init: AstNode.StashInit) -> None:
        self._note_subject(init.stash)
        items = [self._eval(item, init.line) for item in init.items]
        self._stashes[ConfigLoader.normalise(init.stash)] = items

    def _run_stash_append(self, append: AstNode.StashAppend) -> None:
        self._note_subject(append.stash)
        self._stash_items(append.stash, append.line).append(self._eval(append.item, append.line))

    def _run_stash_set(self, set_node: AstNode.StashSet) -> None:
        self._note_subject(set_node.stash)
        items = self._stash_items(set_node.stash, set_node.line)
        index = StashSpots.resolve_index(len(items), set_node.which, set_node.line)
        items[index] = self._eval(set_node.value, set_node.line)

    def _run_stash_remove(self, remove: AstNode.StashRemove) -> None:
        self._note_subject(remove.stash)
        items = self._stash_items(remove.stash, remove.line)
        index = StashSpots.resolve_index(len(items), remove.which, remove.line)
        items.pop(index)

    def _run_take_from_stash(self, take: AstNode.TakeFromStash) -> None:
        who = self._resolve_subject(take.subject, take.line)
        ConfigLoader.require_breed(who, take.line)
        self._bind_pronoun_subject(take.subject, who)
        self._note_subject(who)
        items = self._stash_items(take.stash, take.line)
        index = self._resolve_stash_take_index(items, take.which, take.line)
        items.pop(index)
        current = self._as_number(self._lookup_field(who, DogTopics.FOOD, take.line), take.line)
        self._set_number_field(who, DogTopics.FOOD, current + 1, take.line)
        obj = self._inventory_object_from_topic("cookie")
        if obj is not None:
            self._dog(who).add_held_object(obj)
        self._tick_greedy(who)

    def _resolve_stash_take_index(self, items: list[bv.BarkValue], which: str, line: int) -> int:
        if StashSpots.is_stash_spot(which):
            return StashSpots.resolve_index(len(items), which, line)
        target = ConfigLoader.normalise(which)
        for i, item in enumerate(items):
            if ConfigLoader.normalise(bv.describe(item)) == target:
                return i
        raise BarkError(line, f'The jar doesn\'t have "{which}" for the dog to take.')

    def _run_stash_clear(self, clear: AstNode.StashClear) -> None:
        self._note_subject(clear.stash)
        self._stashes[ConfigLoader.normalise(clear.stash)] = []

    def _run_pile_init(self, init: AstNode.PileInit) -> None:
        self._note_subject(init.pile)
        items = [self._eval(item, init.line) for item in init.items]
        self._piles[ConfigLoader.normalise(init.pile)] = items

    def _run_pile_push(self, push: AstNode.PilePush) -> None:
        self._note_subject(push.pile)
        self._pile_items(push.pile, push.line).append(self._eval(push.item, push.line))

    def _run_pile_pop(self, pop: AstNode.PilePop) -> None:
        self._note_subject(pop.pile)
        items = self._pile_items(pop.pile, pop.line)
        if not items:
            raise BarkError(pop.line, "The pile is flat. Nothing to dig up.")
        items.pop()

    def _run_pile_clear(self, clear: AstNode.PileClear) -> None:
        self._note_subject(clear.pile)
        self._piles[ConfigLoader.normalise(clear.pile)] = []

    @staticmethod
    def _exit_program() -> None:
        print("The dogs are already looking through the windows to wait for your return!")
        sys.exit(0)

    def _eval(self, expression: ParseExpression, line: int) -> bv.BarkValue:
        if isinstance(expression, StringLiteral):
            return bv.of(expression.value)
        if isinstance(expression, NumberLiteral):
            return bv.of(expression.value)
        if isinstance(expression, BooleanLiteral):
            return bv.of(expression.value)
        if isinstance(expression, NullLiteral):
            return bv.BarkNull()
        if isinstance(expression, PeriodLiteral):
            return bv.of("")
        if isinstance(expression, Empty):
            return bv.of("")
        if isinstance(expression, Variable):
            return self._lookup_variable(expression.value, line)
        if isinstance(expression, Field):
            return self._lookup_field(expression.subject, expression.topic, line)
        if isinstance(expression, Binary):
            return self._eval_binary(expression, line)
        if isinstance(expression, Not):
            return bv.of(not self._truthy(self._eval(expression.operand, line)))
        if isinstance(expression, Comparison):
            return self._eval_comparison(expression, line)
        if isinstance(expression, HasExact):
            return self._eval_has_exact(expression, line)
        if isinstance(expression, HasTrait):
            return self._eval_has_trait(expression, line)
        if isinstance(expression, Logical):
            return self._eval_logical(expression, line)
        if isinstance(expression, StashAccess):
            return self._eval_stash_access(expression, line)
        if isinstance(expression, PileAccess):
            return self._eval_pile_access(expression, line)
        if isinstance(expression, Join):
            return self._eval_join(expression, line)
        if isinstance(expression, FunctionCall):
            return self._call_trick(expression, line)
        if isinstance(expression, Length):
            return self._eval_length(expression, line)
        if isinstance(expression, Contains):
            return self._eval_contains(expression, line)
        if isinstance(expression, TypeCheck):
            return self._eval_type_check(expression, line)
        raise BarkError(line, "The dogs don't know how to evaluate that.")

    def _eval_comparison(self, c: Comparison, line: int) -> bv.BarkValue:
        left = self._eval(c.left, line)
        right = self._eval(c.right, line)
        comparator = self._comparators.get(c.op)
        if comparator is None:
            raise BarkError(line, "The dogs don't know how to compare that.")
        return bv.of(comparator(left, right, line))

    def _eval_has_exact(self, has: HasExact, line: int) -> bv.BarkValue:
        subject = self._resolve_subject(has.subject, line)
        actual = self._as_number(self._lookup_field(subject, has.topic, line), line)
        return bv.of(actual == has.amount)

    def _eval_has_trait(self, has: HasTrait, line: int) -> bv.BarkValue:
        who = self._resolve_subject(has.subject, line)
        ConfigLoader.require_breed(who, line)
        return bv.of(self._has_trait(who, has.trait))

    def _eval_logical(self, logical: Logical, line: int) -> bv.BarkValue:
        left = self._eval(logical.left, line)
        right = self._eval(logical.right, line)
        if logical.op is LogicalOp.AND:
            result = self._truthy(left) and self._truthy(right)
        elif logical.op is LogicalOp.OR:
            result = self._truthy(left) or self._truthy(right)
        else:
            result = not self._truthy(left) and not self._truthy(right)
        return bv.of(result)

    def _eval_binary(self, binary: Binary, line: int) -> bv.BarkValue:
        left = self._eval(binary.left, line)
        right = self._eval(binary.right, line)
        if binary.op is BinaryOp.PLUS:
            if isinstance(left, bv.BarkNumber) and isinstance(right, bv.BarkNumber):
                return bv.of(left.value + right.value)
            return bv.of(bv.describe(left) + bv.describe(right))
        if binary.op is BinaryOp.MINUS:
            return bv.of(self._as_number(left, line) - self._as_number(right, line))
        if binary.op is BinaryOp.STAR:
            return bv.of(self._as_number(left, line) * self._as_number(right, line))
        if binary.op is BinaryOp.SLASH:
            divisor = self._as_number(right, line)
            if divisor == 0:
                raise BarkError(line, "Even dogs know you can't divide by zero.")
            return bv.of(self._as_number(left, line) / divisor)
        raise BarkError(line, "Unknown math operation.")

    def _eval_stash_access(self, access: StashAccess, line: int) -> bv.BarkValue:
        items = self._stash_items(access.stash, line)
        if access.part is StashPart.ALL:
            return bv.of(self._format_list(items))
        if access.part is StashPart.COUNT:
            return bv.of(len(items))
        return items[StashSpots.resolve_index(len(items), access.which or "", line)]

    def _eval_pile_access(self, access: PileAccess, line: int) -> bv.BarkValue:
        items = self._pile_items(access.pile, line)
        if access.part is PilePart.ALL:
            return bv.of(self._format_list(items))
        if access.part is PilePart.COUNT:
            return bv.of(len(items))
        if not items:
            raise BarkError(line, "The pile is flat. Nothing on top.")
        return items[-1]

    def _eval_join(self, join: Join, line: int) -> bv.BarkValue:
        items = self._stash_items(join.stash, line)
        delimiter = self._eval(join.delimiter, line)
        if not isinstance(delimiter, bv.BarkString):
            raise BarkError(line, 'Join needs a word to stick between items. like " and ".')
        parts = []
        for i, item in enumerate(items):
            if i > 0:
                parts.append(delimiter.value)
            parts.append(bv.describe(item))
        return bv.of("".join(parts))

    def _call_trick(self, call: FunctionCall, line: int) -> bv.BarkValue:
        trick = self._tricks.get(ConfigLoader.normalise(call.name))
        if trick is None:
            raise BarkError(line, f'No trick named "{call.name}" in the pack.')
        if len(call.args) != len(trick.params):
            raise BarkError(
                line,
                f'Trick "{call.name}" wants {len(trick.params)} treats, not {len(call.args)}.',
            )
        saved = dict(self._values)
        for i, param in enumerate(trick.params):
            self._values[param] = self._eval(call.args[i], line)
        self._run_steps(trick.steps)
        result = self._eval(trick.return_expression, line)
        self._values.clear()
        self._values.update(saved)
        return result

    def _eval_length(self, length: Length, line: int) -> bv.BarkValue:
        value = self._eval(length.value, line)
        if isinstance(value, bv.BarkString):
            return bv.of(len(value.value))
        raise BarkError(line, "Letters only works on words. not bone counts.")

    def _eval_contains(self, contains: Contains, line: int) -> bv.BarkValue:
        haystack = self._eval(contains.haystack, line)
        needle = self._eval(contains.needle, line)
        if isinstance(haystack, bv.BarkString) and isinstance(needle, bv.BarkString):
            return bv.of(needle.value in haystack.value)
        raise BarkError(line, "Sniffs inside only works on words. not numbers.")

    def _eval_type_check(self, type_check: TypeCheck, line: int) -> bv.BarkValue:
        value = self._eval(type_check.value, line)
        if type_check.type is ValueType.NUMBER:
            result = isinstance(value, bv.BarkNumber)
        elif type_check.type is ValueType.WORDS:
            result = isinstance(value, bv.BarkString)
        else:
            result = isinstance(value, bv.BarkNull)
        return bv.of(result)

    def _lookup_variable(self, name: str, line: int) -> bv.BarkValue:
        key = ConfigLoader.normalise(name)
        if ConfigLoader.is_story_number_constant(key):
            return bv.of(self._memory)
        if ConfigLoader.is_story_text_constant(key):
            return bv.of(self._journal)
        value = self._values.get(key)
        if value is not None:
            return value
        prop = self._props.get(key)
        if prop is not None:
            return prop.value
        raise BarkError(
            line,
            f'No scent of "{name}" anywhere.'
            + NameHints.hint_phrase(name, ConfigLoader.list_objects()),
        )

    @staticmethod
    def _to_memory(value: bv.BarkValue, line: int) -> int:
        if not isinstance(value, bv.BarkNumber):
            raise BarkError(line, "memory only holds a whole number. not words or true/false.")
        amount = value.value
        if (
            math.isnan(amount)
            or math.isinf(amount)
            or math.floor(amount) != amount
            or amount < -32768
            or amount > 32767
        ):
            raise BarkError(line, "memory must be a whole number between -32768 and 32767.")
        return int(amount)

    @staticmethod
    def _to_journal(value: bv.BarkValue, line: int) -> str:
        if isinstance(value, bv.BarkBoolean):
            raise BarkError(line, "journal only holds words. not true/false.")
        if isinstance(value, bv.BarkNull):
            return ""
        return bv.describe(value)

    def _lookup_field(self, subject: str, topic: str, line: int) -> bv.BarkValue:
        who = self._resolve_subject(subject, line)
        field = DogTopics.resolve(topic)
        if field is None:
            obj = Keywords.resolve_object_name(topic)
            if obj is not None:
                return bv.of(self._dog(who).count_object(obj))
            raise BarkError(line, f'No scent of "{subject} {topic}" anywhere.')
        value = self._dog(who).get(field, line)
        if field == DogTopics.NAME and isinstance(value, bv.BarkString) and not value.value:
            direct = self._values.get(who)
            if direct is not None:
                return direct
        return value

    def _dog(self, breed: str) -> Dog:
        return self._dogs.setdefault(ConfigLoader.normalise(breed), Dog.fresh())

    def _resolve_subject_for_mood(self, name: str) -> str | None:
        normalized = ConfigLoader.normalise(name)
        if Keywords.is_feminine_breed_pronoun(normalized):
            return self._last_feminine_breed or self._last_breed
        if Keywords.is_masculine_breed_pronoun(normalized):
            return self._last_masculine_breed or self._last_breed
        if Keywords.is_breed_pronoun_word(normalized):
            return self._last_breed
        if Keywords.is_object_pronoun_word(normalized):
            return None
        if ConfigLoader.type_of(normalized) == VariableType.BREED:
            return normalized
        by_name = self._pet_names.get(normalized)
        if by_name is not None:
            return by_name
        return None

    def _resolve_subject(self, name: str, line: int) -> str:
        normalized = ConfigLoader.normalise(name)
        if Keywords.is_feminine_breed_pronoun(normalized):
            if self._last_feminine_breed is not None:
                return self._last_feminine_breed
            if self._last_breed is not None:
                return self._last_breed
            raise BarkError(
                line, f'The dogs don\'t know who "{name}" is yet. Mention a dog first.'
            )
        if Keywords.is_masculine_breed_pronoun(normalized):
            if self._last_masculine_breed is not None:
                return self._last_masculine_breed
            if self._last_breed is not None:
                return self._last_breed
            raise BarkError(
                line, f'The dogs don\'t know who "{name}" is yet. Mention a dog first.'
            )
        if Keywords.is_breed_pronoun_word(normalized):
            if self._last_breed is None:
                raise BarkError(
                    line, f'The dogs don\'t know who "{name}" is yet. Mention a dog first.'
                )
            return self._last_breed
        if Keywords.is_object_pronoun_word(normalized):
            if self._last_object is None:
                raise BarkError(
                    line,
                    f'Nothing has been pointed at yet. Mention a stash, pile, or object before "{name}".',
                )
            return self._last_object
        by_name = self._pet_names.get(normalized)
        if by_name is not None:
            self._note_subject(by_name)
            self._bind_pronoun_subject(name, by_name)
            return by_name
        if Keywords.is_pet_name_word(normalized) and normalized not in self._pet_names:
            raise BarkError(
                line,
                f'No dog named "{name}" yet. introduce a breed and set her name first.',
            )
        self._note_subject(normalized)
        return normalized

    def _register_pet_name(self, breed: str, value: bv.BarkValue) -> None:
        if not isinstance(value, bv.BarkString):
            return
        key = ConfigLoader.normalise(breed)
        self._pet_names = {k: v for k, v in self._pet_names.items() if v != key}
        label = ConfigLoader.normalise(value.value)
        if not label:
            return
        self._pet_names[label] = key

    def _bind_pronoun_subject(self, subject: str, resolved: str) -> None:
        normalized = ConfigLoader.normalise(subject)
        if Keywords.is_feminine_breed_pronoun(normalized):
            self._last_feminine_breed = resolved
        if Keywords.is_masculine_breed_pronoun(normalized):
            self._last_masculine_breed = resolved

    def _note_subject(self, name: str) -> None:
        normalized = ConfigLoader.normalise(name)
        vtype = ConfigLoader.type_of(normalized)
        if vtype in (VariableType.STORY_NUMBER, VariableType.STORY_TEXT):
            return
        if vtype == VariableType.BREED:
            self._last_breed = normalized
            self._dog(normalized)
        elif vtype in (VariableType.OBJECT, VariableType.STASH, VariableType.PILE):
            self._last_object = normalized

    def _stash_items(self, name: str, line: int) -> list[bv.BarkValue]:
        ConfigLoader.require_stash(name, line)
        key = ConfigLoader.normalise(name)
        if key not in self._stashes:
            self._stashes[key] = []
        return self._stashes[key]

    def _pile_items(self, name: str, line: int) -> list[bv.BarkValue]:
        ConfigLoader.require_pile(name, line)
        key = ConfigLoader.normalise(name)
        if key not in self._piles:
            self._piles[key] = []
        return self._piles[key]

    @staticmethod
    def _format_list(items: list[bv.BarkValue]) -> str:
        if not items:
            return "nothing"
        return ", ".join(bv.describe(item) for item in items)

    @staticmethod
    def _as_number(value: bv.BarkValue, line: int) -> float:
        if isinstance(value, bv.BarkNumber):
            return value.value
        raise BarkError(
            line,
            f"The dogs can't count that. expected a number, got {bv.describe(value)}.",
        )

    @staticmethod
    def _truthy(value: bv.BarkValue | None) -> bool:
        if value is None:
            return False
        if isinstance(value, bv.BarkBoolean):
            return value.value
        if isinstance(value, bv.BarkNumber):
            return value.value != 0
        if isinstance(value, bv.BarkNull):
            return False
        if isinstance(value, bv.BarkString):
            return bool(value.value)
        return False
