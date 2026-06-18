from __future__ import annotations

from dataclasses import dataclass

from pbark.config.config_loader import ConfigLoader
from pbark.errors import BarkError
from pbark.lexer import TokenType
from pbark.parser import keywords as Keywords
from pbark.parser.expression.comparison_op import ComparisonOp
from pbark.parser.parse_expression import (
    Comparison,
    Field,
    HasExact,
    HasTrait,
    Logical,
    LogicalOp,
    Not,
    NumberLiteral,
    ParseExpression,
)


@dataclass
class _CountAndTopic:
    amount: float
    topic: str


class ConditionParser:
    def __init__(self, parser) -> None:
        self._parser = parser

    def parse_condition(self, start: int, end: int, line: int) -> ParseExpression:
        if self._word_at(start, Keywords.NEITHER_WORDS):
            return self._parse_neither_condition(start, end, line)
        return self._parse_and_joined_tests(start, end, line)

    def _parse_neither_condition(self, start: int, end: int, line: int) -> ParseExpression:
        nor_at = self._find_keyword_index(start + 1, end, Keywords.NOR_WORDS)
        if nor_at < 0:
            raise BarkError(
                line,
                "This command without nor really confused the dogs... They are doing random stuff now!",
            )
        first_dog = self._find_breed_name(start + 1, nor_at)
        second_dog = self._find_breed_name(nor_at + 1, end)
        count = self._read_count_and_topic(nor_at + 1, end)
        if first_dog is None or second_dog is None or count is None:
            raise BarkError(
                line, "Either one of the dogs or the toy is missing! Did you look around?"
            )
        first_has = HasExact(first_dog, count.topic, count.amount)
        second_has = HasExact(second_dog, count.topic, count.amount)
        return Logical(first_has, LogicalOp.NOR, second_has)

    def _parse_and_joined_tests(self, start: int, end: int, line: int) -> ParseExpression:
        combined: ParseExpression | None = None
        test_start = start
        while test_start < end:
            and_at = self._find_keyword_index(test_start, end, Keywords.CONDITION_AND_WORDS)
            test_end = and_at if and_at >= 0 else end
            one_test = self._parse_one_test(test_start, test_end, line)
            if combined is None:
                combined = one_test
            else:
                combined = Logical(combined, LogicalOp.AND, one_test)
            if and_at < 0:
                return combined
            test_start = and_at + 1
        return combined  # type: ignore[return-value]

    def _parse_one_test(self, start: int, end: int, line: int) -> ParseExpression:
        test = self._match_more_than_count(start, end)
        if test is not None:
            return test
        test = self._match_sniffs_less_than(start, end)
        if test is not None:
            return test
        test = self._match_trait_check(start, end)
        if test is not None:
            return test
        test = self._match_exact_count(start, end)
        if test is not None:
            return test
        raise BarkError(
            line,
            "The dogs tilted their heads at you as they have no clue what you want them to do!",
        )

    def _match_more_than_count(self, start: int, end: int) -> ParseExpression | None:
        for offset in range(start, end - 1):
            if not self._is_more_than_phrase(offset):
                continue
            who = self._find_dog_name(start, offset)
            limit: float | None = None
            topic: str | None = None
            for scan in range(offset + 2, end):
                token = self._at(scan)
                if limit is None and token.is_(TokenType.NUMBER):
                    limit = float(self._parser.normalise(token.value))
                elif limit is not None and topic is None and token.is_(TokenType.IDENTIFIER):
                    resolved = Keywords.explicit_attribute_topic(
                        self._parser.normalise(token.value)
                    )
                    if resolved is not None:
                        topic = resolved
            if who is None or limit is None or topic is None:
                return None
            compare = Comparison(Field(who, topic), ComparisonOp.GREATER_THAN, NumberLiteral(limit))
            if self._is_negated_before_count(start, end):
                return Not(compare)
            return compare
        return None

    def _match_sniffs_less_than(self, start: int, end: int) -> ParseExpression | None:
        sniffs_at = self._find_keyword_index(start, end, Keywords.SNIFFS_WORDS)
        if sniffs_at < 0:
            return None
        who = self._find_dog_name(start, sniffs_at)
        scan = sniffs_at + 1
        if scan >= end or not self._word_at(scan, Keywords.COMPARISON_LESS_WORDS):
            return None
        scan += 1
        if scan >= end or not self._at(scan).is_(TokenType.IDENTIFIER):
            return None
        topic = Keywords.explicit_attribute_topic(self._parser.normalise(self._at(scan).value))
        if topic is None:
            return None
        scan += 1
        if scan >= end or not self._word_at(scan, Keywords.THAN_WORDS):
            return None
        scan += 1
        if scan >= end or not self._at(scan).is_(TokenType.NUMBER):
            return None
        if who is None:
            return None
        limit = float(self._parser.normalise(self._at(scan).value))
        compare = Comparison(Field(who, topic), ComparisonOp.LESS_THAN, NumberLiteral(limit))
        if self._is_negated_before_count(start, end):
            return Not(compare)
        return compare

    def _match_trait_check(self, start: int, end: int) -> ParseExpression | None:
        breed = self._find_breed_name(start, end)
        trait_at = self._find_trait_word(start, end)
        if breed is None or trait_at < 0 or self._has_number_in_range(start, end):
            return None
        trait = self._parser.normalise(self._at(trait_at).value)
        check = HasTrait(breed, trait)
        if self._is_negated_before(trait_at, start):
            return Not(check)
        return check

    def _match_exact_count(self, start: int, end: int) -> ParseExpression | None:
        who = self._find_dog_name(start, end)
        count = self._read_count_and_topic(start, end)
        if who is None or count is None:
            return None
        exact = HasExact(who, count.topic, count.amount)
        if self._is_negated_before_count(start, end):
            return Not(exact)
        return exact

    def _read_count_and_topic(self, start: int, end: int) -> _CountAndTopic | None:
        amount: float | None = None
        topic: str | None = None
        for offset in range(start, end):
            token = self._at(offset)
            if amount is None and token.is_(TokenType.NUMBER):
                amount = float(self._parser.normalise(token.value))
                continue
            if amount is not None and topic is None and token.is_(TokenType.IDENTIFIER):
                resolved = Keywords.explicit_attribute_topic(
                    self._parser.normalise(token.value)
                )
                if Keywords.is_attribute_keyword(resolved):
                    topic = resolved
        if amount is None or topic is None:
            return None
        return _CountAndTopic(amount, topic)

    def _is_negated_before_count(self, start: int, end: int) -> bool:
        for offset in range(start, end):
            if self._at(offset).is_(TokenType.NUMBER):
                return False
            if self._word_at(offset, Keywords.NOT_KEYWORDS):
                return True
        return False

    def _is_negated_before(self, before: int, start: int) -> bool:
        for offset in range(start, before):
            if self._word_at(offset, Keywords.NOT_KEYWORDS):
                return True
        return False

    def _find_keyword_index(self, from_: int, end: int, words: list[str]) -> int:
        for offset in range(from_, end):
            if self._word_at(offset, words):
                return offset
        return -1

    def _find_dog_name(self, start: int, end: int) -> str | None:
        for offset in range(start, end):
            token = self._at(offset)
            if not token.is_(TokenType.IDENTIFIER):
                continue
            word = self._parser.normalise(token.value)
            if Keywords.is_dog_subject_reference(word, token.value):
                return word
        return None

    def _find_breed_name(self, start: int, end: int) -> str | None:
        for offset in range(start, end):
            token = self._at(offset)
            if not token.is_(TokenType.IDENTIFIER):
                continue
            word = self._parser.normalise(token.value)
            if ConfigLoader.is_breed(word):
                return word
        return None

    def _find_trait_word(self, start: int, end: int) -> int:
        for offset in range(start, end):
            token = self._at(offset)
            if token.is_(TokenType.IDENTIFIER) and Keywords.is_trait_keyword(
                self._parser.normalise(token.value)
            ):
                return offset
        return -1

    def _has_number_in_range(self, start: int, end: int) -> bool:
        for offset in range(start, end):
            if self._at(offset).is_(TokenType.NUMBER):
                return True
        return False

    def _is_more_than_phrase(self, offset: int) -> bool:
        return self._word_at(offset, Keywords.COMPARISON_GREATER_WORDS) and self._word_at(
            offset + 1, Keywords.THAN_WORDS
        )

    def _word_at(self, offset: int, words: list[str]) -> bool:
        token = self._at(offset)
        return token.is_(TokenType.IDENTIFIER) and self._parser.normalise(token.value) in words

    def _at(self, offset: int):
        return self._parser.peek_at(offset)
