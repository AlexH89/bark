import pytest

from pbark.cli.dog_art import PAW
from pbark.errors import BarkError
from pbark.parser import keywords as Keywords
from tests.conftest import FETCHY_REPEAT, run_joined, run_lines


def test_print_bark_and_bare_bark():
    assert run_joined('bark "Hello"') == "Hello"
    assert run_joined("bark") == "woof"


def test_natural_how_many_phrasing():
    assert run_joined(
        """
        labrador
        my labrador has 3 toys
        she woof how many toys she has
        """
    ) == "3"


def test_bare_number_sets_age():
    assert run_joined(
        """
        labrador
        labrador 5
        he woof he age
        he woof how many toys he has
        """
    ) == "5\n0"


def test_single_line_otherwise():
    assert (
        run_joined(
            """
        my labrador has 3 toys
        when my labrador has 3 toys then I bark "yes" otherwise I bark "no" bury
        """
        )
        == "yes"
    )
    assert (
        run_joined(
            """
        my labrador has 2 toys
        when my labrador has 3 toys then I bark "yes" otherwise I bark "no" bury
        """
        )
        == "no"
    )


def test_pass_toy_shortcut():
    assert run_joined(
        """
        labrador
        beagle
        my labrador has 2 toys
        the labrador passes a toy to the beagle
        bark how many toys my labrador has
        bark how many toys the beagle has
        """
    ) == "1\n1"


def test_bare_print_uses_style_default():
    assert run_joined("bark") == "woof"
    assert run_joined("say") == "woof"
    assert run_joined("woof") == "WOOF"
    assert run_joined("growl") == "GROWL"
    assert run_joined("howl") == "hhhhhowl"
    assert run_joined("whimper") == "whimper"
    assert run_joined("whine") == "whine"
    assert run_joined("yapping") == "yap yap"
    assert run_joined("whining") == "w h i n e"


def test_triple_bark_paws():
    assert PAW in run_joined("bark bark bark")


def test_print_voices():
    assert run_joined('woof "hi"') == "HI"
    assert run_joined('growl "hi"') == "HI"
    assert run_joined('howl "hi"') == "hhhhhi"
    assert run_joined('whine "HI"') == "hi"
    assert run_joined('yapping "hi"') == "hi hi"
    assert run_joined('whining "hi"') == "h i"


def test_assign_object_and_breed():
    assert run_joined("ball 5\nI bark ball\n") == "5"
    assert run_joined("corgi\nbark how many toys the corgi has\n") == "0"


def test_dog_attributes_and_adjustments():
    assert run_joined(
        """
        my labrador has 3 toys
        labrador name "Bimba"
        bark how many toys my labrador has
        I bark my labrador name
        """
    ) == "3\nBimba"

    assert run_joined(
        """
        my labrador has 3 toys
        my labrador feeds 2 treats
        bark how many toys my labrador has
        my labrador gulps a treat
        bark how many toys my labrador has
        """
    ) == f"3\n{FETCHY_REPEAT}\n3"

    assert run_joined(
        """
        my labrador has 3 toys
        my labrador feeds 2 treats
        bark how many treats my labrador has
        my labrador gulps a treat
        bark how many treats my labrador has
        """
    ) == "2\n1"

    assert run_joined(
        """
        my labrador has 3 toys
        my labrador grows 1 toy
        bark how many toys my labrador has
        """
    ) == "4"

    assert run_joined(
        """
        my labrador has 3 toys
        my labrador misplaces a toy
        bark how many toys my labrador has
        """
    ) == "2"


def test_when_otherwise():
    assert run_joined(
        """
        my labrador has 3 toys
        when my labrador has 3 toys then
            I woof "yes"
        otherwise
            I woof "no"
        bury
        """
    ) == "YES"
    assert run_joined(
        """
        my labrador has 2 toys
        when my labrador has 3 toys then
            I bark "yes"
        otherwise
            I bark "no"
        bury
        """
    ) == "no"


def test_while_and_until():
    assert run_joined(
        """
        my labrador has 3 toys
        while she has more than 0 toys then
            bark how many toys she has
            she misplaces a toy
        bury
        """
    ) == "3\n2\n1"

    assert run_joined(
        """
        my labrador has 1 toy
        until my labrador has 0 toys then
            bark how many toys my labrador has
            my labrador misplaces a toy
        bury
        """
    ) == "1"


def test_for_each_stash():
    assert run_joined(
        """
        cookie_jar holds "a", "b"
        for each snack from cookie_jar then
            I whimper snack
        bury
        """
    ) == "a\nb"


def test_stash_operations():
    assert run_joined(
        """
        cookie_jar holds 1, 2, 3
        bark how many items are in the cookie jar
        cookie_jar second is "y"
        I whimper the second item from the cookie jar
        cookie_jar drops first
        bark how many items are in the cookie jar
        """
    ) == "3\ny\n2"


def test_pile_operations():
    assert run_joined(
        """
        laundry_basket holds "a", "b"
        she puts "c" into the laundry basket
        I bark top of laundry_basket pile
        """
    ) == "c"


def test_functions():
    assert run_joined(
        """
        add expects a b returns a plus b bury
        bark add with 2, 3
        """
    ) == "5"


def test_math_and_logic():
    assert run_joined("I bark 2 plus 3") == "5"
    assert run_joined("I bark not false") == "true"
    assert (
        run_joined(
            "my labrador has 3 toys\n"
            "when my labrador has 3 toys and she has more than 2 toys "
            'then I bark "Three and plenty." bury'
        )
        == "Three and plenty."
    )


def test_string_ops():
    assert run_joined('I bark "woof" letters') == "4"
    assert run_joined('cookie_jar holds "a", "b"\nbark her cookie jar\n') == "a, b"


def test_shares_toys_and_stash():
    assert run_joined(
        """
        labrador
        beagle
        husky
        my labrador has 6 toys
        the labrador shares with the beagle and husky
        bark how many toys my labrador has
        bark how many toys the beagle has
        bark how many toys the husky has
        """
    ) == "2\n2\n2"

    assert run_joined(
        """
        labrador
        beagle
        husky
        her cookie jar holds "a", "b", "c"
        the labrador shares her cookie jar with the beagle, husky
        bark how many treats my labrador has
        bark how many treats the beagle has
        bark how many treats the husky has
        """
    ) == "1\n1\n1"


def test_trait_playful_reverses_dog_speech():
    assert run_joined('border_collie\nborder_collie barks "hello"\n') == "olleh"
    assert run_joined('border_collie\nI bark "hello"\n') == "hello"


def test_traits_loud_and_greedy():
    assert run_joined('husky\nhusky barks "hello"\n') == "HELLO"
    assert run_joined('labrador\nI yap "still yapping"\n') == "still yapping still yapping"
    assert run_joined(
        """
        beagle
        my beagle has 5 toys
        my beagle feeds 1 treat
        my beagle feeds 1 treat
        my beagle feeds 1 treat
        bark how many toys the beagle has
        bark how many treats the beagle has
        """
    ) == "5\n2"


def test_set_trait_runtime():
    assert run_joined('corgi\nmy corgi is loud\ncorgi barks "loud"\n') == "LOUD"


def test_story_intro_and_pronouns():
    assert run_joined(
        """
        I have a labrador.
        She is 2.
        She has 2 toys.
        Her name is "Bimba".
        she woof how many toys she has
        she woof her age
        she woof her name
        """
    ) == f"2\n{FETCHY_REPEAT}\n2\nBIMBA"

    assert run_joined(
        """
        I have a labrador.
        She has 2 toys.
        I have a beagle.
        she passes a toy to the beagle
        she woof how many toys she has
        she woof how many toys the beagle has
        """
    ) == "1\n1"


def test_pronouns():
    assert run_joined(
        """
        my labrador has 3 toys
        she misplaces a toy
        bark how many toys my labrador has
        """
    ) == "2"


def test_control_signals():
    assert run_joined(
        """
        labrador
        my labrador has 3 toys
        while she has more than 0 toys then
            bark how many toys she has
            she misplaces a toy
            when she has 1 toy then heel
        bury
        """
    ) == "3\n2"


def test_share_remainder_stays_with_giver():
    assert run_joined(
        """
        labrador
        beagle
        husky
        my labrador has 7 toys
        the labrador shares with the beagle and husky
        bark how many toys my labrador has
        bark how many toys the beagle has
        bark how many toys the husky has
        """
    ) == "3\n2\n2"


def test_story_take_from_stash_with_whimper():
    assert run_joined(
        """
        labrador
        her cookie jar holds "cookie", "cookie", "cookie", "cookie"
        she whimpers as she wants and takes the first biscuit from her cookie jar
        bark how many items are in her cookie jar
        she woof how many treats she has
        """
    ) == "whimper whimper\n3\n1"


def test_story_take_from_stash_without_whimper():
    assert run_joined(
        """
        labrador
        her cookie jar holds "cookie", "cookie", "cookie", "cookie"
        she wants and takes the first biscuit from her cookie jar
        bark how many items are in her cookie jar
        she woof how many treats she has
        """
    ) == "3\n1"


def test_story_take_from_stash_wants_only():
    assert run_joined(
        """
        labrador
        her cookie jar holds "biscuit", "stick"
        she whimpers as she wants the first biscuit from her cookie jar
        bark how many items are in her cookie jar
        she woof how many treats she has
        """
    ) == f"whimper whimper\n1\n{FETCHY_REPEAT}\n1"


def test_story_stash_clear():
    assert run_joined(
        """
        her cookie jar holds "a", "b"
        her cookie jar is empty
        bark how many items are in her cookie jar
        """
    ) == "0"


def test_colon_story_glue():
    assert run_joined(
        """
        bark: "hello"
        her cookie jar holds some items: "a", "b", "c"
        bark how many items are in her cookie jar
        """
    ) == "hello\n3"


def test_pet_name_resolves_to_breed():
    assert run_joined(
        """
        I have a labrador.
        She has 4 toys.
        Her name is "Bimba".
        I have a dachshund.
        my dachshund has 2 toys
        Bimba shares with the dachshund
        bark how many toys my labrador has
        bark how many toys the dachshund has
        """
    ) == "2\n2"

    assert run_joined(
        """
        labrador
        Her name is "Bimba"
        Bimba pinches a treat
        she woof how many treats she has
        """
    ) == "1"

    assert run_joined(
        """
        labrador
        Her name is "Bimba"
        Bimba pinches a treat
        Bimba pinches a treat
        when Bimba has 1 treats then bark "yes" otherwise bark "no"
        """
    ) == "no"

    assert run_joined(
        """
        labrador
        Her name is "Bimba"
        Bimba pinches a treat
        when Bimba has 1 treats then bark "yes" otherwise bark "no"
        """
    ) == "yes"


def test_share_with_multi_word_breed():
    assert run_joined(
        """
        labrador
        golden retriever
        my labrador has 6 toys
        the labrador shares with the golden retriever
        bark how many toys my labrador has
        bark how many toys the golden retriever has
        """
    ) == f"3\n{FETCHY_REPEAT}\n3"


def test_greedy_loses_food_after_threshold():
    assert run_joined(
        """
        beagle
        my beagle is greedy
        my beagle has 5 toys
        my beagle feeds 1 treat
        my beagle feeds 1 treat
        my beagle feeds 1 treat
        bark how many toys the beagle has
        bark how many treats the beagle has
        """
    ) == "5\n2"


def test_when_inside_print_is_not_a_branch():
    assert run_joined('bark "The pack heads home when the jar is empty."\n') == (
        "The pack heads home when the jar is empty."
    )


def test_counter_tradition_demo():
    assert run_joined(
        """
        I have a corgi.
        while she sniffs less toys than 8 then
            she snags a toy
        bury
        bark "The count is:"
        she woof how many toys she has
        """
    ) == "The count is:\n8"


def test_she_finds_toy_increments():
    assert run_joined("beagle\nshe finds a toy\nshe woof how many toys she has\n") == "1"


def test_she_snags_toy_increments():
    assert run_joined("beagle\nshe snags a toy\nshe woof how many toys she has\n") == "1"


def test_invalid_while_condition_fails():
    with pytest.raises(BarkError) as exc:
        run_lines(
            """
            corgi
            while my corgi toys growls less than 8 then
            bury
            """
        )
    assert "no clue" in str(exc.value)


def test_story_aliases_map_to_canonical_fields():
    assert run_joined(
        """
        labrador
        my labrador has 1 toy
        my labrador feeds 2 treats
        bark how many toys my labrador has
        bark how many treats my labrador has
        labrador nickname "Pepon"
        I bark my labrador name
        """
    ) == "1\n2\nPepon"

    assert run_joined(
        """
        labrador
        Her name is "Bimba"
        her owner is "Alex"
        I bark my labrador name
        I bark my labrador owner
        """
    ) == "Bimba\nAlex"


def test_breed_is_not_a_stored_field():
    assert not Keywords.is_attribute_keyword("breed")


def test_story_constant_from_stash_count():
    assert run_joined(
        """
        her cookie jar holds "cookie", "cookie", "cookie", "cookie"
        memory is count of the items in the cookie jar
        bark memory
        """
    ) == "4"


def test_story_journal():
    assert run_joined('journal is "walk time"\nbark journal\n') == "walk time"


def test_story_constant():
    assert run_joined(
        """
        labrador
        memory is 4
        my labrador has 2 toys
        bark memory minus how many toys the labrador has
        """
    ) == "2"


def test_story_constant_with_pronoun_field():
    assert run_joined(
        """
        labrador
        memory is 4
        my labrador has 2 toys
        bark memory minus how many toys she has
        """
    ) == "2"


def test_human_story_glue_before_print():
    assert run_joined(
        """
        labrador
        Her name is "Bimba"
        She has 3 treats
        when Bimba has 3 treats then the human growls "Nothing left." otherwise bark "no"
        """
    ) == "NOTHING LEFT."


def test_errors_are_dog_themed():
    with pytest.raises(BarkError) as exc:
        run_lines("bark memory minus how many toys phantom has\n")
    assert "No scent" in str(exc.value)
