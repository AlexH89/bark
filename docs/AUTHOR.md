# Writing Bark stories

Bark is a small **esoteric** language dressed up as short dog stories. It is meant to be ridiculous and fun, not practical or serious. You get assigments, branches, loops, stashes, piles, math, and tricks. That should also be enough for the usual "is this Turing complete?" thing that is appartantly part of writing esoteric languages! I did not deep dive into that topic though, so it might as well not be true as well.

The idea: dogs hear *blah name blah blah walk blah blah* and only reacts to the words that they recognise, the rest gets ignored completely. So to me that is how Bark should behave as a language as well. It ignores almost everything that is not a registered name, a keyword, or a value instead of throwing errors. You wrap real commands in filler if you want just so it reads nicer.

**Also useful:** `[MANUAL.md](MANUAL.md)` (full reference) · `[tutorial.woof](../examples/woof/tutorial.woof)` · `[bimba.woof](../examples/woof/bimba.woof)`

---

## Story mode vs script mode

Two ways to write Bark programs.


| Story                                                             | Script (no filler)                            | Same meaning           |
| ----------------------------------------------------------------- | --------------------------------------------- | ---------------------- |
| `her cookie jar holds "biscuit", "stick"`                         | `cookie_jar holds "biscuit", "stick"`         | fill a stash           |
| `she whimpers as she wants the first biscuit from her cookie jar` | `whimper first from cookie_jar`               | print first stash item |
| `her cookie jar is empty`                                         | `cookie_jar empty`                            | clear a stash          |
| `her cookie jar drops first`                                      | `cookie_jar drops first`                      | remove front slot      |
| `she woof how many toys she has`                                  | `bark how many toys the labrador has`         | print toy count        |
| `when she has 2 toys then she woofs "Ready."`                     | `when labrador has 2 toys then bark "Ready."` | branch                 |
| `for each treat from her cookie jar then` …                       | `for each treat from cookie_jar then` …       | loop over stash        |
| `the labrador shares with the golden retriever`                   | `labrador shares with golden_retriever`       | split toys             |
| `the laundry basket holds "shirt", "sock"`                        | `laundry_basket holds "shirt", "sock"`        | fill a pile            |
| `I bark what is on top of the laundry basket`                     | `bark top of laundry_basket`                  | peek pile top          |


**Story:** spaces instead of underscores (`cookie jar`), optional filler (`the`, `as she wants`, even `stash` or `pile` if you like more technical jargon). None of that changes the behaviour of the language.

**Script:** registry names with underscores, heard keywords only, no filler. Do not write `cookie_jar stash`. The name `cookie_jar` is already enough. Use `empty` to clear a stash (or its synonyms like `starving`). Useful for tests and `examples/scratch/`.

---

## What the dogs hear


| Heard                                                   | Not heard (filler/glue)                                                                  |
| ------------------------------------------------------- | ---------------------------------------------------------------------------------------- |
| Breeds, objects, stashes, piles from the registry files | Basically everything else: `is`, `has`, `my`, `the`, `a`, `items`, `stash`, `pile`, etc. |
| Keywords: `when`, `bark`, `holds`, `shares`, `bury`, …  | No separate ignore list. If it is not heard, it is filler.                               |
| **memory** / **journal** (two program-wide variables: number and text) |                                                                                          |
| Strings, numbers, traits                                |                                                                                          |


```woof
I have a labrador.
She is 2.
She has 2 toys.
Her name is "Bimba".
she woofs how many toys she has   → prints 2
```

Meaning comes from **where** words sit on the line (who, what field, what value), not from filler like `is` or `has`.

A single breed on a line still registers it:

```woof
labrador
I have a labrador.
She is 2.              → age = 2
She has 2 toys.        → toys = 2
He is 5 years old      → age = 5
Her name is "Bimba".   → name
my corgi is loud       → loud trait
when she has 2 toys then …  → branch (when must start the line)
```

Filler never triggers behaviour by itself.

**Debugging:** `--strict` warns on lines that did nothing. `--quiet` hides the banner (you will know what I mean by this when you run your first program!) when you print/process output.

---

## One line per statement (mostly)

Each line is usually **one** command. That is the default model. Dogs are simple creatures and would struggle with long commands, so the language should be simple as well.

Exceptions (to keep the language semi-useful):

- **Multiline blocks** after `when` / `while` / `for each` / tricks, closed with `bury` (or `enough`, `goodnight`, etc.)
- **One-liner branches** like `when she has 1 toy then she woofs "Hi" otherwise she woofs "Nope"` with no `bury`

So it is not "only single-line if ever". It is "one statement per line, and a block is a bunch of lines between `then` and `bury`."

---

## List slots: `first`, `second`, `last`

These **are** heard keywords. The dog reacts to them because they pick a stash slot.

They only do something useful in stash/pile context (`drops first`, `first from cookie jar`, etc.). On a random story line they are just words that happen to be heard, like `top` on a pile.

**Not** heard: spelled-out numbers used as slots (`one`, `two`, `twenty third`). Those still work in stash lines via `StashSpots`, but stay filler globally so they do not collide with normal language/filler.

---

## Dog fields (nine stored, many story words)

When you mention a breed (`labrador`, `I have a beagle`), you are **registering a dog**. The breed name is the handle. It comes from `breeds.txt`. It is **not** one of the nine stored fields. There is no `breed` counter you set with `She has 2 breeds`.

Each registered dog keeps **nine values** in memory (age, name, toy count, and so on). Story words are **aliases** for those fields. The parser maps `toys` → `items`, `treats` → `food`, and so on.


| Stored field  | Type   | Story aliases (examples)                                                      |
| ------------- | ------ | ----------------------------------------------------------------------------- |
| `food`        | number | `treat`, `snacks`, `cookie`, `biscuit`, etc. `**she steals a treat`**         |
| `items`       | number | `toy`, `toys`, `walk`, `nap`, `friend`, `squirrel`, etc. `**She has 2 toys`** |
| `inventory`   | text   | `belongings`, a comma list of held things (`ball`, `stick`)                   |
| `age`         | number | `**She is 2`**, `**labrador 5`**, `years old`                                 |
| `name`        | text   | `**name**`, `**nickname**` only                                               |
| `description` | text   | `owner`, `collar`, `tag`, `family`                                            |
| `color`       | text   | `color`, `species`, `gender`                                                  |
| `happy`       | yes/no | `goodboy`, `happy`, `trained`, etc.                                           |
| `fed`         | yes/no | `fed`, `groomed`, `washed`                                                    |


### Treats vs toys (two counters)

Dogs track **food** and **items** separately.

- **Food** is edible stuff: treats, snacks, biscuits. Lines like `she steals a treat` or `my labrador pinches a treat` add to **food**.
- **Items** is everything else you count on a dog in stories: toys, walks, naps, squirrels, friends. `She has 2 toys` and `she finds a toy` touch **items**.

That split is deliberate. Stories talk about treats and toys differently, and some personality rules only care about food (see **Traits** below).

### World objects (`ball`, `bone`, etc.)

Names in `objects.txt` are **world counters**, not dog fields.

```woof
ball 5              → five balls exist in the story world (global count)
I bark ball         → prints 5 (script mode, can be made to sound more 'human' using filler)
```

That is separate from a dog's **inventory** field (what this dog is holding as a comma list). You can also ask how many of an object a dog has via inventory, like counting balls they carry.

So: **dog fields** = per-breed state. **Registry objects** = global named numbers. **Stashes/piles** = lists of values. Three different features.

---

## Comparisons in conditions

Less-than: `*sniffs`* + `*less`*. Greater-than: `*has more than`*.


| You want             | Write                                    |
| -------------------- | ---------------------------------------- |
| loop while toys < 8  | `while she sniffs less toys than 8 then` |
| branch when toys > 2 | `when she has more than 2 toys then`     |


Details: `[MANUAL.md` §8](MANUAL.md#8-conditions).

---

## Branches (`when` / `if`)

`when` is a real keyword, but only when it **starts the line**:

```woof
when she has 2 toys then she woofs "Ready."     → branch
bark "They left when the jar was empty."        → print only
The sun set when they reached the gate.         → story-only (--strict warns)
```


| Where `when` is        | Effect                                             |
| ---------------------- | -------------------------------------------------- |
| First word on the line | Branch                                             |
| After `otherwise`      | Next branch: `otherwise when she has 1 toy then …` |
| Inside `"quotes"`      | Printed text                                       |
| Anywhere else          | Ignored for control flow                           |


Loops work the same way: `while`, `until`, `for each` must lead the line.

---

## Pronouns


| Word                 | Resolves to                         |
| -------------------- | ----------------------------------- |
| `she` / `her`        | Last dog you spoke about as she/her |
| `he` / `him` / `his` | Last dog as he/him/his              |
| `it`                 | Last stash, pile, or object         |
| `they` / `them`      | Filler                              |


```woof
I have a labrador.
She is 2.
I have a beagle.
she passes a toy to the beagle    → labrador gives, not beagle
```

---

## Stash vs pile

**Stash** = ordered 'jar'. Slots matter (`first`, `second`, `last`).

Story:

- `her cookie jar holds "a", "b", "c"`
- `her cookie jar stows "stick"`
- `her cookie jar drops first`
- `for each treat from her cookie jar then` … `bury`

Script:

- `cookie_jar holds "a", "b", "c"`
- `whimper first from cookie_jar` (ordinal before `from`)
- `cookie_jar empty`

**Pile** = stack (last in, first out).

Story: `the laundry basket holds "a"`, `I bark what is on top of the laundry basket`  
Script: `laundry_basket holds "a"`, `bark top of laundry_basket`

---

## Synonyms

Learn one phrase, swap words when it sounds better.


| Idea      | Start here           | Also works                    |
| --------- | -------------------- | ----------------------------- |
| Equals    | `matches`            | `has`, `is`, `are` (assign)   |
| Greater   | `growls louder than` | `bigger`, `more`              |
| End block | `bury`               | `enough`, `goodnight, done`   |
| Add toy   | `finds a toy`        | `gets`, `fetches`, `collects` |
| Add treat | `feeds a treat`      | food verbs, not toy verbs     |
| Lose toy  | `misplaces a toy`    | `tosses`, `hides`             |
| Eat treat | `gulps a treat`      | not toy verbs                 |


---

## Shortcuts


| Line                                            | Meaning                                                                                  |
| ----------------------------------------------- | ---------------------------------------------------------------------------------------- |
| `the labrador shares with the beagle and husky` | Split toys. Remainder stays with giver (`[MANUAL.md` §3](MANUAL.md#3-runtime-semantics)) |
| `the labrador passes a toy to the beagle`       | One toy, one friend                                                                      |
| `her cookie jar is empty`                       | Clear stash                                                                              |
| `bark bark bark`                                | Paw print easter egg                                                                     |


---

## Traits (personality, not fields)

Traits are **optional flags** on a dog. They are not part of the nine fields above. They change how the interpreter behaves when that breed does things.

Defaults live in `[traits.json](../src/main/resources/traits.json)` (60 breeds get semi-realistic defaults). You can also set or clear them in a story:

```woof
my corgi is loud
my beagle is greedy
my labrador is not greedy
```


| Trait     | What it does in practice                                                                                                                                                                                                                           |
| --------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `loud`    | When the **dog** speaks (`labrador barks`, `she woof`), output goes through growl (UPPERCASE). Narrator `bark "…"` stays normal.                                                                                                                   |
| `greedy`  | After **3** feed/gulp/share actions **in a row** on that dog, they lose **1 treat** from **food** (not toys). Counter resets after the penalty. Beagles and labradors default as greedy because...well... you know.                                |
| `lazy`    | `wait` sleeps twice as long.                                                                                                                                                                                                                       |
| `wet`     | `whine` / `whimper` output is printed twice.                                                                                                                                                                                                       |
| `fetchy`  | Printing the **same** text twice in a row adds a proud fetch line on stdout, then prints again.                                                                                                                                                    |
| `chaser`  | Each loop tick (`while` / `until` / `for each`) adds **+1 toy** to that dog's **items** count. Story word `squirrels` is the same field and can be used to get the current count like `how many squirrels she has`.                                |
| `playful` | When the **dog** speaks (`border collie barks`, `she woof`), output characters are **reversed** before formatting. Narrator `bark "…"` stays normal. Default on bearded collie, border collie, flat-coated retriever, cockapoo, shetland sheepdog. |


**Mood (no trait required):**

- **Dog speaks** (`she barks`, `labrador woofs "…"`): you still write `bark` / `woof`, but if that dog has **0 toys**, output is **lowercase** (whine formatting). If they are `loud`, **uppercase** instead.
- **Narrator** (`bark "…"`, `I bark "…"`): no dog subject before the verb → text prints **as written**. Mood never applies.

Full runtime detail: `[MANUAL.md` §3](MANUAL.md#3-runtime-semantics) and [§10 Traits](MANUAL.md#10-traits).

## Comments

```woof
// scene note
# act two
```

---

## Debugging

1. `bark --list-breeds` and `bark --list-objects`
2. Typos get fuzzy hints (*Did you mean cookie jar?*)
3. Errors are dog-themed and prefer dog themed over usefulness. Sorry, not sorry.
4. Short branches can live on one line. Longer scenes use `bury`
5. Compare with `[tutorial.woof](../examples/woof/tutorial.woof)` and `[MANUAL.md](MANUAL.md)`

---

## Example

```bash
./bark examples/woof/tutorial.woof          # release or after ./gradlew shadowJar
./gradlew run --args="examples/woof/tutorial.woof"   # from a clone
```

`[tutorial.woof](../examples/woof/tutorial.woof)`: Bimba at the park. Scratch work goes in `examples/scratch/` (gitignored).