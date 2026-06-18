from pbark.parser import ast_node as AstNode
from pbark.parser.parse_expression import StashAccess, StashPart
from tests.conftest import parse_line, parse_program


def test_script_stash_forms_without_filler_word():
    assert parse_line('cookie_jar holds "a", "b"').stash == "cookie_jar"
    assert parse_line("cookie_jar empty").stash == "cookie_jar"
    assert parse_line("cookie_jar drops first").stash == "cookie_jar"


def test_script_print_first_stash_item():
    print_node = parse_line("whimper first from cookie_jar")
    assert isinstance(print_node, AstNode.Print)
    access = print_node.values[0]
    assert isinstance(access, StashAccess)
    assert access.stash == "cookie_jar"
    assert access.which == "first"
    assert access.part is StashPart.ELEMENT


def test_story_glue_after_stash_name_does_not_change_script_parse():
    plain = parse_line('cookie_jar holds "a", "b"')
    with_glue = parse_line('cookie_jar stash holds "a", "b"')
    assert plain.stash == with_glue.stash
    plain_print = parse_line("whimper first from cookie_jar")
    glue_print = parse_line("whimper first from cookie_jar stash")
    assert plain_print.values == glue_print.values
