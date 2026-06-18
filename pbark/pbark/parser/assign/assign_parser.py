from __future__ import annotations

from pbark.config.config_loader import ConfigLoader
from pbark.config.dog_topics import DogTopics
from pbark.errors import BarkError
from pbark.lexer import TokenType
from pbark.parser import ast_node as AstNode
from pbark.parser import keywords as Keywords
from pbark.parser.expression.value_parser import ValueParser
from pbark.parser.parse_expression import Binary, BinaryOp, Field, NumberLiteral, StringLiteral


class AssignParser:
    def __init__(self, parser) -> None:
        self._parser = parser

    def parse_register_line(self):
        breed = None
        offset = 0
        while not self._parser.is_at_end_or_line_end_offset(offset):
            token = self._parser.peek_at(offset)
            if not token.is_(TokenType.IDENTIFIER):
                offset += 1
                continue
            word = self._parser.normalise(token.value)
            if not ConfigLoader.is_breed(word):
                offset += 1
                continue
            if breed is not None:
                return None
            breed = word
            offset += 1
        if breed is None:
            return None
        line = self._parser.peek().line
        self._parser.consume_until_line_end()
        return AstNode.Assign(breed, NumberLiteral(0), line)

    def parse_leading_object_line(self):
        if not self._parser.peek().is_(TokenType.NUMBER):
            return None
        value = float(self._parser.normalise(self._parser.peek().value))
        obj = self._find_object_after_leading_number()
        if obj is None:
            return None
        line = self._parser.peek().line
        self._parser.consume_until_line_end()
        return AstNode.Assign(obj, NumberLiteral(value), line)

    def _find_object_after_leading_number(self) -> str | None:
        offset = 1
        while not self._parser.is_at_end_or_line_end_offset(offset):
            token = self._parser.peek_at(offset)
            if not token.is_(TokenType.IDENTIFIER):
                offset += 1
                continue
            obj = Keywords.resolve_object_name(self._parser.normalise(token.value))
            if obj is not None:
                return obj
            offset += 1
        return None

    def parse_object_count_line(self):
        obj = None
        value = None
        offset = 0
        while not self._parser.is_at_end_or_line_end_offset(offset):
            token = self._parser.peek_at(offset)
            if obj is None and token.is_(TokenType.IDENTIFIER):
                resolved = Keywords.resolve_object_name(self._parser.normalise(token.value))
                if resolved is not None:
                    obj = resolved
            if value is None and token.is_(TokenType.NUMBER):
                value = float(self._parser.normalise(token.value))
            offset += 1
        if obj is None or value is None:
            return None
        line = self._parser.peek().line
        self._parser.consume_until_line_end()
        return AstNode.Assign(obj, NumberLiteral(value), line)

    def parse_has_line(self):
        who = None
        amount = None
        topic = None
        offset = 0
        while not self._parser.is_at_end_or_line_end_offset(offset):
            token = self._parser.peek_at(offset)
            if who is None and token.is_(TokenType.IDENTIFIER):
                word = self._parser.normalise(token.value)
                if Keywords.is_dog_subject_reference(word, token.value):
                    who = word
            if amount is None and token.is_(TokenType.NUMBER):
                amount = float(self._parser.normalise(token.value))
            if amount is not None and topic is None and token.is_(TokenType.IDENTIFIER):
                word = self._parser.normalise(token.value)
                resolved = Keywords.explicit_attribute_topic(word)
                if resolved is not None:
                    topic = resolved
            offset += 1
        if who is None or amount is None or topic is None:
            return None
        line = self._parser.peek().line
        self._parser.consume_until_line_end()
        return AstNode.SetAttribute(who, topic, NumberLiteral(amount), line)

    def parse_name_line(self):
        name_at = -1
        offset = 0
        while not self._parser.is_at_end_or_line_end_offset(offset):
            token = self._parser.peek_at(offset)
            if not token.is_(TokenType.IDENTIFIER):
                offset += 1
                continue
            if DogTopics.NAME == Keywords.explicit_attribute_topic(
                self._parser.normalise(token.value)
            ):
                name_at = offset
                break
            offset += 1
        if name_at < 0:
            return None
        who = None
        for off in range(name_at):
            token = self._parser.peek_at(off)
            if not token.is_(TokenType.IDENTIFIER):
                continue
            word = self._parser.normalise(token.value)
            if ConfigLoader.is_breed(word) or Keywords.is_pronoun_word(word):
                who = word
                break
        if who is None:
            return None
        value = self._find_name_value_after(name_at)
        if value is None:
            return None
        line = self._parser.peek().line
        self._parser.consume_until_line_end()
        return AstNode.SetAttribute(who, DogTopics.NAME, value, line)

    def _find_name_value_after(self, name_at: int):
        offset = name_at + 1
        while not self._parser.is_at_end_or_line_end_offset(offset):
            token = self._parser.peek_at(offset)
            if token.is_(TokenType.STRING):
                return StringLiteral(token.value)
            if token.is_(TokenType.IDENTIFIER):
                word = self._parser.normalise(token.value)
                if Keywords.is_pet_name_word(word, token.value):
                    return StringLiteral(token.value)
            offset += 1
        return None

    def parse_years_old_line(self):
        number_at = self._find_years_old_number()
        if number_at < 0:
            return None
        who = None
        for off in range(number_at):
            token = self._parser.peek_at(off)
            if not token.is_(TokenType.IDENTIFIER):
                continue
            word = self._parser.normalise(token.value)
            if ConfigLoader.is_breed(word) or Keywords.is_pronoun_word(word):
                who = word
                break
        if who is None:
            return None
        age = float(self._parser.normalise(self._parser.peek_at(number_at).value))
        line = self._parser.peek().line
        self._parser.consume_until_line_end()
        return AstNode.SetAttribute(who, DogTopics.AGE, NumberLiteral(age), line)

    def parse_is_age_line(self):
        who = None
        age = None
        offset = 0
        while not self._parser.is_at_end_or_line_end_offset(offset):
            token = self._parser.peek_at(offset)
            if who is None and token.is_(TokenType.IDENTIFIER):
                word = self._parser.normalise(token.value)
                if Keywords.is_dog_subject_reference(word, token.value):
                    who = word
            if age is None and token.is_(TokenType.NUMBER):
                age = float(self._parser.normalise(token.value))
            offset += 1
        if who is None or age is None:
            return None
        line = self._parser.peek().line
        self._parser.consume_until_line_end()
        return AstNode.SetAttribute(who, DogTopics.AGE, NumberLiteral(age), line)

    def parse_adjust_line(self):
        who = None
        verb_at = -1
        increment = False
        amount = None
        topic_word = None
        offset = 0
        while not self._parser.is_at_end_or_line_end_offset(offset):
            token = self._parser.peek_at(offset)
            if who is None and token.is_(TokenType.IDENTIFIER):
                word = self._parser.normalise(token.value)
                if Keywords.is_dog_subject_reference(word, token.value):
                    who = word
            if verb_at < 0 and token.is_(TokenType.IDENTIFIER):
                word = self._parser.normalise(token.value)
                if Keywords.is_attribute_adjust_decrement(word):
                    verb_at = offset
                    increment = False
                elif Keywords.is_attribute_adjust_increment(word):
                    verb_at = offset
                    increment = True
            if verb_at >= 0 and amount is None and offset > verb_at and token.is_(TokenType.NUMBER):
                amount = float(self._parser.normalise(token.value))
            if verb_at >= 0 and offset > verb_at and token.is_(TokenType.IDENTIFIER):
                word = self._parser.normalise(token.value)
                resolved = Keywords.explicit_attribute_topic(word)
                if resolved is not None:
                    topic_word = word
            offset += 1
        if who is None or verb_at < 0:
            return None
        change_by = amount if amount is not None else 1.0
        verb = self._parser.normalise(self._parser.peek_at(verb_at).value)
        topic = Keywords.resolve_adjust_topic(topic_word, verb)
        field = Field(who, topic)
        change_expr = NumberLiteral(change_by)
        value = (
            Binary(field, BinaryOp.PLUS, change_expr)
            if increment
            else Binary(field, BinaryOp.MINUS, change_expr)
        )
        line = self._parser.peek().line
        self._parser.consume_until_line_end()
        return AstNode.SetAttribute(who, topic, value, line)

    def parse_trait_line(self):
        if self._parser.line_has_word(Keywords.PRINT_KEYWORDS):
            return None
        breed = None
        trait = None
        trait_at = -1
        offset = 0
        while not self._parser.is_at_end_or_line_end_offset(offset):
            token = self._parser.peek_at(offset)
            if not token.is_(TokenType.IDENTIFIER):
                offset += 1
                continue
            word = self._parser.normalise(token.value)
            if breed is None and ConfigLoader.is_breed(word):
                breed = word
            if trait is None and Keywords.is_trait_keyword(word):
                trait = word
                trait_at = offset
            offset += 1
        if breed is None or trait is None:
            return None
        if self._find_number_on_line() is not None:
            return None
        line = self._parser.peek().line
        enabled = self._trait_enabled_on_line(trait_at)
        self._parser.consume_until_line_end()
        return AstNode.SetTrait(breed, trait, enabled, line)

    def _find_years_old_number(self) -> int:
        number_at = -1
        offset = 0
        while not self._parser.is_at_end_or_line_end_offset(offset):
            if self._parser.peek_at(offset).is_(TokenType.NUMBER):
                number_at = offset
                break
            offset += 1
        if number_at < 0:
            return -1
        after_number: list[str] = []
        scan = number_at + 1
        while not self._parser.is_at_end_or_line_end_offset(scan):
            token = self._parser.peek_at(scan)
            if not token.is_(TokenType.IDENTIFIER):
                break
            after_number.append(self._parser.normalise(token.value))
            scan += 1
        return number_at if Keywords.is_years_old_suffix(after_number) else -1

    def _find_number_on_line(self) -> float | None:
        offset = 0
        while not self._parser.is_at_end_or_line_end_offset(offset):
            token = self._parser.peek_at(offset)
            if token.is_(TokenType.NUMBER):
                return float(self._parser.normalise(token.value))
            offset += 1
        return None

    def _trait_enabled_on_line(self, trait_at: int) -> bool:
        for offset in range(trait_at):
            token = self._parser.peek_at(offset)
            if token.is_(TokenType.IDENTIFIER) and self._parser.normalise(token.value) in Keywords.NOT_KEYWORDS:
                return False
        offset = trait_at + 1
        while not self._parser.is_at_end_or_line_end_offset(offset):
            token = self._parser.peek_at(offset)
            if not token.is_(TokenType.IDENTIFIER):
                offset += 1
                continue
            word = self._parser.normalise(token.value)
            if Keywords.is_null_clear_word(word):
                return False
            bool_val = Keywords.parse_boolean(word)
            if bool_val is not None:
                return bool_val
            offset += 1
        return True

    def parse_bare_age_line(self):
        who = None
        age = None
        offset = 0
        while not self._parser.is_at_end_or_line_end_offset(offset):
            token = self._parser.peek_at(offset)
            if who is None and token.is_(TokenType.IDENTIFIER):
                word = self._parser.normalise(token.value)
                if Keywords.is_dog_subject_reference(word, token.value):
                    who = word
            if age is None and token.is_(TokenType.NUMBER):
                age = float(self._parser.normalise(token.value))
            offset += 1
        if who is None or age is None:
            return None
        line = self._parser.peek().line
        self._parser.consume_until_line_end()
        return AstNode.SetAttribute(who, DogTopics.AGE, NumberLiteral(age), line)

    def parse_story_constant_assign_line(self):
        constant = None
        subject_at = -1
        offset = 0
        while not self._parser.is_at_end_or_line_end_offset(offset):
            token = self._parser.peek_at(offset)
            if not token.is_(TokenType.IDENTIFIER):
                offset += 1
                continue
            word = self._parser.normalise(token.value)
            if ConfigLoader.is_story_number_constant(word) or ConfigLoader.is_story_text_constant(word):
                constant = word
                subject_at = offset
                break
            offset += 1
        if constant is None or subject_at < 0:
            return None
        value_start = self._find_offset_after_assign_glue(subject_at + 1)
        if value_start < 0:
            return None
        line = self._parser.peek().line
        value = ValueParser(self._parser).parse_part(value_start, self._parser.count_tokens_ahead(0))
        self._parser.consume_until_line_end()
        return AstNode.Assign(constant, value, line)

    def parse_attribute_expression_assign_line(self):
        if self._parser.line_has_word(Keywords.PRINT_KEYWORDS):
            return None
        who = None
        attribute_at = -1
        topic = None
        offset = 0
        while not self._parser.is_at_end_or_line_end_offset(offset):
            token = self._parser.peek_at(offset)
            if not token.is_(TokenType.IDENTIFIER):
                offset += 1
                continue
            word = self._parser.normalise(token.value)
            if who is None and (ConfigLoader.is_breed(word) or Keywords.is_pronoun_word(word)):
                who = word
            if attribute_at < 0:
                resolved = Keywords.explicit_attribute_topic(word)
                if (
                    resolved is not None
                    and resolved != DogTopics.NAME
                    and not Keywords.is_trait_keyword(word)
                ):
                    attribute_at = offset
                    topic = resolved
            offset += 1
        if who is None or attribute_at < 0 or topic is None:
            return None
        value_start = self._find_offset_after_assign_glue(attribute_at + 1)
        if value_start < 0:
            return None
        line = self._parser.peek().line
        value = ValueParser(self._parser).parse_part(value_start, self._parser.count_tokens_ahead(0))
        self._parser.consume_until_line_end()
        return AstNode.SetAttribute(who, topic, value, line)

    def _find_offset_after_assign_glue(self, from_offset: int) -> int:
        offset = from_offset
        while not self._parser.is_at_end_or_line_end_offset(offset):
            token = self._parser.peek_at(offset)
            if token.is_(TokenType.IDENTIFIER) and Keywords.is_assign_subject_glue(
                self._parser.normalise(token.value)
            ):
                return offset + 1
            offset += 1
        return -1
