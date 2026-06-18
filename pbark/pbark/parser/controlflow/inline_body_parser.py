from __future__ import annotations

from pbark.errors import BarkError
from pbark.lexer import TokenType
from pbark.parser import ast_node as AstNode
from pbark.parser import keywords as Keywords


class InlineBodyParser:
    @staticmethod
    def parse_then_steps(parser, token_count: int, line: int, empty_error: str):
        if token_count <= 0 or parser.is_at_end_or_line_end_offset(0):
            raise BarkError(line, empty_error)
        first = parser.peek_at(0)
        if first.is_(TokenType.IDENTIFIER) and token_count == 1:
            word = parser.normalise(first.value)
            if word in Keywords.BREAK_KEYWORDS:
                parser.advance_by(1)
                return [AstNode.Break(line)]
            if word in Keywords.CONTINUE_KEYWORDS:
                parser.advance_by(1)
                return [AstNode.Continue(line)]
        if parser.line_has_word(Keywords.PRINT_KEYWORDS):
            return [parser.get_print_parser().parse_print_for_next_tokens(token_count)]
        raise BarkError(line, empty_error)
