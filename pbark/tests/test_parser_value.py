from pbark.config.config_loader import ConfigLoader
from pbark.parser import ast_node as AstNode
from pbark.parser import keywords as Keywords
from pbark.parser.expression.value_parser import ValueParser
from pbark.parser.parse_expression import (
    Binary,
    Field,
    FunctionCall,
    Not,
    NumberLiteral,
    StashAccess,
    StashPart,
    StringLiteral,
    Variable,
)
from pbark.parser.parser import Parser
from pbark.lexer import Lexer
from pbark.options import BarkOptions
from tests.conftest import parse_line, parse_program, run_lines


def test_bark_not_false():
    print_node = parse_line("bark not false")
    assert isinstance(print_node, AstNode.Print)
    not_expr = print_node.values[0]
    assert isinstance(not_expr, Not)
    assert isinstance(not_expr.operand, type(print_node.values[0].operand.__class__)) or isinstance(
        not_expr.operand, type(NumberLiteral(0)).__bases__[0]
    )
    from pbark.parser.parse_expression import BooleanLiteral

    assert isinstance(not_expr.operand, BooleanLiteral)


def test_bark_stash_all():
    print_node = parse_line("bark cookie_jar")
    stash = print_node.values[0]
    assert isinstance(stash, StashAccess)
    assert stash.stash == "cookie_jar"
    assert stash.part is StashPart.ALL


def test_bark_her_cookie_jar():
    print_node = parse_line("bark her cookie jar")
    stash = print_node.values[0]
    assert isinstance(stash, StashAccess)
    assert stash.stash == "cookie_jar"
    assert stash.part is StashPart.ALL


def test_bark_how_many_toys_the_toy_box_has():
    print_node = parse_line("bark how many toys the toy box has")
    stash = print_node.values[0]
    assert isinstance(stash, StashAccess)
    assert stash.stash == "toy_box"
    assert stash.part is StashPart.COUNT


def test_bark_letters():
    print_node = parse_line('bark "woof" letters')
    from pbark.parser.parse_expression import Length

    assert isinstance(print_node.values[0], Length)


def test_story_constant_is_count_of_jar():
    assign = parse_line("memory is count of the items in the cookie jar")
    assert isinstance(assign, AstNode.Assign)
    assert assign.variable == ConfigLoader.memory_name()
    stash = assign.value
    assert isinstance(stash, StashAccess)
    assert stash.stash == "cookie_jar"
    assert stash.part is StashPart.COUNT


def test_story_constant_is_number():
    assign = parse_line("memory is 4")
    assert isinstance(assign.value, NumberLiteral)
    assert assign.value.value == 4.0


def test_story_journal_is_string():
    assign = parse_line('journal is "walk time"')
    assert assign.variable == ConfigLoader.journal_name()
    assert isinstance(assign.value, StringLiteral)
    assert assign.value.value == "walk time"


def test_growl_story_constant_minus_her_treats():
    print_node = parse_line('they growl memory minus how many treats she has, "missing!"')
    assert len(print_node.values) == 2
    math = print_node.values[0]
    assert isinstance(math, Binary)
    assert isinstance(math.left, Variable)
    treats = math.right
    assert isinstance(treats, Field)
    assert treats.subject == "she"
    assert treats.topic == "food"


def test_wife_has_labrador_registers_breed():
    register = parse_line("So, my wife has a labrador")
    assert isinstance(register, AstNode.Assign)
    assert register.variable == "labrador"


def test_stash_word_is_story_glue():
    assert Keywords.is_ignored("stash")
    assert Keywords.is_ignored("pile")
    assert not Keywords.is_ignored("first")
    assert not Keywords.is_ignored("last")


def test_value_parser_skips_glue_after_collection_name():
    parser = Parser(Lexer("cookie_jar stash first\n").tokenise(), BarkOptions.defaults())
    with_glue = ValueParser(parser).parse_part(0, parser.count_tokens_ahead(0))
    parser = Parser(Lexer("cookie_jar first\n").tokenise(), BarkOptions.defaults())
    plain = ValueParser(parser).parse_part(0, parser.count_tokens_ahead(0))
    assert plain == with_glue
    assert isinstance(with_glue, StashAccess)
    assert with_glue.part is StashPart.ELEMENT
    assert with_glue.which == "first"


def test_bark_her_cookie_jar_with_trailing_stash_glue():
    plain = parse_line("bark her cookie jar")
    with_glue = parse_line("bark her cookie jar stash")
    assert plain.values == with_glue.values


def test_bark_stash_glue_before_collection_name():
    print_node = parse_line("bark stash her cookie jar")
    access = print_node.values[0]
    assert isinstance(access, StashAccess)
    assert access.stash == "cookie_jar"
    assert access.part is StashPart.ALL


def test_bark_mixed_comma_separated_values():
    print_node = parse_line('bark "test", memory, 5')
    assert len(print_node.values) == 3
    assert isinstance(print_node.values[0], StringLiteral)
    assert print_node.values[0].value == "test"
    assert isinstance(print_node.values[1], Variable)
    assert print_node.values[1].value == ConfigLoader.memory_name()
    assert isinstance(print_node.values[2], NumberLiteral)
    assert print_node.values[2].value == 5.0


def test_growl_comma_separated_print_list():
    print_node = parse_line('they growl memory, "missing!"')
    assert len(print_node.values) == 2
    assert isinstance(print_node.values[0], Variable)
    assert isinstance(print_node.values[1], StringLiteral)


def test_bark_trick_call_comma_is_not_print_list():
    print_node = parse_line("bark add with 2, 3")
    assert len(print_node.values) == 1
    assert isinstance(print_node.values[0], FunctionCall)


def test_bark_stash_all_prints_comma_separated():
    program = Parser(
        Lexer('cookie_jar holds "a", "b"\nbark her cookie jar\n').tokenise(),
        BarkOptions.defaults(),
    ).parse()
    assert run_lines('cookie_jar holds "a", "b"\nbark her cookie jar\n') == ["a, b"]
