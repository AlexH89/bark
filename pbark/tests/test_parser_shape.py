from pbark.parser import ast_node as AstNode
from pbark.parser.parse_expression import Field, NumberLiteral, StringLiteral
from pbark.print_style import PrintStyle
from tests.conftest import parse_line


def test_belongings_are_sets_inventory():
    inventory = parse_line('my labrador belongings are "ball, stick"')
    assert isinstance(inventory, AstNode.SetAttribute)
    assert inventory.subject == "labrador"
    assert inventory.topic == "inventory"
    assert isinstance(inventory.value, StringLiteral)
    assert inventory.value.value == "ball, stick"


def test_finds_adds_one_toy():
    adjust = parse_line("he finds a toy")
    assert isinstance(adjust, AstNode.SetAttribute)
    assert adjust.subject == "he"
    assert adjust.topic == "items"
    from pbark.parser.parse_expression import Binary

    assert isinstance(adjust.value, Binary)


def test_years_old_sets_age():
    age = parse_line("He is 5 years old")
    assert isinstance(age, AstNode.SetAttribute)
    assert age.subject == "he"
    assert age.topic == "age"
    assert isinstance(age.value, NumberLiteral)
    assert age.value.value == 5.0


def test_howl_print_style():
    print_node = parse_line('he howls "WHOOOO!"')
    assert isinstance(print_node, AstNode.Print)
    assert print_node.style is PrintStyle.HOWL
    assert isinstance(print_node.values[0], StringLiteral)
    assert print_node.values[0].value == "WHOOOO!"


def test_growl_prints_field():
    chain = parse_line("when he has 1 toy then he growls his name")
    assert isinstance(chain, AstNode.IfChain)
    print_node = chain.branches[0].steps[0]
    assert isinstance(print_node, AstNode.Print)
    assert print_node.style is PrintStyle.GROWL
    field = print_node.values[0]
    assert isinstance(field, Field)
    assert field.subject == "his"
    assert field.topic == "name"


def test_woof_prints_name_field():
    chain = parse_line("when she has 1 toy then she woof her name")
    assert isinstance(chain, AstNode.IfChain)
    print_node = chain.branches[0].steps[0]
    assert isinstance(print_node, AstNode.Print)
    assert print_node.style is PrintStyle.WOOF
    field = print_node.values[0]
    assert isinstance(field, Field)
    assert field.subject == "her"
    assert field.topic == "name"
