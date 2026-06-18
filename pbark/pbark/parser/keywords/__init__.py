from __future__ import annotations

from pbark.config.config_loader import ConfigLoader
from pbark.config.dog_topics import DogTopics
from pbark.config.variable_type import VariableType
from pbark.lexer import Token, TokenType
from pbark.parser.keywords.attribute_keyword_groups import AttributeKeywordGroups
from pbark.parser.keywords.collection_keyword_groups import CollectionKeywordGroups
from pbark.parser.keywords.control_flow_keyword_groups import ControlFlowKeywordGroups
from pbark.parser.keywords.keyword_registry import KeywordRegistry
from pbark.parser.keywords.logic_keyword_groups import LogicKeywordGroups
from pbark.parser.keywords.print_keyword_groups import PrintKeywordGroups
from pbark.print_style import PrintStyle

registry = KeywordRegistry()
_print = PrintKeywordGroups(registry)
_control = ControlFlowKeywordGroups(registry)
_collection = CollectionKeywordGroups(registry)
_logic = LogicKeywordGroups(registry)
_attribute = AttributeKeywordGroups(registry)

PRINT_KEYWORDS = _print.print_words
EXIT_KEYWORDS = _print.exit_words
STDIN_KEYWORDS = _print.stdin_words
WAIT_KEYWORDS = _print.wait_words

BOOLEAN_VALUES = _logic.boolean_values
NULL_VALUES = _logic.null_values

IF_START_WORDS = _control.if_start_words
WHILE_KEYWORDS = _control.while_words
UNTIL_KEYWORDS = _control.until_words
FOR_WORDS = _control.for_words
EACH_WORDS = _control.each_words
FROM_WORDS = _control.from_words
THEN_KEYWORDS = _control.then_words
OTHERWISE_WORDS = _control.otherwise_words
ELSE_WORDS = _control.else_words
BURY_KEYWORDS = _control.bury_words
BREAK_KEYWORDS = _control.break_words
CONTINUE_KEYWORDS = _control.continue_words
EXPECTS_WORDS = _control.expects_words
TRICK_RETURNS_WORDS = _control.trick_returns_words

HOLDS_WORDS = _collection.holds_words
FINDS_WORDS = _collection.finds_words
STASH_ADD_WORDS = _collection.stash_add_words
PILE_GRAB_FROM_WORDS = _collection.pile_grab_from_words
PILE_PUSH_INTO_WORDS = _collection.pile_push_into_words
DROPS_WORDS = _collection.drops_words
DIG_WORDS = _collection.dig_words
COUNT_WORDS = _collection.count_words
JOIN_WORDS = _collection.join_words
TAKES_WORDS = _collection.takes_words
GIVES_WORDS = _collection.gives_words
BACK_WORDS = _collection.back_words
WITH_WORDS = _collection.with_words
TOP_WORDS = _collection.top_words
STASH_SPOT_WORDS = _collection.stash_spot_words
SHARES_STATEMENT_KEYWORDS = _collection.shares_words
PASSES_STATEMENT_KEYWORDS = _collection.passes_words
TO_WORDS = _collection.to_words
TAKE_FROM_STASH_WORDS = _collection.take_from_stash_words

LETTERS_WORDS = _logic.letters_words
INSIDE_WORDS = _logic.inside_words
COUNTS_WORDS = _logic.counts_words
LOGICAL_AND_WORDS = _logic.logical_and_words
CONDITION_AND_WORDS = _logic.condition_and_words
LOGICAL_OR_WORDS = _logic.logical_or_words
NEITHER_WORDS = _logic.neither_words
NOR_WORDS = _logic.nor_words
COMPARISON_EQUAL_WORDS = _logic.comparison_equal_words
COMPARISON_NOT_EQUAL_WORDS = _logic.comparison_not_equal_words
COMPARISON_GREATER_WORDS = _logic.comparison_greater_words
COMPARISON_LESS_WORDS = _logic.comparison_less_words
COMPARISON_AT_LEAST_WORDS = _logic.comparison_at_least_words
COMPARISON_AT_MOST_WORDS = _logic.comparison_at_most_words

PLUS_KEYWORDS = _logic.plus_words
MINUS_KEYWORDS = _logic.minus_words
STAR_KEYWORDS = _logic.star_words
SLASH_KEYWORDS = _logic.slash_words
TRAIT_KEYWORDS = _attribute.trait_words
NOT_KEYWORDS = _logic.not_words

LIST_AND_WORDS = _attribute.list_and_words
THE_WORDS = _attribute.the_words
OF_WORDS = _attribute.of_words
ITEMS_WORDS = _attribute.items_words
IN_WORDS = _attribute.in_words
AS_WORDS = _attribute.as_words
BY_WORDS = _attribute.by_words
HOW_WORDS = _attribute.how_words
MANY_WORDS = _attribute.many_words
YEARS_WORDS = _attribute.years_words
OLD_WORDS = _attribute.old_words
TYPE_NUMBER_WORDS = _attribute.type_number_words
TYPE_WORDS_WORDS = _attribute.type_words_words
TYPE_NOTHING_WORDS = _attribute.type_nothing_words
SNIFFS_WORDS = _attribute.sniff_words
GROWLS_WORDS = _attribute.growl_words
SAME_WORDS = _attribute.same_words
THAN_WORDS = _attribute.than_words
AT_WORDS = _attribute.at_words
TOGETHER_WORDS = _attribute.together_words
OR_WORDS = _attribute.or_words

BREED_PRONOUN_WORDS = ["he", "she", "him", "her", "his"]
FEMININE_BREED_PRONOUNS = ["she", "her"]
MASCULINE_BREED_PRONOUNS = ["he", "him", "his"]
OBJECT_PRONOUN_WORDS = ["it"]
ARTICLE_WORDS = ["a", "an"]
ATTRIBUTE_INCREMENT_WORDS = _attribute.increment_words
ATTRIBUTE_DECREMENT_WORDS = _attribute.decrement_words
FOOD_INCREMENT_WORDS = _attribute.food_increment_words
FOOD_DECREMENT_WORDS = _attribute.food_decrement_words
ITEM_INCREMENT_WORDS = _attribute.item_increment_words
ITEM_DECREMENT_WORDS = _attribute.item_decrement_words

HOW_MANY_PHRASE_STEPS = [HOW_WORDS, MANY_WORDS]
HOW_MANY_ITEMS_IN_PHRASE_STEPS = [HOW_WORDS, MANY_WORDS, ITEMS_WORDS, IN_WORDS]
EQUAL_COMPARISON_PHRASE_STEPS = [SNIFFS_WORDS, SAME_WORDS, AS_WORDS]
NOT_EQUAL_COMPARISON_PHRASE_STEPS = [SNIFFS_WORDS, COMPARISON_NOT_EQUAL_WORDS, FROM_WORDS]
GREATER_COMPARISON_PHRASE_STEPS = [GROWLS_WORDS, COMPARISON_GREATER_WORDS, THAN_WORDS]
LESS_COMPARISON_PHRASE_STEPS = [SNIFFS_WORDS, COMPARISON_LESS_WORDS, THAN_WORDS]
AT_LEAST_COMPARISON_PHRASE_STEPS = [SNIFFS_WORDS, AT_WORDS, COMPARISON_AT_LEAST_WORDS]
AT_MOST_COMPARISON_PHRASE_STEPS = [SNIFFS_WORDS, AT_WORDS, COMPARISON_AT_MOST_WORDS]
OR_ELSE_PHRASE_STEPS = [OR_WORDS, ELSE_WORDS]
TOGETHER_WITH_PHRASE_STEPS = [TOGETHER_WORDS, WITH_WORDS]
SNIFFS_INSIDE_PHRASE_STEPS = [SNIFFS_WORDS, INSIDE_WORDS]

TYPE_CHECK_KINDS = {
    **{w: "NUMBER" for w in TYPE_NUMBER_WORDS},
    **{w: "WORDS" for w in TYPE_WORDS_WORDS},
    **{w: "NOTHING" for w in TYPE_NOTHING_WORDS},
}

OBJECT_ALIASES = {
    "balls": "ball",
    "bones": "bone",
    "bowls": "bowl",
    "frisbees": "frisbee",
    "leashes": "leash",
    "sticks": "stick",
    "slippers": "slippers",
    "tennis_balls": "tennis_ball",
    "squeaky_toys": "squeaky_toy",
    "toy_box": "toy_box",
    "treat": "treat",
    "biscuit": "biscuit",
    "biscuits": "biscuit",
    "cookie": "cookie",
    "cookies": "cookie",
    "snacks": "snack",
}

PRINT_STYLES = {
    "bark": PrintStyle.BARK,
    "barks": PrintStyle.BARK,
    "shout": PrintStyle.GROWL,
    "shouts": PrintStyle.GROWL,
    "say": PrintStyle.BARK,
    "says": PrintStyle.BARK,
    "mention": PrintStyle.BARK,
    "mentions": PrintStyle.BARK,
    "tell": PrintStyle.BARK,
    "tells": PrintStyle.BARK,
    "announce": PrintStyle.BARK,
    "announces": PrintStyle.BARK,
    "declare": PrintStyle.BARK,
    "declares": PrintStyle.BARK,
    "command": PrintStyle.BARK,
    "commands": PrintStyle.BARK,
    "pant": PrintStyle.BARK,
    "pants": PrintStyle.BARK,
    "growl": PrintStyle.GROWL,
    "growls": PrintStyle.GROWL,
    "whimper": PrintStyle.WHIMPER,
    "whimpers": PrintStyle.WHIMPER,
    "whisper": PrintStyle.WHIMPER,
    "whispers": PrintStyle.WHIMPER,
    "howl": PrintStyle.HOWL,
    "howls": PrintStyle.HOWL,
    "woof": PrintStyle.WOOF,
    "woofs": PrintStyle.WOOF,
    "whine": PrintStyle.WHINE,
    "whines": PrintStyle.WHINE,
    "whining": PrintStyle.WHINING,
    "mutter": PrintStyle.WHINING,
    "mutters": PrintStyle.WHINING,
    "yapping": PrintStyle.YAPPING,
    "yapps": PrintStyle.YAPPING,
    "yap": PrintStyle.YAPPING,
    "yaps": PrintStyle.YAPPING,
    "squeak": PrintStyle.YAPPING,
    "squeaks": PrintStyle.YAPPING,
}

KEYWORD_SET: set[str] = set()
registry.fill_keyword_set(KEYWORD_SET)

ASSIGN_SUBJECT_GLUE_WORDS = ["is", "are", "has", "have", "was", "were", "becomes", "became"]


def is_keyword(word: str) -> bool:
    return word.strip().lower() in KEYWORD_SET


def is_heard(word: str) -> bool:
    normalized = word.strip().lower()
    return (
        is_keyword(normalized)
        or ConfigLoader.is_valid_name(normalized)
        or is_pronoun_word(normalized)
        or is_attribute_keyword(normalized)
        or is_trait_keyword(normalized)
        or normalized in BOOLEAN_VALUES
        or normalized in NULL_VALUES
        or resolve_object_name(normalized) is not None
    )


def is_ignored(word: str) -> bool:
    return not is_heard(word)


def is_if_start(word: str) -> bool:
    return word.strip().lower() in IF_START_WORDS


def is_then_word(word: str) -> bool:
    return word in THEN_KEYWORDS


def is_otherwise_word(word: str) -> bool:
    normalized = word.strip().lower()
    return normalized in OTHERWISE_WORDS or normalized in ELSE_WORDS


def is_block_end_word(word: str) -> bool:
    return word.strip().lower() in BURY_KEYWORDS


def is_null_clear_word(word: str) -> bool:
    return word in NULL_VALUES


def is_for_word(word: str) -> bool:
    return word.strip().lower() in FOR_WORDS


def parse_boolean(word: str) -> bool | None:
    normalized = word.strip().lower()
    if normalized not in BOOLEAN_VALUES:
        return None
    if normalized in ("true", "yes", "yeah", "yep"):
        return True
    if normalized in ("false", "no", "nope"):
        return False
    return bool(word)


def is_pronoun_word(word: str) -> bool:
    normalized = word.strip().lower()
    return normalized in BREED_PRONOUN_WORDS or normalized in OBJECT_PRONOUN_WORDS


def is_breed_pronoun_word(word: str) -> bool:
    return word.strip().lower() in BREED_PRONOUN_WORDS


def is_feminine_breed_pronoun(word: str) -> bool:
    return word.strip().lower() in FEMININE_BREED_PRONOUNS


def is_masculine_breed_pronoun(word: str) -> bool:
    return word.strip().lower() in MASCULINE_BREED_PRONOUNS


def is_object_pronoun_word(word: str) -> bool:
    return word.strip().lower() in OBJECT_PRONOUN_WORDS


def is_subject_reference(word: str) -> bool:
    normalized = word.strip().lower()
    return ConfigLoader.is_valid_name(normalized) or is_pronoun_word(normalized)


def is_dog_subject_reference(word: str, original: str | None = None) -> bool:
    original = word if original is None else original
    normalized = word.strip().lower()
    return (
        ConfigLoader.is_breed(normalized)
        or is_breed_pronoun_word(normalized)
        or is_pet_name_word(normalized, original)
    )


def is_pet_name_word(normalized: str, original: str | None = None) -> bool:
    if ConfigLoader.is_valid_name(normalized):
        return False
    if normalized in THE_WORDS or normalized in ARTICLE_WORDS:
        return False
    if is_heard(normalized):
        return False
    return bool(original) and original[0].isupper()


def is_article_word(word: str) -> bool:
    return word.strip().lower() in ARTICLE_WORDS


def resolve_type_check(word: str) -> str | None:
    return TYPE_CHECK_KINDS.get(word.strip().lower())


def is_assign_subject_glue(word: str) -> bool:
    return word.strip().lower() in ASSIGN_SUBJECT_GLUE_WORDS


def is_attribute_keyword(word: str) -> bool:
    return DogTopics.is_field_word(word)


def is_trait_keyword(word: str) -> bool:
    return word.strip().lower() in TRAIT_KEYWORDS


def is_math_add_keyword(word: str) -> bool:
    normalized = word.strip().lower()
    return normalized in PLUS_KEYWORDS or normalized in ATTRIBUTE_INCREMENT_WORDS


def is_math_subtract_keyword(word: str) -> bool:
    normalized = word.strip().lower()
    return normalized in MINUS_KEYWORDS or normalized in ATTRIBUTE_DECREMENT_WORDS


def is_print_expression_cue(word: str) -> bool:
    normalized = word.strip().lower()
    return (
        normalized in WITH_WORDS
        or is_math_add_keyword(normalized)
        or is_math_subtract_keyword(normalized)
        or normalized in STAR_KEYWORDS
        or normalized in SLASH_KEYWORDS
        or normalized in NOT_KEYWORDS
        or normalized in LETTERS_WORDS
        or normalized in COUNT_WORDS
    )


def is_attribute_adjust_increment(word: str) -> bool:
    return word in ATTRIBUTE_INCREMENT_WORDS


def is_attribute_adjust_decrement(word: str) -> bool:
    return word in ATTRIBUTE_DECREMENT_WORDS


def is_food_adjust_increment(word: str) -> bool:
    return word in FOOD_INCREMENT_WORDS


def is_item_adjust_increment(word: str) -> bool:
    return word in ITEM_INCREMENT_WORDS


def is_food_adjust_decrement(word: str) -> bool:
    return word in FOOD_DECREMENT_WORDS


def is_item_adjust_decrement(word: str) -> bool:
    return word in ITEM_DECREMENT_WORDS


def explicit_attribute_topic(word: str) -> str | None:
    return DogTopics.resolve(word)


def resolve_attribute_topic(word: str) -> str:
    explicit = explicit_attribute_topic(word)
    return explicit if explicit is not None else DogTopics.default_adjust_topic()


def resolve_adjust_topic(optional_topic_word: str | None, verb: str) -> str:
    if optional_topic_word:
        from_word = explicit_attribute_topic(optional_topic_word)
        if from_word is not None:
            return from_word
    return default_adjust_topic_for_verb(verb)


def default_adjust_topic_for_verb(verb: str) -> str:
    if is_food_adjust_increment(verb) or is_food_adjust_decrement(verb):
        return DogTopics.default_food_adjust_topic()
    return DogTopics.default_adjust_topic()


def is_years_old_suffix(following_words) -> bool:
    saw_years = False
    for raw in following_words:
        word = raw.strip().lower()
        if is_ignored(word):
            continue
        if word in YEARS_WORDS:
            saw_years = True
            continue
        if word in OLD_WORDS and saw_years:
            return True
        break
    return saw_years


def is_list_separator(token: Token) -> bool:
    if token.is_(TokenType.COMMA):
        return True
    return token.is_(TokenType.IDENTIFIER) and token.value.strip().lower() in LIST_AND_WORDS


def resolve_object_name(word: str) -> str | None:
    normalized = word.strip().lower()
    if ConfigLoader.type_of(normalized) == VariableType.OBJECT:
        return normalized
    singular = OBJECT_ALIASES.get(normalized)
    if singular is not None and ConfigLoader.type_of(singular) == VariableType.OBJECT:
        return singular
    return None
