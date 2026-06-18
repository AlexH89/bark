from __future__ import annotations

import sys
from pathlib import Path

from pbark.cli import dog_art as DogArt
from pbark.cli import version as Version
from pbark.config.config_loader import ConfigLoader
from pbark.errors import BarkError, ExitCode
from pbark.interpreter.interpreter import Interpreter
from pbark.lexer import Lexer
from pbark.options import BarkOptions
from pbark.parser.parser import Parser


def print_help() -> None:
    print(
        """pbark: a dog-themed story programming language (Python port of jbark)

Usage:
  pbark [script.woof]          Run a .woof file
  pbark                        Read lines from stdin (Ctrl+D to finish)
  pbark --version              Show version and a dog fact
  pbark --list-breeds          List registered breeds
  pbark --list-objects         List registered objects
  pbark --strict script.woof   Warn on story-only lines that do nothing
  pbark --quiet script.woof    Suppress startup banner and goodbye

Docs: docs/AUTHOR.md · docs/MANUAL.md
"""
    )


def _list_names(label: str, names: list[str]) -> None:
    print(f"{label}:")
    for name in names:
        print(f"  {name}")


def _is_help_flag(arg: str) -> bool:
    return arg in ("--help", "-h")


def _is_version_flag(arg: str) -> bool:
    return arg in ("--version", "-version")


def run(source: str, options: BarkOptions | None = None) -> int:
    options = options or BarkOptions.defaults()
    try:
        if not options.quiet:
            DogArt.print_banner()
        lexer = Lexer(source)
        parser = Parser(lexer.tokenise(), options)
        program = parser.parse()
        Interpreter().run(program)
        if not options.quiet:
            DogArt.print_goodbye()
        return ExitCode.OK
    except BarkError as e:
        print(e, file=sys.stderr)
        return ExitCode.DATA_ERROR


def main() -> None:
    options = BarkOptions.defaults()
    script_path: str | None = None
    for arg in sys.argv[1:]:
        if _is_help_flag(arg):
            print_help()
            sys.exit(ExitCode.OK)
        if _is_version_flag(arg):
            Version.print_version()
            sys.exit(ExitCode.OK)
        if arg == "--strict":
            options = BarkOptions(True, options.quiet)
            continue
        if arg == "--quiet":
            options = BarkOptions(options.strict, True)
            continue
        if arg == "--list-breeds":
            _list_names("Breeds", ConfigLoader.list_breeds())
            sys.exit(ExitCode.OK)
        if arg == "--list-objects":
            _list_names("Objects", ConfigLoader.list_objects())
            sys.exit(ExitCode.OK)
        if arg.startswith("-"):
            print(f"Unknown flag: {arg}. Try pbark --help", file=sys.stderr)
            sys.exit(ExitCode.USAGE)
        if script_path is not None:
            print("Usage: pbark [flags] [script.woof]. Try pbark --help", file=sys.stderr)
            sys.exit(ExitCode.USAGE)
        script_path = arg

    try:
        if script_path is not None:
            source = Path(script_path).read_text(encoding="utf-8")
        else:
            print("> ", end="")
            source = sys.stdin.read()
    except OSError as e:
        print(f"Couldn't dig up that file: {e}", file=sys.stderr)
        sys.exit(ExitCode.INVALID_ARGUMENTS)

    sys.exit(run(source, options))


if __name__ == "__main__":
    main()
