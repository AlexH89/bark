# Bark

**A dog-themed esoteric programming language that reads like a short story.**

**Repository:** [github.com/AlexH89/bark](https://github.com/AlexH89/bark)

```
    ___     __
  /(. .)\     )
   (*)_____/|
   /        |
  /    |--\ |
 (_)(_)   (_)
```

## In short

Bark is a **dog-themed esoteric programming language**: you write `.woof` files that read like short stories, and an interpreter runs them. Hobby project, not production. Dogs only react to words they recognise; everything else is story filler.

You get variables (dogs, objects, cookie jars), `when` branches, `while` loops, lists, stacks, math, functions called “tricks”, stdin, traits like `greedy` and `fetchy`, and many ways to print (`bark`, `woof`, `howl`, `whimper`, `yapping`, …). Enough for the esoteric-language bingo card. Not enough to run your startup.

**Sample** — this story prints `3` (periods and words like `is` / `has` are optional filler):

```woof
I have a labrador.
She is 2.
She has 3 toys.
Her name is "Bimba".
she woofs how many toys she has
```

```
3
```

**Try it** — pick an interpreter:

| You have… | Do this |
|-----------|---------|
| **Release zip** from [GitHub Releases](https://github.com/AlexH89/bark/releases) | Unzip (`bark`, `bark.cmd`, JAR). Run **your own** story: `./bark mystory.woof` — the zip does **not** include `examples/`. Requires **Java 25+**. |
| **A clone** (Java) | `./gradlew run --args="examples/woof/bimba.woof"` or, after `./gradlew shadowJar`, `./bin/bark examples/woof/bimba.woof` |
| **A clone** (Python) | `cd pbark && pip install -e .` then `./bin/pbark ../examples/woof/bimba.woof` — requires **Python 3.10+** |

On Windows use `bark.cmd` / `gradlew.bat` instead of `./bark` / `./gradlew`.

More: [docs/AUTHOR.md](docs/AUTHOR.md) · [docs/MANUAL.md](docs/MANUAL.md) · [examples](examples/README.md)

---

## How we got here

Sometimes things just click together. As head of a QA department you obviously code slightly less than in my times as a developer or SDET. Because of this, I find it important to keep my programming skills sharp. Luckily, this is usually not hard to do. Apart from exposure at work, I also often have people around me that need scripts or projects to solve specific problems in their life. On top of that, I often work on my own projects or games. 

But not this particular evening. I had been without inspiration for a while and the same time a nostalgic urge to work with Java again (you don't often use this as QA anymore these days). But on what...

Then I overheard my wife, who was complaining about how unfriendly a certain programming language or framework (which shall not be named) feels to read and use. This gave me the inspiration I needed. As a kid I always wanted to write my own programming language, but never tried it as it is such a difficult and daunting project to pick up. However, I also recently watched a really good presentation that included the topic of the esoteric programming language [Rockstar](https://codewithrockstar.com/). If you have never heard of it, look it up. I have had lots of fun playing around with it. So there it was, the project I could work on. An esoteric language, written in Java, based on something my wife could enjoy. The topic was now clear, it just had to be about dogs...

To be clear: **Bark is a hobby project and not to be taken seriously.** It exists because I wanted to brush up my skills, write Java again after too long away, finally scratch the “build your own language” itch, and do something silly that still counts as a real technical challenge. It is **not** production software. It will not replace Java or Python. Yet… It might make you smile. If anyone has a good time playing around with it, I consider it a huge success.

---

## Esoteric...what?

Good question. I didn't know that either before I saw the presentation on [Rockstar](https://codewithrockstar.com/), which then reminded me of Brainfuck which was mentioned when I was still in University.

### What is an esoteric language?

Normal languages optimise for clarity, speed, or usability. **Esoteric languages** optimise for a certain joke, a puzzle, or a concept. For example, some well-known languages ask: *what if code looked like a rock ballad, a cooking recipe, or only emoticons?* They are programming as an art project, not as a day-to-day tool.

Bark is definitely in that camp, but then obviously more as a joke and not as art (let's not take ourselves too seriously here!).

### “Turing complete”? You wrote a self conscious program?

Yes... No, unfortunately not. Most of the languages I looked at included a turing completion test. It felt to me like an inside joke or a 'rite of passage'. So obviously I then have to include it as well, can't be breaking tradition after all! So what does it mean? A language is **Turing complete** when it can compute anything a general computer can compute, given enough time, memory, and patience. Branches, loops, variables, and some way to do arithmetic usually get you there. It is the informal “this is a real programming language, not a fancy calculator” badge.

Bark has `when`, `while`, `until`, `for each`, tricks (functions), stashes (lists), piles (stacks), math, and shared state. That is the usual checklist. I did not write a formal proof at 2 a.m like some others mentioned they did. I **did** write [counter-tradition.woof](examples/woof/counter-tradition.woof), which is my version of the inside joke for people who remember/know Brainfuck. Treat “Turing complete” here as big *maybe, definitely not the point*. I did not look into it too much, so there is a pretty big chance people reading this will argue it isn't turing complete at all.

### How Bark fits the genre


| Esolang tradition             | Bark’s version                                                                         |
| ----------------------------- | -------------------------------------------------------------------------------------- |
| Weird syntax                  | Story/English driven with filler everywhere                                            |
| One memorable gimmick         | Dogs ignore words they do not know, dog specific behaviour and reserved variable names |
| Surprisingly real semantics   | Assignments, loops, stashes, piles, traits, moods                                      |
| Absolutely not for production | Correct                                                                                |


If you want more in-depth explanations, read [docs/AUTHOR.md](docs/AUTHOR.md) (how to write stories) and [docs/MANUAL.md](docs/MANUAL.md) (full reference).

---

## Flavours & a real programming language?

Most people that read the first chapter of this page would probably have thought: "Java?? That is a terrible choice for what you are trying to do". They are probably not wrong. I did want to use Java again, which made it a logical choice for my hobby project, but I also felt I would end up needing something else as well (being Python).

But first, let me explain something to people that have never done something like this before as well. You might be confused about why Java and Python are mentioned at all. Were you not building a NEW language? Well, yes and no. You can build a new language, but something still has to run it. That "something" is a program you have to write, in a language that already exists. For a not so serious language a high level language like Java (or Python) is more than enough as speed and efficiency are not that relevant. So:

- **My language** (Bark) = what users write (`.woof` files)
- **Java or Python** = the tool that reads Bark, knows what to do with it and then runs those files

So Java/Python isn’t Bark. They are just the toolbox I use to build Bark’s interpreters. Now, there are three flavours you can pick from when building the tools that run your language: compilers, interpreters and transpilers. I won't go into it too much as I am not an expert on the topic at all, but in short:

- **Compiler**: turns a program into another form *before* running (like machine code or `.class` files)
- **Interpreter**: reads a program and runs it step by step, no separate output file
- **Transpiler**: converts a language into *another* language (like Bark → JavaScript), then that runs

Small, weird languages like Bark are just for fun and experiments. An interpreter is **fast to build**, **easy to change**, and **good enough** as  you don’t need the speed or complexity of a full compiler. So we ended up with these two interpreters:

- **jbark** — the Java interpreter (`./bin/bark`, Gradle, release JAR). The reference implementation.
- **pbark** — the Python interpreter (`./pbark/bin/pbark`, `python -m pbark`). A **semi-automated port** of jbark: same behaviour in intent, but translated from the Java source. Jbark is leading if the two ever diverge.

---

## Bark behaviour

The first question to ask was how the language should behave. What would make it unique, different, hopefully funny? As the language was going to be dog themed, it also had to behave like one. That immediately defined the first two behaviours. The language has to feel like written or spoken English, not with heavy syntax or special characters as that has nothing to do with dogs. Woof files need to read like short stories and be flexible. Then the other: real dogs do not parse English grammar. In fact, they only react to words they recognise. When you ask a dog if they want to go for a walk, what they will hear is: "Bla bla Pepon bla bla bla walk bla bla?". They latch onto the words they know, their name and *walk*, and treat everything else as background noise. That is exactly what the language has to do. It won't throw syntax errors on unknown keywords but just completely ignore them. Where most esoteric languages have lists of included/excluded and allowed filler keywords, Bark does the opposite. It ignores everything it doesn't recognise.

Examples:


| The runtime **reacts** to                          | Everything else is **story filler**            |
| -------------------------------------------------- | ---------------------------------------------- |
| Registered names (breeds, `ball`, `cookie jar`, …) | `I`, `my`, `the`, `a`, `is`, `has`, `stash`, … |
| Keywords (`bark`, `when`, `shares`, `bury`, …)     | filler you add so lines read naturally         |
| Values (numbers, `"strings"`, `yes` / `no`)        | punctuation, articles, dramatic pauses         |


There is **no separate ignore list**. If a word is not *heard*, it is ignored. Meaning comes from **structure:** who the line is about, which keyword was fired, what value sits at the end.

That is why `my labrador has 3 toys` and `labrador 3 toys` can mean the same thing, and why `She is 2.` sets her **age** while `She has 2 toys.` sets her **toy count**. The parser cares about placement above all.

Multi-word registry names use spaces in stories (`cookie jar` → `cookie_jar` internally). Errors are dog-themed (`No scent of "missing" anywhere.`). Of course they are. They are also less useful because of this. Sorry, not sorry!

To keep the story dog themed, there is also a limitation to variable names that can be used. Only dog breeds can be used as variable names and they come with a limited set of attributes that can be used and altered (with a few exceptions). These dog breeds can also come with unexpected quirks and behaviour, so be careful with choosing which one to use!

Final need to know: Bimba and Pepon are names you will see often in the examples and tutorials. They are my wife's dogs — Bimba a really intelligent and hungry labrador, Pepon… nobody knows. He is small, simple, but sweet.

---

## Install and run

Bark ships two interpreters that run the same `.woof` programs. Pick whichever runtime you have handy.

### Java (jbark) — requires Java 25+

Check: `java -version`

#### Download a release (easiest)

Grab `bark-1.0.0.zip` (or the loose JAR + scripts) from [GitHub Releases](https://github.com/AlexH89/bark/releases). Unzip so `bark`, `bark.cmd`, and `bark-1.0.0-all.jar` sit in the same folder.

The release contains the **interpreter only** — not the example stories. Point it at your own file:

```bash
./bark mystory.woof
./bark --help
./bark --version
```

On Windows:

```bat
bark.cmd mystory.woof
```

Want the shipped examples (`bimba.woof`, `tutorial.woof`, …)? **Clone this repo**, then either use Gradle or the dev launcher:

```bash
git clone https://github.com/AlexH89/bark.git
cd bark
./gradlew run --args="examples/woof/bimba.woof"
# or after ./gradlew shadowJar:
./bin/bark examples/woof/bimba.woof
```

The `bark` / `bark.cmd` scripts are thin wrappers around `java -jar …`; you can call the JAR directly:

```bash
java -jar bark-1.0.0-all.jar mystory.woof
```

Pipe a story from stdin (Ctrl+D on Mac/Linux, Ctrl+Z then Enter on Windows when done):

```bash
./bark
```

**Homebrew (later):** a personal tap (`brew install AlexH89/tap/bark`) does not need Homebrew-core approval. Getting into **homebrew-core** is the slow path (popularity thresholds, review). A tap is fine for a hobby project whenever you want it.

#### Build from source (developers)

Clone [github.com/AlexH89/bark](https://github.com/AlexH89/bark), then Gradle runs the interpreter without packaging:

```bash
./gradlew run --args="examples/woof/bimba.woof"
./gradlew shadowJar   # writes build/libs/bark-1.0.0-all.jar
./bin/bark examples/woof/bimba.woof   # launcher finds build/libs/
./gradlew dist        # same JAR + launchers in build/dist/
./gradlew test
```

On Windows, use `gradlew.bat` and `bin\bark.cmd`.

When you are ready to publish: [docs/RELEASE-JAVA.md](docs/RELEASE-JAVA.md) (GitHub Releases, Maven Central, Homebrew).

### Use as a Java library (Maven Central, later)

Other Java projects can depend on the **library JAR** (not the fat `-all` CLI):

```gradle
repositories { mavenCentral() }

dependencies {
    implementation("dev.klomptech:bark:1.0.0")
}
```

That embeds the parser and interpreter in your own app. It does **not** install a global `bark` command — for that, use the release zip or put `bin/bark` on your `PATH`.

### Python (pbark) — requires Python 3.10+

**Note:** pbark is a semi-automated port of jbark. It passes the same test suite, but it was generated/translated from the Java codebase rather than written independently. jbark remains the source of truth.

Check: `python3 --version`

From a clone:

```bash
cd pbark
python3 -m venv .venv
source .venv/bin/activate   # Windows: .venv\Scripts\activate
pip install -e .
./bin/pbark ../examples/woof/bimba.woof
```

Or without activating the venv (from repo root, after `pip install -e pbark/`):

```bash
./pbark/bin/pbark examples/woof/goodboy.woof
python3 -m pbark examples/woof/tutorial.woof
```

Same flags as jbark: `--help`, `--version`, `--strict`, `--quiet`, `--list-breeds`, `--list-objects`.

**Run the Python tests:**

```bash
cd pbark
pip install -e ".[dev]"
pytest tests/ -q
```

See [pbark/README.md](pbark/README.md) for more. When you are ready to publish: [docs/RELEASE-PYTHON.md](docs/RELEASE-PYTHON.md).

---

## Quick start (from a clone)

If you downloaded only the release zip, see **Install and run** above — you need a clone for the `examples/` folder.

**Hello world:**

```bash
./gradlew run --args="examples/woof/goodboy.woof"
# or with pbark:
./pbark/bin/pbark examples/woof/goodboy.woof
```

**Best place to start**: Bimba the labrador's adventures

```bash
./gradlew run --args="examples/woof/bimba.woof"
# or:
./pbark/bin/pbark examples/woof/bimba.woof
```

**Learn the syntax**: a kind of guided tour:

```bash
./gradlew run --args="examples/woof/tutorial.woof"
# or:
./pbark/bin/pbark examples/woof/tutorial.woof
```

**Type lines interactively**:

```bash
./gradlew run
# or:
./pbark/bin/pbark
```

**Useful flags:**

```bash
./gradlew run --args="--help"
./gradlew run --args="--list-breeds"
./gradlew run --args="--list-objects"
./gradlew run --args="--strict examples/woof/bimba.woof"   # warn on lines that did nothing at all
./gradlew run --args="--quiet examples/woof/bimba.woof"    # hide banner and goodbye
./gradlew run --args="--version"
```

**Run the tests** (yes, there are tests; even silly languages need accountability):

```bash
./gradlew test          # Java (jbark)
cd pbark && pytest -q   # Python (pbark)
```

---

## Smallest possible program

```woof
bark "Hello, World"
```

Bare `bark` with no value prints a paw. I was serious about the bit where you should not take this too seriously.

---

## Other design choices (the short tour)

**Registry, not free-form names.** Breeds, objects, stashes, and piles must exist in text files (`breeds.txt`, `objects.txt`, …). You cannot invent `her snack tin` until you add it to the world. That keeps stories in a shared dog universe instead of turning into generic scripting.

**Story mode vs script mode.** Shipped examples read like normal language (`her cookie jar`, `as she wants`). Tests and scratch files use registry names (`cookie_jar holds "stick"`). Same language, different look.

**Two story globals.** `memory` (a whole number) and `journal` (text) for plot counters and diary lines, just to make the language slightly more useful and to offer a bit of freedom. Not a general variable system.

**Traits change behaviour.** `greedy` dogs lose treats if you feed them too often. `fetchy` dogs comment when you print the same line twice. `playful` dogs reverse their dialogue. `lazy` dogs take twice as long on `wait`. Narrator `bark "hi"` stays normal; `labrador barks "hi"` might not.

**Limited usefulness, by design.** No modules, no package manager, no file I/O beyond stdin, no online runner yet. Bark is a playground: Java/Python practice, parser/interpreter exercise, and to try and write code that can be readable *and* ridiculous.

---

## Example programs


| Program | Why run it |
| ------- | ---------- |
| [goodboy.woof](examples/woof/goodboy.woof) | Hello world — Pepon the dachshund |
| [bimba.woof](examples/woof/bimba.woof) | Main story: traits, voices, sharing |
| [tutorial.woof](examples/woof/tutorial.woof) | Syntax tour at the park |
| [counter-tradition.woof](examples/woof/counter-tradition.woof) | Esolang tradition, explained with tail wags |

More notes: [examples/README.md](examples/README.md)

---

## Documentation


| Doc | For |
| --- | --- |
| [docs/MANUAL.md](docs/MANUAL.md) | Grammar, registries, runtime rules |
| [docs/AUTHOR.md](docs/AUTHOR.md) | Writing stories, voice, filler, tips |
| [docs/RELEASE-JAVA.md](docs/RELEASE-JAVA.md) | Ship jbark — **done in repo:** workflow + JAR build. **You still do:** run Release workflow; optional Maven Central + Homebrew |
| [docs/RELEASE-PYTHON.md](docs/RELEASE-PYTHON.md) | Ship pbark — **done in repo:** interpreter, tests, bundled resources. **You still do:** PyPI account + `twine upload` |
| [examples/README.md](examples/README.md) | Runnable tour |


---

## Registry files (the dog world)


| File                             | Holds                                       |
| -------------------------------- | ------------------------------------------- |
| `src/main/resources/breeds.txt`  | Dog breeds                                  |
| `src/main/resources/objects.txt` | World objects (`ball`, `bone`, …)           |
| `src/main/resources/stashes.txt` | Named lists (`cookie_jar`, `toy_basket`, …) |
| `src/main/resources/piles.txt`   | LIFO piles (`laundry_basket`, …)            |
| `src/main/resources/traits.json` | Default breed personalities                 |


---

## License

[GNU AGPL-3.0](LICENSE): free to use, share, and hack on. Derivatives and network services must stay open and credit [Alex Hovenkamp](docs/AUTHOR.md) as the original author. See [NOTICE](NOTICE).

---

*Built with Java and Python, too much coffee, and constant wondering if I am not overdoing it now...*