from pbark.parser import ast_node as AstNode
from pbark.parser.parse_expression import Binary, BinaryOp, FunctionCall, NullLiteral, NumberLiteral, StashAccess, StashPart, Variable
from pbark.parser.parser import Parser
from pbark.lexer import Lexer
from pbark.options import BarkOptions
from tests.conftest import parse_line, parse_program, run_lines


def test_cookie_jar_is_empty_clears_stash():
    clear = parse_line("her cookie jar is empty")
    assert isinstance(clear, AstNode.StashClear)
    assert clear.stash == "cookie_jar"


def test_trick_on_one_line_with_bury():
    trick = parse_program("add expects a b returns a bury\n")[0]
    assert isinstance(trick, AstNode.FunctionDef)
    assert trick.name == "add"
    assert trick.params == ["a", "b"]
    assert isinstance(trick.return_expression, Variable)
    assert trick.return_expression.value == "a"
    assert trick.steps == []


def test_trick_on_one_line_with_end():
    trick = parse_program("add expects a b returns a end\n")[0]
    assert isinstance(trick, AstNode.FunctionDef)
    assert trick.name == "add"
    assert trick.steps == []


def test_trick_with_return_value():
    trick = parse_program("add expects a b returns a\nbury\n")[0]
    assert isinstance(trick, AstNode.FunctionDef)
    assert trick.name == "add"
    assert trick.params == ["a", "b"]
    assert isinstance(trick.return_expression, Variable)
    assert trick.return_expression.value == "a"
    assert trick.steps == []


def test_trick_return_expression():
    trick = parse_program("add expects a b returns a plus b bury\n")[0]
    assert isinstance(trick, AstNode.FunctionDef)
    total = trick.return_expression
    assert isinstance(total, Binary)
    assert total.op is BinaryOp.PLUS
    assert isinstance(total.left, Variable)
    assert total.left.value == "a"
    assert isinstance(total.right, Variable)
    assert total.right.value == "b"


def test_bark_trick_call():
    program = Parser(
        Lexer("add expects a b returns a plus b bury\nbark add with 2, 3\n").tokenise(),
        BarkOptions.defaults(),
    ).parse()
    print_node = program.statements[1]
    assert isinstance(print_node, AstNode.Print)
    call = print_node.values[0]
    assert isinstance(call, FunctionCall)
    assert call.name == "add"
    assert len(call.args) == 2
    assert call.args[0].value == 2.0
    assert call.args[1].value == 3.0


def test_bark_trick_call_runs():
    source = "add expects a b returns a plus b bury\nbark add with 2, 3\n"
    assert run_lines(source) == ["5"]


def test_trick_with_body_returns_nothing():
    trick = parse_program('greet expects name returns nothing\n    bark name\nend\n')[0]
    assert isinstance(trick, AstNode.FunctionDef)
    assert trick.name == "greet"
    assert trick.params == ["name"]
    assert isinstance(trick.return_expression, NullLiteral)
    assert len(trick.steps) == 1
    assert isinstance(trick.steps[0], AstNode.Print)


def test_stash_slot_set_with_plain_number():
    set_node = parse_line("cookie_jar first is 5")
    assert isinstance(set_node, AstNode.StashSet)
    assert set_node.stash == "cookie_jar"
    assert set_node.which == "first"
    assert isinstance(set_node.value, NumberLiteral)
    assert set_node.value.value == 5.0


def test_stash_slot_set_with_expression():
    set_node = parse_line("cookie_jar first is count of the items in the cookie jar")
    assert isinstance(set_node, AstNode.StashSet)
    assert set_node.stash == "cookie_jar"
    assert set_node.which == "first"
    count = set_node.value
    assert isinstance(count, StashAccess)
    assert count.part is StashPart.COUNT
