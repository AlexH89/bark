from __future__ import annotations

import sys

from pbark.errors import BarkError
from pbark.lexer import Token, TokenType
from pbark.options import BarkOptions
from pbark.parser import ast_node as AstNode
from pbark.parser import keywords as Keywords
from pbark.parser.assign.assign_parser import AssignParser
from pbark.parser.collection.pile_action_parser import PileActionParser
from pbark.parser.collection.pile_parser import PileParser
from pbark.parser.collection.stash_parser import StashParser
from pbark.parser.collection.take_parser import TakeParser
from pbark.parser.controlflow.foreach_parser import ForEachParser
from pbark.parser.controlflow.function_parser import FunctionParser
from pbark.parser.controlflow.if_parser import IfParser
from pbark.parser.controlflow.loop_control_parser import LoopControlParser
from pbark.parser.controlflow.until_parser import UntilParser
from pbark.parser.controlflow.while_parser import WhileParser
from pbark.parser.input.input_parser import InputParser
from pbark.parser.printing.print_parser import PrintParser
from pbark.parser.share.share_parser import ShareParser


class Parser:
    def __init__(self, tokens: list[Token], options: BarkOptions | None = None) -> None:
        self._tokens = tokens
        self._options = options if options is not None else BarkOptions.defaults()
        self._current = 0
        self._print_parser = PrintParser(self)
        self._if_parser = IfParser(self)
        self._while_parser = WhileParser(self)
        self._until_parser = UntilParser(self)
        self._for_each_parser = ForEachParser(self)
        self._loop_control_parser = LoopControlParser(self)
        self._function_parser = FunctionParser(self)
        self._input_parser = InputParser(self)
        self._assign_parser = AssignParser(self)
        self._share_parser = ShareParser(self)
        self._stash_parser = StashParser(self)
        self._pile_parser = PileParser(self)
        self._pile_action_parser = PileActionParser(self)
        self._take_parser = TakeParser(self)

    def parse(self) -> AstNode.Program:
        statements = []
        while not self.is_at_end():
            if self._is_newline():
                self.advance()
                continue
            line = self.peek().line
            node = self._parse_line()
            if isinstance(node, AstNode.LineGroup):
                statements.extend(node.statements)
            elif node is not None:
                statements.append(node)
            else:
                self.consume_until_line_end()
                self._warn_if_unrecognized_line(line)
        return AstNode.Program(statements)

    def _parse_line(self):
        token = self.peek()
        if token.is_(TokenType.EOF) or token.is_(TokenType.NEWLINE):
            return None
        if token.is_(TokenType.NUMBER):
            return self._assign_parser.parse_leading_object_line()
        if token.is_(TokenType.IDENTIFIER):
            return self._parse_word_line()
        return None

    def _parse_word_line(self):
        if self.line_has_word(Keywords.IF_START_WORDS):
            return self._if_parser.parse_if_line()
        if self.line_has_word(Keywords.WHILE_KEYWORDS):
            return self._while_parser.parse_while_line()
        if self.line_has_word(Keywords.UNTIL_KEYWORDS):
            return self._until_parser.parse_until_line()
        if self.current_line_starts_with_any(Keywords.FOR_WORDS):
            return self._for_each_parser.parse_for_each_line()
        loop_break = self._loop_control_parser.parse_break_line()
        if loop_break is not None:
            return loop_break
        loop_continue = self._loop_control_parser.parse_continue_line()
        if loop_continue is not None:
            return loop_continue
        trick = self._function_parser.parse_trick_line()
        if trick is not None:
            return trick
        exit_node = self._input_parser.parse_exit_line()
        if exit_node is not None:
            return exit_node
        wait = self._input_parser.parse_wait_line()
        if wait is not None:
            return wait
        listen = self._input_parser.parse_listen_line()
        if listen is not None:
            return listen
        pass_node = self._share_parser.parse_pass_line()
        if pass_node is not None:
            return pass_node
        share = self._share_parser.parse_share_line()
        if share is not None:
            return share
        stash = self._stash_parser.parse_stash_line()
        if stash is not None:
            return stash
        pile_pop = self._pile_action_parser.parse_grab_top_line()
        if pile_pop is not None:
            return pile_pop
        pile_push = self._pile_action_parser.parse_push_into_line()
        if pile_push is not None:
            return pile_push
        take = self._take_parser.parse_take_line()
        if take is not None:
            return take
        pile = self._pile_parser.parse_pile_line()
        if pile is not None:
            return pile
        adjust = self._assign_parser.parse_adjust_line()
        if adjust is not None:
            return adjust
        has_assign = self._assign_parser.parse_has_line()
        if has_assign is not None:
            return has_assign
        object_count = self._assign_parser.parse_object_count_line()
        if object_count is not None:
            return object_count
        name_assign = self._assign_parser.parse_name_line()
        if name_assign is not None:
            return name_assign
        if self.line_has_word(Keywords.PRINT_KEYWORDS):
            return self._print_parser.parse_print_line()
        trait = self._assign_parser.parse_trait_line()
        if trait is not None:
            return trait
        years_old = self._assign_parser.parse_years_old_line()
        if years_old is not None:
            return years_old
        is_age = self._assign_parser.parse_is_age_line()
        if is_age is not None:
            return is_age
        bare_age = self._assign_parser.parse_bare_age_line()
        if bare_age is not None:
            return bare_age
        story_constant = self._assign_parser.parse_story_constant_assign_line()
        if story_constant is not None:
            return story_constant
        attribute_expr = self._assign_parser.parse_attribute_expression_assign_line()
        if attribute_expr is not None:
            return attribute_expr
        register = self._assign_parser.parse_register_line()
        if register is not None:
            return register
        return None

    def line_has_word(self, words: list[str]) -> bool:
        offset = 0
        while not self.is_at_end_or_line_end_offset(offset):
            token = self.peek_at(offset)
            if token.is_(TokenType.IDENTIFIER) and self.normalise(token.value) in words:
                return True
            offset += 1
        return False

    def _warn_if_unrecognized_line(self, line: int) -> None:
        if self._options.strict:
            print(
                f"Line {line}: the dogs didn't catch that line (story-only words?).",
                file=sys.stderr,
            )

    def peek(self) -> Token:
        return self._tokens[self._current]

    def peek_at(self, offset: int) -> Token:
        index = self._current + offset
        if index >= len(self._tokens):
            return self._tokens[-1]
        return self._tokens[index]

    def advance(self) -> Token:
        token = self._tokens[self._current]
        self._current += 1
        return token

    def advance_by(self, count: int) -> None:
        self._current += count

    def is_at_end(self) -> bool:
        return self.peek().is_(TokenType.EOF)

    def _is_newline(self) -> bool:
        return self.peek().is_(TokenType.NEWLINE)

    def is_at_end_or_line_end_offset(self, offset: int) -> bool:
        token = self.peek_at(offset)
        return token.is_(TokenType.NEWLINE) or token.is_(TokenType.EOF)

    def count_tokens_ahead(self, start_offset_from_cursor: int) -> int:
        count = 0
        while not self.is_at_end_or_line_end_offset(start_offset_from_cursor + count):
            count += 1
        return count

    def skip_newlines(self) -> None:
        while not self.is_at_end() and self._is_newline():
            self.advance()

    def current_line_starts_with(self, word: str) -> bool:
        token = self.peek()
        return token.is_(TokenType.IDENTIFIER) and word == self.normalise(token.value)

    def current_line_starts_with_otherwise(self) -> bool:
        return self.current_line_starts_with_any(
            Keywords.OTHERWISE_WORDS
        ) or self.current_line_starts_with_any(Keywords.ELSE_WORDS)

    def current_line_is_otherwise_when(self) -> bool:
        if not self.current_line_starts_with_otherwise():
            return False
        offset = 1
        while not self.is_at_end_or_line_end_offset(offset):
            token = self.peek_at(offset)
            if token.is_(TokenType.IDENTIFIER) and Keywords.is_if_start(
                self.normalise(token.value)
            ):
                return True
            offset += 1
        return False

    def current_line_is_bury(self) -> bool:
        return self.current_line_starts_with_any(Keywords.BURY_KEYWORDS)

    def current_line_is_multiline_if_stop(self) -> bool:
        return self.current_line_starts_with_otherwise() or self.current_line_is_bury()

    def current_line_starts_with_any(self, words: list[str]) -> bool:
        token = self.peek()
        if not token.is_(TokenType.IDENTIFIER):
            return False
        return self.normalise(token.value) in words

    def parse_statement(self):
        return self._parse_line()

    def parse_statement_into(self, steps: list) -> None:
        stmt = self.parse_statement()
        if isinstance(stmt, AstNode.LineGroup):
            steps.extend(stmt.statements)
        elif stmt is not None:
            steps.append(stmt)
        else:
            self.consume_until_line_end()

    def consume_bury_line(self) -> None:
        if self.current_line_is_bury():
            self.consume_until_line_end()
        self.skip_newlines()

    def consume_until_line_end(self) -> None:
        while not self.is_at_end():
            self.advance()
            if self._is_newline():
                return

    def normalise(self, value: str) -> str:
        return value.strip().lower()

    def skip_ignored_identifiers(self, from_: int, to: int) -> int:
        pos = from_
        while pos < to:
            token = self.peek_at(pos)
            if not token.is_(TokenType.IDENTIFIER):
                break
            word = self.normalise(token.value)
            if not Keywords.is_ignored(word):
                break
            pos += 1
        return pos

    def get_print_parser(self) -> PrintParser:
        return self._print_parser

    def cursor_index(self) -> int:
        return self._current

    def set_current(self, index: int) -> None:
        self._current = index

    def all_tokens(self) -> list[Token]:
        return self._tokens

    def line_end_index(self) -> int:
        for i in range(self._current, len(self._tokens)):
            if self._tokens[i].is_(TokenType.NEWLINE):
                return i
        return len(self._tokens)
