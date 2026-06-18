# Examples

All shipped `.woof` files use **story syntax** with filler. Script-style lines belong in `examples/scratch/` (gitignored).

Full language reference: `[docs/MANUAL.md](../docs/MANUAL.md)`.

**Run a file:**

- **Java:** `./bark woof/bimba.woof` if you have a [release](https://github.com/AlexH89/bark/releases) unzip, or `./gradlew run --args="examples/woof/…"` from a clone.
- **Python:** `./pbark/bin/pbark examples/woof/bimba.woof` from a clone (after `pip install -e pbark/`).

## Run these

**Hello world** (paste this one in chat, ~12 lines):

```bash
./gradlew run --args="examples/woof/goodboy.woof"
# or:
./pbark/bin/pbark examples/woof/goodboy.woof
```

`[woof/goodboy.woof](woof/goodboy.woof)`: Dot the beagle, empty bowl, one `when`, print voices.

**Hero (start here):**

```bash
./gradlew run --args="examples/woof/bimba.woof"
# or:
./pbark/bin/pbark examples/woof/bimba.woof
```

`[woof/bimba.woof](woof/bimba.woof)`: Bimba the labrador, greedy trait, print voices, sharing.

**Tradition** (esolang inside joke):

```bash
./gradlew run --args="examples/woof/counter-tradition.woof"
# or:
./pbark/bin/pbark examples/woof/counter-tradition.woof
```

`[woof/counter-tradition.woof](woof/counter-tradition.woof)`: counter loop (Brainfuck `+++++.` spirit).

**Learn** (syntax tour):

```bash
./gradlew run --args="examples/woof/tutorial.woof"
# or:
./pbark/bin/pbark examples/woof/tutorial.woof
```

`[woof/tutorial.woof](woof/tutorial.woof)`: Bimba at the park.

**If / while demo:**

```bash
./gradlew run --args="examples/woof/loops.woof"
# or:
./pbark/bin/pbark examples/woof/loops.woof
```

`[woof/rebuild-if-while.woof](woof/rebuild-if-while.woof)`: multiline branches and loops.

Dog state is nine fields per breed (`food`, `items`, `inventory`, `age`, `name`, `description`, `color`, `happy`, `fed`). Story usually says **toys** and **treats**. See `[docs/AUTHOR.md](../docs/AUTHOR.md#dog-fields-nine-stored-many-story-words)`.