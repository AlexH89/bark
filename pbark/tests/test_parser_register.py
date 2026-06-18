from pbark.parser import ast_node as AstNode
from pbark.parser.parse_expression import NumberLiteral, StringLiteral
from tests.conftest import parse_line


def test_bare_breed_registers():
    program = parse_line("labrador")
    assert isinstance(program, AstNode.Assign)
    assert program.variable == "labrador"


def test_story_introduce_breed():
    node = parse_line("I have a labrador")
    assert isinstance(node, AstNode.Assign)
    assert node.variable == "labrador"


def test_possessive_introduce_breed():
    node = parse_line("My wife has a dachshund")
    assert isinstance(node, AstNode.Assign)
    assert node.variable == "dachshund"


def test_pronoun_introduce_breed():
    node = parse_line("she has a labrador")
    assert isinstance(node, AstNode.Assign)
    assert node.variable == "labrador"


def test_quoted_name():
    name = parse_line('Her name is "Bimba"')
    assert isinstance(name, AstNode.SetAttribute)
    assert name.subject == "her"
    assert name.topic == "name"
    assert isinstance(name.value, StringLiteral)
    assert name.value.value == "Bimba"


def test_toy_count_beats_age():
    toys = parse_line("she is 3 toys")
    assert isinstance(toys, AstNode.SetAttribute)
    assert toys.subject == "she"
    assert toys.topic == "items"
    assert isinstance(toys.value, NumberLiteral)
    assert toys.value.value == 3.0
