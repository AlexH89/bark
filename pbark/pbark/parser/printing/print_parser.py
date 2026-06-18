from __future__ import annotations

from dataclasses import dataclass

from pbark.config.config_loader import ConfigLoader
from pbark.errors import BarkError
from pbark.lexer import TokenType
from pbark.parser import ast_node as AstNode
from pbark.parser import keywords as Keywords
from pbark.parser.collection import stash_spots as StashSpots
from pbark.parser.expression.value_parser import ValueParser
from pbark.parser.parse_expression import (
    Empty,
    Field,
    HasTrait,
    Not,
    NumberLiteral,
    ParseExpression,
    PileAccess,
    PilePart,
    StashAccess,
    StashPart,
    StringLiteral,
)
from pbark.parser.printing.print_verb import PrintVerb
from pbark.print_style import PrintStyle


@dataclass
class _PrintParts:
    style: PrintStyle | None
    values: list[ParseExpression] | None
    dog_speaker: str | None


class PrintParser:
    def __init__(self, parser) -> None:
        self._parser = parser
        self._value_parser = ValueParser(parser)

    def parse_print_line(self):
        line = self._parser.peek().line
        triple_style = self._find_triple_print_style()
        if triple_style is not None:
            self._parser.consume_until_line_end()
            return AstNode.Print(triple_style, [], line, True, None)
        return self.parse_print_for_next_tokens(self._parser.count_tokens_ahead(0))

    def parse_print_for_next_tokens(self, token_count: int):
        parts = self._find_print_parts_in_next_tokens(token_count)
        node = self._to_print_node(parts, self._parser.peek().line)
        self._parser.advance_by(token_count)
        return node

    def _to_print_node(self, parts: _PrintParts, line: int):
        values = parts.values
        if not values:
            values = [Empty()]
        return AstNode.Print(
            parts.style if parts.style is not None else PrintStyle.BARK,
            values,
            line,
            False,
            parts.dog_speaker,
        )

    def _find_print_parts_in_next_tokens(self, token_count: int) -> _PrintParts:
        print_values: list[ParseExpression] | None = None
        print_verb = PrintVerb(None, -1)
        offset = 0
        while offset < token_count and not self._parser.is_at_end_or_line_end_offset(offset):
            token = self._parser.peek_at(offset)
            self._capture_print_verb(print_verb, token, offset)
            if (
                print_values is None
                and print_verb.offset >= 0
                and offset == print_verb.offset + 1
            ):
                lone_value = offset + 1 >= token_count
                if lone_value and token.is_(TokenType.STRING):
                    print_values = [StringLiteral(token.value)]
                elif lone_value and token.is_(TokenType.NUMBER):
                    print_values = [NumberLiteral(float(self._parser.normalise(token.value)))]
            if print_verb.offset >= 0 and print_values is not None:
                break
            offset += 1
        if print_values is None and print_verb.offset >= 0:
            after_verb = print_verb.offset
            if self._has_math_after_verb(after_verb, token_count):
                print_values = self._find_values_after_verb(after_verb, token_count)
            else:
                phrase = self._find_how_many_items_in_collection(after_verb, token_count)
                if phrase is None:
                    phrase = self._find_top_of_pile(after_verb, token_count)
                if phrase is None:
                    phrase = self._find_stash_slot_from_jar(after_verb, token_count)
                if phrase is None:
                    phrase = self._find_trait_check(after_verb, token_count)
                if phrase is None:
                    phrase = self._find_how_many_topic_in_collection(after_verb, token_count)
                if phrase is None:
                    phrase = self._find_how_many_field_after_verb(after_verb, token_count)
                if phrase is None:
                    phrase = self._find_collection_after_verb(after_verb, token_count)
                if phrase is None:
                    phrase = self._find_field_after_verb(after_verb, token_count)
                if phrase is not None:
                    print_values = [phrase]
                elif self._should_use_value_after_verb(after_verb, token_count):
                    print_values = self._find_values_after_verb(after_verb, token_count)
        dog_speaker = self._find_dog_speaker_before_print_verb(print_verb.offset)
        return _PrintParts(print_verb.style, print_values, dog_speaker)

    def _find_how_many_items_in_collection(self, print_offset: int, token_count: int):
        for offset in range(print_offset + 1, token_count - 3):
            if not self._word_at(offset, Keywords.HOW_WORDS):
                continue
            if not self._word_at(offset + 1, Keywords.MANY_WORDS):
                continue
            if not self._word_at(offset + 2, Keywords.ITEMS_WORDS):
                continue
            if not self._word_at(offset + 3, Keywords.IN_WORDS):
                continue
            name = ConfigLoader.resolve_collection_from_tokens(
                self._parser.all_tokens(),
                self._parser.cursor_index() + offset + 4,
                self._parser.line_end_index(),
            )
            if name is None:
                return None
            if ConfigLoader.is_stash(name):
                return StashAccess(name, StashPart.COUNT)
            if ConfigLoader.is_pile(name):
                return PileAccess(name, PilePart.COUNT)
        return None

    def _find_top_of_pile(self, print_offset: int, token_count: int):
        for offset in range(print_offset + 1, token_count):
            if not self._word_at(offset, Keywords.TOP_WORDS):
                continue
            name_start = offset + 1
            if name_start < token_count and self._word_at(name_start, Keywords.OF_WORDS):
                name_start += 1
            name = ConfigLoader.resolve_collection_from_tokens(
                self._parser.all_tokens(),
                self._parser.cursor_index() + name_start,
                self._parser.line_end_index(),
            )
            if name is not None and ConfigLoader.is_pile(name):
                return PileAccess(name, PilePart.TOP)
        return None

    def _find_stash_slot_from_jar(self, print_offset: int, token_count: int):
        for offset in range(print_offset + 1, token_count):
            token = self._parser.peek_at(offset)
            if not token.is_(TokenType.IDENTIFIER):
                continue
            spot = self._parser.normalise(token.value)
            if not StashSpots.is_stash_spot(spot):
                continue
            from_at = self._find_from_after(offset + 1, token_count)
            if from_at < 0:
                continue
            stash = ConfigLoader.resolve_collection_from_tokens(
                self._parser.all_tokens(),
                self._parser.cursor_index() + from_at + 1,
                self._parser.line_end_index(),
            )
            if stash is not None and ConfigLoader.is_stash(stash):
                return StashAccess(stash, StashPart.ELEMENT, spot)
        return None

    def _find_trait_check(self, print_offset: int, token_count: int):
        breed = None
        trait_at = -1
        for offset in range(print_offset + 1, token_count):
            token = self._parser.peek_at(offset)
            if breed is None and token.is_(TokenType.IDENTIFIER):
                word = self._parser.normalise(token.value)
                if ConfigLoader.is_breed(word):
                    breed = word
            if trait_at < 0 and token.is_(TokenType.IDENTIFIER) and Keywords.is_trait_keyword(
                self._parser.normalise(token.value)
            ):
                trait_at = offset
            if token.is_(TokenType.NUMBER):
                return None
        if breed is None or trait_at < 0:
            return None
        check = HasTrait(breed, self._parser.normalise(self._parser.peek_at(trait_at).value))
        if self._has_not_before(trait_at, print_offset + 1):
            return Not(check)
        return check

    def _find_from_after(self, start: int, token_count: int) -> int:
        for offset in range(start, token_count):
            if self._word_at(offset, Keywords.FROM_WORDS):
                return offset
        return -1

    def _has_not_before(self, trait_at: int, start: int) -> bool:
        for offset in range(start, trait_at):
            if self._word_at(offset, Keywords.NOT_KEYWORDS):
                return True
        return False

    def _word_at(self, offset: int, words: list[str]) -> bool:
        token = self._parser.peek_at(offset)
        return token.is_(TokenType.IDENTIFIER) and self._parser.normalise(token.value) in words

    def _find_values_after_verb(self, print_offset: int, token_count: int) -> list[ParseExpression]:
        start = self._parser.skip_ignored_identifiers(print_offset + 1, token_count)
        if self._looks_like_trick_call_after_verb(start, token_count):
            return [self._value_parser.parse_part(start, token_count)]
        if self._has_comma_after_verb(start, token_count):
            return self._value_parser.parse_comma_separated(start, token_count)
        return [self._value_parser.parse_part(start, token_count)]

    def _looks_like_trick_call_after_verb(self, start: int, token_count: int) -> bool:
        for offset in range(start, token_count - 1):
            token = self._parser.peek_at(offset)
            if token.is_(TokenType.IDENTIFIER) and self._word_at(offset + 1, Keywords.WITH_WORDS):
                return True
        return False

    def _has_math_after_verb(self, print_offset: int, token_count: int) -> bool:
        for offset in range(print_offset + 1, token_count):
            token = self._parser.peek_at(offset)
            if token.type in (
                TokenType.PLUS,
                TokenType.MINUS,
                TokenType.STAR,
                TokenType.SLASH,
            ):
                return True
            if token.is_(TokenType.IDENTIFIER):
                word = self._parser.normalise(token.value)
                if (
                    Keywords.is_math_add_keyword(word)
                    or Keywords.is_math_subtract_keyword(word)
                    or word in Keywords.STAR_KEYWORDS
                    or word in Keywords.SLASH_KEYWORDS
                ):
                    return True
        return False

    def _has_comma_after_verb(self, start: int, token_count: int) -> bool:
        for offset in range(start, token_count):
            if self._parser.peek_at(offset).is_(TokenType.COMMA):
                return True
        return False

    def _should_use_value_after_verb(self, print_offset: int, token_count: int) -> bool:
        for offset in range(print_offset + 1, token_count):
            token = self._parser.peek_at(offset)
            if token.is_(TokenType.IDENTIFIER) and Keywords.is_ignored(
                self._parser.normalise(token.value)
            ):
                continue
            if self._token_looks_like_expression_value(token, offset):
                return True
        return False

    def _token_looks_like_expression_value(self, token, offset: int) -> bool:
        if token.type in (TokenType.PLUS, TokenType.MINUS, TokenType.STAR, TokenType.SLASH, TokenType.NUMBER, TokenType.STRING):
            return True
        if token.is_(TokenType.IDENTIFIER):
            return self._identifier_looks_like_expression_value(token, offset)
        return False

    def _identifier_looks_like_expression_value(self, token, offset: int) -> bool:
        word = self._parser.normalise(token.value)
        if Keywords.is_print_expression_cue(word):
            return True
        if ConfigLoader.is_valid_name(word):
            return True
        if ConfigLoader.is_stash(word) or ConfigLoader.is_pile(word):
            return True
        return (
            ConfigLoader.resolve_collection_from_tokens(
                self._parser.all_tokens(),
                self._parser.cursor_index() + offset,
                self._parser.line_end_index(),
            )
            is not None
        )

    def _find_field_after_verb(self, print_offset: int, token_count: int):
        who = None
        what = None
        for offset in range(print_offset + 1, token_count):
            token = self._parser.peek_at(offset)
            if not token.is_(TokenType.IDENTIFIER):
                continue
            word = self._parser.normalise(token.value)
            if Keywords.is_attribute_keyword(word):
                what = Keywords.resolve_attribute_topic(word)
            elif Keywords.is_dog_subject_reference(word, token.value):
                who = word
        if who is not None and what is not None:
            return Field(who, what)
        return None

    def _find_collection_after_verb(self, print_offset: int, token_count: int):
        start = self._parser.skip_ignored_identifiers(print_offset + 1, token_count)
        if start >= token_count or self._word_at(start, Keywords.HOW_WORDS):
            return None
        name = ConfigLoader.resolve_collection_from_tokens(
            self._parser.all_tokens(),
            self._parser.cursor_index() + start,
            self._parser.line_end_index(),
        )
        if name is None:
            return None
        if ConfigLoader.is_stash(name):
            return StashAccess(name, StashPart.ALL)
        if ConfigLoader.is_pile(name):
            return PileAccess(name, PilePart.ALL)
        return None

    def _find_how_many_topic_in_collection(self, print_offset: int, token_count: int):
        for offset in range(print_offset + 1, token_count - 2):
            if not self._word_at(offset, Keywords.HOW_WORDS):
                continue
            if not self._word_at(offset + 1, Keywords.MANY_WORDS):
                continue
            topic_offset = offset + 2
            if topic_offset >= token_count:
                return None
            topic_token = self._parser.peek_at(topic_offset)
            if not topic_token.is_(TokenType.IDENTIFIER):
                continue
            if Keywords.explicit_attribute_topic(self._parser.normalise(topic_token.value)) is None:
                continue
            name = ConfigLoader.resolve_collection_from_tokens(
                self._parser.all_tokens(),
                self._parser.cursor_index() + topic_offset + 1,
                self._parser.line_end_index(),
            )
            if name is None:
                continue
            if ConfigLoader.is_stash(name):
                return StashAccess(name, StashPart.COUNT)
            if ConfigLoader.is_pile(name):
                return PileAccess(name, PilePart.COUNT)
        return None

    def _find_how_many_field_after_verb(self, print_offset: int, token_count: int):
        for offset in range(print_offset + 1, token_count - 1):
            token = self._parser.peek_at(offset)
            if not token.is_(TokenType.IDENTIFIER):
                continue
            word = self._parser.normalise(token.value)
            if word not in Keywords.HOW_WORDS:
                continue
            many_token = self._parser.peek_at(offset + 1)
            if not many_token.is_(TokenType.IDENTIFIER):
                continue
            if self._parser.normalise(many_token.value) not in Keywords.MANY_WORDS:
                continue
            topic_offset = offset + 2
            if topic_offset >= token_count:
                return None
            topic_token = self._parser.peek_at(topic_offset)
            if not topic_token.is_(TokenType.IDENTIFIER):
                return None
            topic = Keywords.explicit_attribute_topic(self._parser.normalise(topic_token.value))
            if topic is None:
                continue
            who_start = topic_offset + 1
            if who_start >= token_count:
                return None
            who = ConfigLoader.resolve_name_from_tokens(
                self._parser.all_tokens(),
                self._parser.cursor_index() + who_start,
                self._parser.line_end_index(),
            )
            if who is None:
                who_token = self._parser.peek_at(who_start)
                if not who_token.is_(TokenType.IDENTIFIER):
                    return None
                who_word = self._parser.normalise(who_token.value)
                if Keywords.is_dog_subject_reference(who_word, who_token.value):
                    who = who_word
            if who is not None and (
                ConfigLoader.is_valid_name(who) or Keywords.is_dog_subject_reference(who)
            ):
                return Field(who, topic)
        return None

    def _capture_print_verb(self, print_verb: PrintVerb, token, offset: int) -> None:
        if not token.is_(TokenType.IDENTIFIER):
            return
        word = self._parser.normalise(token.value)
        if word not in Keywords.PRINT_KEYWORDS:
            return
        if print_verb.offset >= 0:
            return
        print_verb.style = Keywords.PRINT_STYLES.get(word, PrintStyle.BARK)
        print_verb.offset = offset

    def _find_dog_speaker_before_print_verb(self, print_offset: int) -> str | None:
        if print_offset <= 0:
            return None
        for offset in range(print_offset):
            token = self._parser.peek_at(offset)
            if not token.is_(TokenType.IDENTIFIER):
                continue
            word = self._parser.normalise(token.value)
            if Keywords.is_dog_subject_reference(word, token.value):
                return word
        return None

    def _find_triple_print_style(self) -> PrintStyle | None:
        print_word = None
        print_count = 0
        offset = 0
        while not self._parser.is_at_end_or_line_end_offset(offset):
            token = self._parser.peek_at(offset)
            if not token.is_(TokenType.IDENTIFIER):
                offset += 1
                continue
            word = self._parser.normalise(token.value)
            if word in Keywords.PRINT_KEYWORDS:
                if print_word is not None and print_word != word:
                    return None
                print_word = word
                print_count += 1
            elif Keywords.is_heard(word):
                return None
            offset += 1
        if print_count >= 3 and print_word is not None:
            return Keywords.PRINT_STYLES.get(print_word, PrintStyle.BARK)
        return None
