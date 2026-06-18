# pbark

Python port of [jbark](../src/main/java/dev/klomptech/jbark/), the dog-themed story programming language.

**Semi-automated port:** pbark was translated from the Java source. It aims for parity with jbark and shares the same test suite. If behaviour diverges, jbark is canonical.

## Release status

**Already in this repo:** interpreter, 95 tests, CLI, registry files in `pbark/resources/`, `pip install .` works from a clone.

**Still to do (you):** PyPI account → `twine upload` so others can `pip install pbark`. Full steps: [docs/RELEASE-PYTHON.md](../docs/RELEASE-PYTHON.md).

When you edit breeds/objects in `src/main/resources/`, refresh the Python copy:

```bash
./scripts/sync-pbark-resources.sh   # from repo root
```

## Install

Requires **Python 3.10+**.

```bash
cd pbark
python3 -m venv .venv
source .venv/bin/activate   # Windows: .venv\Scripts\activate
pip install -e .
```

## Run

From the repo root (after install):

```bash
./pbark/bin/pbark examples/woof/goodboy.woof
./pbark/bin/pbark --version
./pbark/bin/pbark --list-breeds
```

Or with the module directly:

```bash
python3 -m pbark examples/woof/bimba.woof
```

Flags match jbark: `--help`, `--version`, `--strict`, `--quiet`, `--list-breeds`, `--list-objects`.

## Tests

```bash
pip install -e ".[dev]"
pytest tests/ -q
```

The test suite is ported from jbark's JUnit tests. Golden output, parser shape, and feature coverage.

## Docs

- [docs/MANUAL.md](../docs/MANUAL.md) — grammar and runtime rules
- [docs/AUTHOR.md](../docs/AUTHOR.md) — writing stories
- [docs/RELEASE-PYTHON.md](../docs/RELEASE-PYTHON.md) — what's done vs what you still do for PyPI
- [examples/](../examples/) — runnable `.woof` programs
