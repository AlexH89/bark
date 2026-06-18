from __future__ import annotations

from pbark.config._resources import resources_dir
from pbark.config.variable_type import VariableType
from pbark.errors import BarkError
from pbark.lexer import Token, TokenType


class ConfigLoader:
    MEMORY = "memory"
    JOURNAL = "journal"

    _INDEX: dict[str, VariableType] = {}
    _PHRASE_ALIASES: dict[str, str] = {}
    _loaded = False

    @classmethod
    def _ensure_loaded(cls) -> None:
        if cls._loaded:
            return
        cls._load("/breeds.txt", VariableType.BREED)
        cls._load("/objects.txt", VariableType.OBJECT)
        cls._load("/stashes.txt", VariableType.STASH)
        cls._load("/piles.txt", VariableType.PILE)
        cls._INDEX[cls.MEMORY] = VariableType.STORY_NUMBER
        cls._INDEX[cls.JOURNAL] = VariableType.STORY_TEXT
        cls._build_phrase_aliases()
        cls._loaded = True

    @classmethod
    def _load(cls, filename: str, vtype: VariableType) -> None:
        path = resources_dir() / filename.lstrip("/")
        if not path.is_file():
            raise IllegalStateError(f"Resource not found: {filename}")
        for line in path.read_text(encoding="utf-8").splitlines():
            word = line.strip()
            if word:
                cls._INDEX[cls.normalise(word)] = vtype

    @classmethod
    def _build_phrase_aliases(cls) -> None:
        for name in list(cls._INDEX.keys()):
            cls._PHRASE_ALIASES[name] = name
            if "_" in name:
                cls._PHRASE_ALIASES[name.replace("_", " ")] = name

    @staticmethod
    def normalise(name: str) -> str:
        return name.strip().lower()

    @classmethod
    def type_of(cls, name: str) -> VariableType | None:
        cls._ensure_loaded()
        return cls._INDEX.get(cls.normalise(name))

    @classmethod
    def is_valid_name(cls, name: str) -> bool:
        return cls.type_of(name) is not None

    @classmethod
    def is_breed(cls, name: str) -> bool:
        return cls.type_of(name) == VariableType.BREED

    @classmethod
    def is_stash(cls, name: str) -> bool:
        return cls.type_of(name) == VariableType.STASH

    @classmethod
    def is_pile(cls, name: str) -> bool:
        return cls.type_of(name) == VariableType.PILE

    @classmethod
    def is_story_number_constant(cls, name: str) -> bool:
        return cls.type_of(name) == VariableType.STORY_NUMBER

    @classmethod
    def is_story_text_constant(cls, name: str) -> bool:
        return cls.type_of(name) == VariableType.STORY_TEXT

    @classmethod
    def is_story_constant(cls, name: str) -> bool:
        return cls.is_story_number_constant(name)

    @classmethod
    def memory_name(cls) -> str:
        return cls.MEMORY

    @classmethod
    def journal_name(cls) -> str:
        return cls.JOURNAL

    @classmethod
    def token_index_after_resolved_name(
        cls, tokens: list[Token], fr: int, to: int, resolved_name: str
    ) -> int:
        cls._ensure_loaded()
        words: list[str] = []
        positions: list[int] = []
        for i in range(fr, to):
            token = tokens[i]
            if token.is_(TokenType.IDENTIFIER):
                words.append(cls.normalise(token.value))
                positions.append(i)
        for length in range(min(4, len(words)), 0, -1):
            for start in range(len(words) - length + 1):
                phrase = " ".join(words[start : start + length])
                if resolved_name == cls.resolve_name_phrase(phrase):
                    return positions[start + length - 1] + 1
        key = cls.normalise(resolved_name)
        for i in range(fr, to):
            token = tokens[i]
            if token.is_(TokenType.IDENTIFIER) and key == cls.normalise(token.value):
                return i + 1
        return -1

    @classmethod
    def list_names(cls, vtype: VariableType) -> list[str]:
        cls._ensure_loaded()
        return sorted(k for k, v in cls._INDEX.items() if v == vtype)

    @classmethod
    def list_breeds(cls) -> list[str]:
        return cls.list_names(VariableType.BREED)

    @classmethod
    def list_objects(cls) -> list[str]:
        return cls.list_names(VariableType.OBJECT)

    @classmethod
    def list_stashes(cls) -> list[str]:
        return cls.list_names(VariableType.STASH)

    @classmethod
    def list_piles(cls) -> list[str]:
        return cls.list_names(VariableType.PILE)

    @classmethod
    def resolve_name_phrase(cls, phrase: str) -> str | None:
        cls._ensure_loaded()
        return cls._PHRASE_ALIASES.get(cls.normalise(phrase))

    @classmethod
    def resolve_name_from_tokens(cls, tokens: list[Token], fr: int, to: int) -> str | None:
        cls._ensure_loaded()
        words = cls._identifier_words(tokens, fr, to)
        cls._skip_leading_story_noise(words)
        return cls._match_longest_name_phrase(words)

    @classmethod
    def resolve_collection_from_tokens(
        cls, tokens: list[Token], fr: int, to: int
    ) -> str | None:
        cls._ensure_loaded()
        words = cls._identifier_words(tokens, fr, to)
        cls._skip_leading_story_noise(words)
        best = None
        best_length = 0
        for length in range(min(4, len(words)), 0, -1):
            for start in range(len(words) - length + 1):
                phrase = " ".join(words[start : start + length])
                resolved = cls.resolve_name_phrase(phrase)
                if (
                    resolved is not None
                    and (cls.is_stash(resolved) or cls.is_pile(resolved))
                    and length >= best_length
                ):
                    best = resolved
                    best_length = length
        return best

    @classmethod
    def _skip_leading_story_noise(cls, words: list[str]) -> None:
        while words:
            if cls.is_valid_name(words[0]) or cls.resolve_name_phrase(words[0]) is not None:
                break
            name_ahead = False
            for length in range(1, min(4, len(words)) + 1):
                if cls.resolve_name_phrase(" ".join(words[:length])) is not None:
                    name_ahead = True
                    break
            if name_ahead:
                break
            words.pop(0)

    @staticmethod
    def _identifier_words(tokens: list[Token], fr: int, to: int) -> list[str]:
        words: list[str] = []
        for i in range(fr, to):
            token = tokens[i]
            if token.is_(TokenType.IDENTIFIER):
                words.append(ConfigLoader.normalise(token.value))
        return words

    @classmethod
    def _match_longest_name_phrase(cls, words: list[str]) -> str | None:
        for length in range(min(4, len(words)), 0, -1):
            for start in range(len(words) - length + 1):
                phrase = " ".join(words[start : start + length])
                resolved = cls.resolve_name_phrase(phrase)
                if resolved is not None:
                    return resolved
        return None

    @classmethod
    def require_stash(cls, name: str, line: int) -> None:
        if not cls.is_stash(name):
            from pbark.config.name_hints import NameHints

            raise BarkError(
                line,
                f"'{name}' is not a not something the dogs can get stuff from or bury in."
                + NameHints.hint_phrase(name, cls.list_stashes()),
            )

    @classmethod
    def require_pile(cls, name: str, line: int) -> None:
        if not cls.is_pile(name):
            from pbark.config.name_hints import NameHints

            raise BarkError(
                line,
                f"'{name}' is not a registered pile. wrong spot."
                + NameHints.hint_phrase(name, cls.list_piles()),
            )

    @classmethod
    def require_breed(cls, name: str, line: int) -> None:
        if not cls.is_breed(name):
            from pbark.config.name_hints import NameHints

            raise BarkError(
                line,
                f'"{name}" isn\'t a breed we know about. Garbage bin mix??'
                + NameHints.hint_phrase(name, cls.list_breeds()),
            )


class IllegalStateError(RuntimeError):
    pass
