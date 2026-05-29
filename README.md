# Bark

A small, dog-themed programming language. One statement per line. No semicolons. No `=`. Just words.

## Getting Started

### Tutorial

If you're just getting started with Bark, read this page top to bottom — it'll walk you through the basics and have you barking in no time.

Build and run:

```bash
./gradlew run --args="src/main/java/dev/klomptech/jbark/examples/example.woof"
```

Or:

```bash
./gradlew compileJava
java -cp build/classes/java/main dev.klomptech.jbark.Bark src/main/java/dev/klomptech/jbark/examples/example.woof
```

Run with no arguments to type a program at the `>` prompt (end with Ctrl+D).

---

## "Hello, World"

"Hello, World" in Bark looks like this:

```woof
bark "Hello, World"
```

(prints: `Hello, World`)

Plain `bark` prints the value as-is. Other dogs have their own style — see below.

---

## Printing

Dogs don't just bark the same way every time. These are all valid print verbs:

```woof
bark "Hello from bark"       (prints: Hello from bark)
woof "Hello from woof"       (prints: Woof! Hello from woof)
growl "Hello from growl"     (prints: HELLO FROM GROWL)
howl "Hello from howl"       (prints: Awoooo~ Hello from howl)
whine "Hello from whine"     (prints: (Hello from whine))
yapping "Hello from yapping" (prints: Hello from yapping! Hello from yapping!)
whining "Hello from whining" (prints: …Hello from whining…)
```

`whimper` is also a print alias (same style as `whine`).

Every print verb takes exactly one value after it: a string, number, boolean, or variable.

---

## Values

Bark supports:

- **Strings** — double-quoted, one line: `"hello"`
- **Numbers** — integers and decimals: `42`, `3.14`
- **Booleans** — `true`, `false`

```woof
bark 42
bark 3.14
bark true
bark false
```

---

## Variables

Assign by putting a name first, then a value. No `=` — this is a word language:

```woof
bones 5
dogName bones
bark dogName
```

(prints: `5`)

The right-hand side can be a literal, another variable, or stdin (see below). The source variable must already exist — `dogName bones` looks up `bones` in the yard.

---

## Reading from stdin

To read a line of input into a variable, put the name first and an input verb second:

```woof
dogName listen
bark dogName
```

When the program reaches `dogName listen`, it reads one line from stdin and stores it in `dogName`.

Other input verbs (all do the same thing):

- `listen`
- `sniff`
- `perk`
- `hear`
- `eavesdrop`
- `wait`

```woof
age sniff
bark age
```

**Note:** `listen dogName` is **not** valid — the dog needs a name to hold the treat first. Use `dogName listen`.

---

## Line model

- **One statement per line.**
- Blank lines are fine.
- Extra words on a line are an error (the pack only handles one treat at a time).

---

## Comments

Bark supports `//` line comments and `#` line comments:

```woof
// This is a comment
bark "hi"

# This works too
bark 42
```

Inside strings, `//` and `#` are **not** comments — they stay in the value:

```woof
bark "visit https://example.com"
bark "color #ff0000"
```

---

## Command line

| Argument | Effect |
|----------|--------|
| `--version`, `-version` | Display the Bark version and exit. |
| `[script]` | Run a `.woof` file. |
| *(no arguments)* | Read a program from stdin (REPL-style). |

```bash
bark --version
# bark 0.1.0
```

---

## Funny errors

Bark does not do boring compiler messages. When something goes wrong, the pack tries to tell you in character — you can usually guess what happened if you know the language, but clarity is not the primary goal. Entertainment is.

Format:

```
Line 3 — <dog-themed message>
```

### Examples

**Unknown statement**

```woof
fetch stick
```

```
Line 1 — "fetch"? The dogs don't know that trick yet.
```

**Bad character (no `=` in Bark)**

```woof
dog1 = 2
```

```
Line 1 — Sniffed a '='. Dogs don't bury symbols like that.
```

**Listen without a name**

```woof
listen
```

```
Line 1 — A dog can't listen at nothing — point those ears at a name first, like dogName listen.
```

**Undefined variable**

```woof
bark missingDog
```

```
Line 1 — No scent of "missingDog" anywhere.
```

**Too many words on one line**

```woof
bark "hi" extra
```

```
Line 1 — Too many treats on one line: "bark" can only handle one thing, not "extra".
```

**Unclosed string**

```woof
bark "hello
```

```
Line 1 — You started a howl with " but never finished, dogs are still waiting.
```

---

## Not implemented yet

- `if` / `else`
- Word-based expressions (`plus`, `minus`, `times`, …)
- Commands (`eats`, `grows`, `shares`)
- Loops

See `docs/spec.md` for the broader language vision (original interpreter).

---

## Example program

A full sample lives at `src/main/java/dev/klomptech/jbark/examples/example.woof`. It covers print styles, literals, assignment, comments, and stdin.

```bash
java -cp build/classes/java/main dev.klomptech.jbark.Bark \
  src/main/java/dev/klomptech/jbark/examples/example.woof
```

When it hits `dogName listen`, type a name and press Enter — the next line will bark it back.
