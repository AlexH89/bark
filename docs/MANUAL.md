# Bark language manual

Reference for what the parser accepts today. Bark is an **esoteric** dog-story language. It is for fun, not to be taken seriously.

**How lines work:** usually one statement per line. Multiline `when` / `while` / `for each` / trick bodies are the main exception. They run from `then` until `bury` (or `enough`, `goodnight`, …).

**How hearing works:** the runtime only reacts to registered names, keywords, and values. Everything else is story glue, like a dog ignoring *blah name blah walk blah* until it hears *walk*. See `[AUTHOR.md](AUTHOR.md)` for writing tips.

**Also:** `[AUTHOR.md](AUTHOR.md)` (writing stories)

---

## How to read this manual


| Doc                       | Purpose                                       |
| ------------------------- | --------------------------------------------- |
| `[AUTHOR.md](AUTHOR.md)`  | Story vs script, voice, why glue exists       |
| **MANUAL.md** (this file) | Grammar tables, registry names, runtime rules |


Each feature has:

- **Line**: something you can write
- **Parses as**: AST shape (for hackers)
- **Need to know**: runtime rules

Expression shorthand: `Field(she, items)`, `StashAccess(cookie_jar, COUNT)`, `Binary(PLUS, …)`.

---

## 1. Running Bark

Bark has two interpreters: **jbark** (Java) and **pbark** (Python). They accept the same `.woof` syntax and should produce the same output.

### Java (jbark)

**Release** (after unzipping [GitHub Releases](https://github.com/AlexH89/bark/releases); Java 25+):

| Command                             | Need to know                             |
| ----------------------------------- | ---------------------------------------- |
| `bark story.woof`                   | Run a file (`./bark` on Mac/Linux)       |
| `bark`                              | Read from stdin until EOF                |
| `bark --help` or `bark -h`          | Show usage                               |
| `bark --version` or `bark -version` | Show version and a dog fact              |
| `bark --list-breeds`                | List every breed in `breeds.txt`         |
| `bark --list-objects`               | List every object in `objects.txt`       |
| `bark --strict story.woof`          | Warn on story-only lines that do nothing |
| `bark --quiet story.woof`           | Hide startup banner and goodbye          |

Windows: `bark.cmd` instead of `bark`. The launchers wrap `java -jar bark-*-all.jar`.

**From a clone** (developers):

Gradle: `./gradlew run --args="examples/woof/tutorial.woof"`

After `./gradlew shadowJar`: `./bin/bark examples/woof/tutorial.woof` (or `./gradlew dist` then `./build/dist/bark …`).

### Python (pbark)

**From a clone** (Python 3.10+):

```bash
cd pbark && pip install -e .
./bin/pbark ../examples/woof/tutorial.woof
```

Or: `python3 -m pbark examples/woof/tutorial.woof` from the repo root (after install).

| Command                             | Need to know                             |
| ----------------------------------- | ---------------------------------------- |
| `pbark story.woof`                  | Run a file (`./pbark/bin/pbark` on Mac/Linux) |
| `pbark`                             | Read from stdin until EOF                |
| `pbark --help` or `pbark -h`        | Show usage                               |
| `pbark --version` or `pbark -version` | Show version and a dog fact          |
| `pbark --list-breeds`               | List every breed in `breeds.txt`         |
| `pbark --list-objects`              | List every object in `objects.txt`       |
| `pbark --strict story.woof`         | Warn on story-only lines that do nothing |
| `pbark --quiet story.woof`          | Hide startup banner and goodbye          |

Tests: `cd pbark && pip install -e ".[dev]" && pytest tests/ -q`

---

## 2. Registries

Names the parser **hears**. Spaces in stories map to underscores (`cookie jar` → `cookie_jar`).


| Registry | File          | Story example                  | Script name              |
| -------- | ------------- | ------------------------------ | ------------------------ |
| Breeds   | `breeds.txt`  | `labrador`, `golden retriever` | same (spaces become `_`) |
| Objects  | `objects.txt` | `ball`, `5 balls`              | `ball`                   |
| Stashes  | `stashes.txt` | `her cookie jar`               | `cookie_jar`             |
| Piles    | `piles.txt`   | `the laundry basket`           | `laundry_basket`         |
| Memory   | (built-in)    | `memory`                       | whole number             |
| Journal  | (built-in)    | `journal`                      | text                     |


Use `bark --list-breeds` and `bark --list-objects` to print breed and object names from the CLI.

### Why names are registry-bound

Bark is a **dog-themed story language**, not a general-purpose scripting language. To keep stories dog-themed it is important to limit what can be used in the language. Breeds, objects, stashes, and piles must appear in the resource files under `src/main/resources/` before the parser treats them as real names.


| If you write…                                 | What happens                                                     |
| --------------------------------------------- | ---------------------------------------------------------------- |
| `I have a labrador. (with or without period)` | Works: `labrador` is in `breeds.txt`                             |
| `I have a poodle mix.`                        | Fails or warns: `poodle_mix` is not registered unless you add it |
| `her cookie jar holds "biscuit"`              | Works: `cookie_jar` is in `stashes.txt`                          |
| `her snack tin holds "bit"`                   | Fails: invent a stash in `stashes.txt` first                     |


**This is intentional.** The registry keeps stories grounded in a shared dog world. For quick experiments use **script mode** with registry names. See `[AUTHOR.md](AUTHOR.md)`.

**Adding names:** edit the `.txt` files (and optionally `traits.json` for breeds), rebuild, run.

**Not planned:** user-defined variables for breed/object names, `import`, or runtime registration.

### Story variables

Two program-wide variables for story authors, **not** a general variable system:

- `memory`: whole number (`ConfigLoader.MEMORY`). Grammar: [§11 Story variables](#11-story-variables).
- `journal`: text (`ConfigLoader.JOURNAL`). Same assign/print glue as `memory`.

### Breeds

Canonical list: `[src/main/resources/breeds.txt](../src/main/resources/breeds.txt)`. Run `bark --list-breeds` to print every registered name from the CLI.

In stories, spaces become underscores (`golden retriever` → `golden_retriever`). Only breeds in that file can appear in scripts.

### Objects

Canonical list: `[src/main/resources/objects.txt](../src/main/resources/objects.txt)`. Run `bark --list-objects` to print every registered name from the CLI.

World counters like `ball 5` and `I bark ball` only work for names in that file.

Use **feed/eat** verbs for **food** (`treat`, `snack`, `cookie`, …) and **find/lose** verbs for **items** (`toy`, `ball`, …). They update separate counters on the dog.

### Stashes

Canonical list: `[src/main/resources/stashes.txt](../src/main/resources/stashes.txt)`. Story phrases like `her cookie jar` map to registry names (`cookie_jar`). Only stashes in that file can appear in scripts.

### Piles

Canonical list: `[src/main/resources/piles.txt](../src/main/resources/piles.txt)`. Story phrases like `the laundry basket` map to registry names (`laundry_basket`). Only piles in that file can appear in scripts.

### Default traits (`traits.json`)

Breeds listed here get default traits at registration. Other breeds in `breeds.txt` work fine but start with **no traits** until you assign them in the story (`my corgi is loud`) or add an entry here. Traits should reflect real breed tendencies in a playful, stereotype-y way. See existing entries for tone.

```json
{
  "labrador": ["loud"],
  "husky": ["loud", "chaser"],
  "beagle": ["greedy", "fetchy"],
  "corgi": ["greedy", "lazy"]
}
```

---

## 3. Runtime semantics

Rules that are easy to miss from grammar alone. For writing stories, see `[AUTHOR.md](AUTHOR.md)`.

### Story glue vs keywords


| Kind                  | Examples                                     | Behaviour                                                       |
| --------------------- | -------------------------------------------- | --------------------------------------------------------------- |
| **Story glue**        | `is`, `has`, `my`, `the`, `stash`, `pile`, … | Ignored unless heard                                            |
| **Stash slots**       | `first`, `second`, `last`, …                 | Heard keywords. Used in stash grammar ([§19](#19-stashes-list)) |
| **Control flow**      | `when`, `if`, `then`, `while`, `bury`        | Must **lead the line** (except `otherwise when …`)              |
| **Inside `"quotes"`** | anything                                     | Printed text only                                               |


Branch grammar: [§15 Branches](#15-branches-when--if). Story tips: `[AUTHOR.md](AUTHOR.md#branches-when--if)`.

### Sharing toys

When dogs **share toys** (no stash), toys are split with **integer division**. Any **remainder stays with the giver**.

Example: labrador has **7** toys and shares with beagle and husky (3 dogs):


| Dog              | Toys after share        |
| ---------------- | ----------------------- |
| labrador (giver) | **3** (2 + 1 remainder) |
| beagle           | 2                       |
| husky            | 2                       |


Stash sharing round-robins items and clears the stash.

**Story syntax:** `her cookie jar holds …`, `she whimpers as she wants the first biscuit from her cookie jar`, `her cookie jar is empty`. Story phrasing: `[AUTHOR.md` § Story mode](AUTHOR.md#story-mode-vs-script-mode). Stash grammar: [§19 Stashes](#19-stashes-list).

### Greedy trait

Default: after **3** feeds, gulps, or shares in a row, a **greedy** dog loses **1 treat** (food only; see `TraitLoader.GREEDY_FEEDS_BEFORE_LOSS` in code). Counter resets after the penalty.

Per-breed defaults live in `[traits.json](../src/main/resources/traits.json)`. See [§2 Default traits](#default-traits-traitsjson).

### Other traits (quick reference)


| Trait     | Effect                                                                                                                                                                                          |
| --------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `loud`    | Dog-voiced `bark`/`barks` → growl (UPPERCASE); narrator `bark` unchanged                                                                                                                        |
| `lazy`    | `wait` sleeps twice as long                                                                                                                                                                     |
| `wet`     | `whine` / `whimper` doubled                                                                                                                                                                     |
| `fetchy`  | Same print twice → *tail THUMP THUMP, same stick, still the best stick* on stdout, then the line again                                                                                         |
| `chaser`  | Each loop tick adds **+1 toy** to that dog's **items** (same field as `squirrels` in stories)                                                                                                   |
| `playful` | Dog speaks → output reversed before formatting; narrator `bark` unchanged. Defaults: bearded collie, border collie, flat-coated retriever, Nova Scotia duck toller, papillon, shetland sheepdog |


**Mood:** **Dog speaks** (`she bark`, `labrador woof`): 0 toys → lowercase even though the verb is `bark`; `loud` → uppercase. **Narrator** (`bark "…"`, `I bark "…"`): as written.

Trait assignment and story examples: `[AUTHOR.md` § Traits](AUTHOR.md#traits-personality). Parser grammar: [§10 Traits](#10-traits).

### Pronouns

Full grammar: [§6 Pronouns and pet names](#6-pronouns-and-pet-names).

- `**she` / `her`** → last dog described with feminine voice (`She is 2.`)
- `**he` / `his` / `him`** → last dog described with masculine voice
- Introducing another dog (`I have a beagle.`) does **not** rebind `she` from the first

---

## 4. Basics


| Line                                 | Parses as        | Need to know           |
| ------------------------------------ | ---------------- | ---------------------- |
| *(empty line)*                       | skipped          | Blank lines are fine   |
| `bark "hello"`                       | `Print[BARK, …]` | One statement per line |
| Story-only prose with no heard words | ignored          | Use `--strict` to warn |


**Heard** means: registered names, keywords (`when`, `bark`, `holds`, …), numbers, strings, booleans (`yes`, `nope`, …), null words (`empty`, `nothing`, …).

**Ignored** means everything else (`I`, `have`, `a`, `the`, `some`, …).

**Assign glue** (between subject and value): `is`, `has`, `have`, `was`, `were`, `becomes`, `became`.

---

## 5. Registering dogs and objects

### Register a breed


| Line                         | Parses as             | Need to know                                           |
| ---------------------------- | --------------------- | ------------------------------------------------------ |
| `labrador`                   | `Assign[labrador, 0]` | Breed must be in `breeds.txt`. Age 0, 0 toys, 0 treats |
| `I have a labrador.`         | same                  | Story glue around the breed is ignored                 |
| `So, my wife has a labrador` | same                  | Line with exactly one breed word registers it          |


### Register object counts (global, not on a dog)


| Line      | Parses as         | Need to know                                         |
| --------- | ----------------- | ---------------------------------------------------- |
| `ball 5`  | `Assign[ball, 5]` | Object must be in `objects.txt`                      |
| `5 balls` | `Assign[ball, 5]` | Number first; plural aliases work (`balls` → `ball`) |


Object aliases include `balls` → `ball`, `cookies` → `cookie`, `biscuits` → `biscuit`, `snacks` → `snack`.

Print a global object count: `bark ball` → `Variable(ball)`.

---

## 6. Pronouns and pet names


| Word               | Resolves to                                         |
| ------------------ | --------------------------------------------------- |
| `she`, `her`       | Last dog introduced with feminine voice             |
| `he`, `him`, `his` | Last dog introduced with masculine voice            |
| `it`               | Object pronoun (print mood only; not a dog subject) |


Introducing another breed (`I have a beagle.`) does **not** rebind `she` from the first dog.

**Pet names:** After `Her name is "Bimba"`, the capitalized word `Bimba` works as a dog subject in later lines. You can also assign a name without quotes if the word is capitalized: `labrador nickname Bimba` (same as a string).

**Multi-word breeds:** `the golden retriever`, `bark how many toys the golden retriever has`.

---

## 7. Dog fields (complete reference)

Each dog has nine stored fields. **Toys and treats are separate counters** (`items` vs `food`).


| Field         | Type                | Story words (aliases)                                                                                                                                                                                                                     |
| ------------- | ------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `food`        | number              | `treat`, `treats`, `snack`, `snacks`, `cookie`, `cookies`, `biscuit`, `biscuits`, `food`                                                                                                                                                  |
| `items`       | number              | `toy`, `toys`, `item`, `items`, `puppy`, `puppies`, `bed`, `beds`, `walk`, `walks`, `nap`, `naps`, `trick`, `tricks`, `friend`, `friends`, `squirrel`, `squirrels`, `zoomies`, `baths`                                                    |
| `age`         | number              | `age`                                                                                                                                                                                                                                     |
| `name`        | string              | `name`, `nickname`                                                                                                                                                                                                                        |
| `description` | string              | `description`, `collar`, `tag`, `owner`, `family`                                                                                                                                                                                         |
| `color`       | string              | `color`, `species`, `gender`                                                                                                                                                                                                              |
| `inventory`   | string (comma list) | `inventory`, `belongings`                                                                                                                                                                                                                 |
| `happy`       | boolean             | `goodboy`, `goodgirl`, `gooddog`, `happy`, `trained`, `sleepy`, `tired`, `excited`, `muddy`, `wet`, `dirty`, `clean`, `sick`, `healthy`, `rescued`, `adopted`, `loyal`, `stubborn`, `clever`, `silly`, `calm`, `noisy`, `quiet`, `lonely` |
| `fed`         | boolean             | `fed`, `groomed`, `washed`, `vetted`                                                                                                                                                                                                      |


`breed` is **not** a stored field.

Reading an object name as a field on a dog counts how many of that object are in the dog's **inventory** (e.g. `how many balls she has` if `ball` is a registered object).

---

## 8. Setting dog attributes

### Set a numeric field


| Line                     | Parses as                          | Need to know                    |
| ------------------------ | ---------------------------------- | ------------------------------- |
| `my labrador has 3 toys` | `SetAttribute[labrador, items, 3]` | `has` / `have` glue optional    |
| `She has 2 treats.`      | `SetAttribute[she, food, 2]`       | Pronouns resolve                |
| `She is 2.`              | `SetAttribute[she, age, 2]`        | Bare `is` + number sets **age** |
| `He is 5 years old`      | `SetAttribute[he, age, 5]`         | `years old` suffix              |
| `labrador 5`             | `SetAttribute[labrador, age, 5]`   | Breed then number sets age      |


### Set text fields


| Line                            | Parses as                                 | Need to know                 |
| ------------------------------- | ----------------------------------------- | ---------------------------- |
| `Her name is "Bimba"`           | `SetAttribute[her, name, "Bimba"]`        | Enables `Bimba` as subject   |
| `labrador nickname "Pepon"`     | `SetAttribute[labrador, name, "Pepon"]`   |                              |
| `her owner is "Alex"`           | `SetAttribute[her, description, "Alex"]`  | `owner` → description        |
| `my labrador color is "golden"` | `SetAttribute[labrador, color, "golden"]` | Any string field + `is` glue |


### Set boolean fields


| Line                       | Parses as                             | Need to know                        |
| -------------------------- | ------------------------------------- | ----------------------------------- |
| `my labrador happy is yes` | `SetAttribute[labrador, happy, true]` | `true`, `yes`, `yeah`, `yep` → true |
| `she fed is nope`          | `SetAttribute[she, fed, false]`       | `false`, `no`, `nope` → false       |


### Set from an expression


| Line                                                         | Parses as                                          | Need to know                                                                                      |
| ------------------------------------------------------------ | -------------------------------------------------- | ------------------------------------------------------------------------------------------------- |
| `my labrador treats is count of the items in the cookie jar` | `SetAttribute[labrador, food, StashAccess(COUNT)]` | Right side is any value expression. Can be made to sound more human by adding extra filler words. |
| `my labrador belongings are "ball, stick"`                   | `SetAttribute[…, inventory, "ball, stick"]`        | Replaces inventory list. Glue: `is`, `are`, `has`, `was`, `were`, …                               |


---

## 9. Adjusting fields (increment / decrement)

Default amount is **1** when no number is given (`she finds a toy` → +1).

Food verbs bump **treats** when no topic word is given. Item verbs bump **toys**.

### Food increment verbs (+ treats)

`feed`, `feeds`, `refills`, `steals`, `pinch`, `pinches`

### Food decrement verbs (− treats)

`gulp`, `gulps`, `eat`, `eats`, `ate`, `devour`, `devours`, `chews`, `munches`, `scarfs`, `wolfs`, `nibbles`

### Item increment verbs (+ toys)

`grow`, `grows`, `grew`, `gets`, `receives`, `earns`, `gains`, `collects`, `snags`, `nabs`, `inherits`, `scores`, `find`, `finds`, `fetch`, `fetches`, `discovers`, `unearths`

### Item decrement verbs (− toys)

`buried`, `lost`, `misplaces`, `breaks`, `destroys`, `hides`, `buries`, `forgets`, `tosses`, `abandons`

### Examples


| Line                         | Parses as               | Need to know        |
| ---------------------------- | ----------------------- | ------------------- |
| `she misplaces a toy`        | `SetAttribute` −1 items |                     |
| `she finds a toy`            | +1 items                |                     |
| `my labrador feeds 2 treats` | +2 food                 | Explicit topic      |
| `my labrador gulps a treat`  | +1 food                 |                     |
| `Bimba pinches a treat`      | +1 food                 | Pet name as subject |


Greedy breeds lose treats after feeding past a threshold (see [§3 Runtime semantics](#3-runtime-semantics)).

---

## 10. Traits

Trait words: `loud`, `greedy`, `lazy`, `wet`, `fetchy`, `chaser`.


| Line                   | Parses as                      | Need to know                   |
| ---------------------- | ------------------------------ | ------------------------------ |
| `my corgi is loud`     | `SetTrait[corgi, loud, true]`  |                                |
| `my corgi is not loud` | `SetTrait[corgi, loud, false]` | `not` / `never` before trait   |
| `husky is loud nope`   | `SetTrait[husky, loud, false]` | Boolean after trait also works |
| `husky is loud empty`  | `SetTrait[husky, loud, false]` | Null-clear words disable       |


Default traits per breed live in `traits.json`. Breeds without an entry start with no traits.

### Runtime trait effects


| Trait     | Effect                                                                                                                                                                           |
| --------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `loud`    | Dog-voiced `bark`/`barks`/`woof` print in ALL CAPS; narrator `bark` unchanged                                                                                                    |
| `greedy`  | After 3 feeds/gulps/shares in a row, lose 1 treat (food only; counter resets)                                                                                                    |
| `lazy`    | `wait` sleeps twice as long                                                                                                                                                      |
| `wet`     | `whine` / `whimper` output doubled                                                                                                                                               |
| `fetchy`  | Same print twice → *tail THUMP THUMP,  same stick, still the best stick* on stdout, then the line again                                                                          |
| `chaser`  | Each loop tick adds **+1 toy** to that dog's **items** (same field as `squirrels` in stories)                                                                                    |
| `playful` | Dog speaks → output reversed before formatting; narrator `bark` unchanged. Defaults: bearded collie, border collie, flat-coated retriever, cockapoo, papillon, shetland sheepdog |


**Mood:** **Dog speaks** (`she barks`, `labrador woofs`): 0 toys → lowercase even though the verb is `bark`; `loud` → uppercase. **Narrator** (`bark "…"`, `I bark "…"`): as written.

---

## 11. Story variables

Two fixed globals. Same assign glue as dog fields (`is`, `are`, `has`, …).

### `memory` (whole number)


| Line                                             | Parses as                            | Need to know                        |
| ------------------------------------------------ | ------------------------------------ | ----------------------------------- |
| `memory is 4`                                    | `Assign[memory, 4]`                  | Must be whole number in short range |
| `memory is count of the items in the cookie jar` | `Assign[memory, StashAccess(COUNT)]` | Coerced to a number                 |
| `bark memory`                                    | `Print` + `Variable(memory)`         |                                     |


### `journal` (text)


| Line                     | Parses as                      | Need to know                  |
| ------------------------ | ------------------------------ | ----------------------------- |
| `journal is "walk time"` | `Assign[journal, "walk time"]` | Strings and field reads       |
| `journal is her name`    | `Assign[journal, Field(…)]`    | Any value that prints as text |
| `bark journal`           | `Print` + `Variable(journal)`  | Starts empty                  |


Lines where `memory` or `journal` is the assignee parse as story-variable assignment. No other user-defined globals exist (except trick params, `for each` variable).

---

## 12. Printing

### Print verbs and output styles


| Verbs                                                                                                                                                          | Style   | Output effect                              |
| -------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------- | ------------------------------------------ |
| `bark`, `barks`, `say`, `says`, `mention`, `mentions`, `tell`, `tells`, `announce`, `announces`, `declare`, `declares`, `command`, `commands`, `pant`, `pants` | BARK    | As written                                 |
| `growl`, `growls`, `shout`, `shouts`                                                                                                                           | GROWL   | UPPERCASE                                  |
| `woof`, `woofs`                                                                                                                                                | WOOF    | UPPERCASE                                  |
| `howl`, `howls`                                                                                                                                                | HOWL    | First letter ×4 + rest (`"hi"` → `hhhhhi`) |
| `whimper`, `whimpers`, `whisper`, `whispers`                                                                                                                   | WHIMPER | lowercase                                  |
| `whine`, `whines`                                                                                                                                              | WHINE   | lowercase                                  |
| `yap`, `yaps`, `yapping`, `yapps`, `squeak`, `squeaks`                                                                                                         | YAPPING | Text repeated twice with space             |
| `whining`, `mutter`, `mutters`                                                                                                                                 | WHINING | Letters spaced (`"hi"` → `h i`)            |


Bare verb with no value prints a default for that style (`bark` → `woof`, `growl` → `growl`, `howl` → `howl`, …).

### Simple values


| Line                                                          | Parses as                             | Need to know                           |
| ------------------------------------------------------------- | ------------------------------------- | -------------------------------------- |
| `bark "hello"`                                                | `Print[BARK, StringLiteral("hello")]` |                                        |
| `bark`                                                        | `Print` + empty value                 | Default `woof`                         |
| `bark 2 plus 3`                                               | `Print` + `Binary(PLUS, 2, 3)`        | Math after verb                        |
| `bark "a", memory, 5`                                         | `Print` with 3 values                 | Comma-separated                        |
| `they growl memory minus how many treats she has, "missing!"` | `Print` + math + string               | Math first when `minus`/`plus` on line |


### Dog field reads (without `how many`)


| Line                                         | Parses as                         | Need to know                              |
| -------------------------------------------- | --------------------------------- | ----------------------------------------- |
| `bark my labrador name`                      | `Print` + `Field(labrador, name)` | Any attribute word after breed/pronoun    |
| `she woof her name`                          | `Print` + `Field(her, name)`      | Pronoun before verb sets dog speaker mood |
| `he growls his age`                          | `Print` + `Field(his, age)`       |                                           |
| `when she has 1 toy then he growls his name` | inline `IfChain` + `Print`        | Field read in branch                      |


### Dog field counts (`how many … has`)


| Line                                          | Parses as                                  | Need to know     |
| --------------------------------------------- | ------------------------------------------ | ---------------- |
| `bark how many toys she has`                  | `Print` + `Field(she, items)`              |                  |
| `bark how many treats Bimba has`              | `Print` + `Field(Bimba, food)`             |                  |
| `bark how many toys the golden retriever has` | `Print` + `Field(golden_retriever, items)` | Multi-word breed |


### Stash and pile in print


| Line                                          | Parses as                      | Need to know                 |
| --------------------------------------------- | ------------------------------ | ---------------------------- |
| `bark how many items are in the cookie jar`   | `StashAccess(COUNT)`           | Print only (not math)        |
| `bark how many items are in her cookie jar`   | same                           | `her` is glue                |
| `bark how many toys the toy box has`          | `StashAccess(toy_box, COUNT)`  | Topic + collection           |
| `bark her cookie jar`                         | `StashAccess(ALL)`             | Comma-separated items        |
| `bark cookie_jar`                             | same                           | Script name                  |
| `whimper the second item from the cookie jar` | `StashAccess(ELEMENT, second)` | Ordinals (see stash section) |
| `bark top of laundry_basket pile`             | `PileAccess(TOP)`              | Top item                     |
| `bark laundry_basket pile`                    | `PileAccess(ALL)`              | All items, bottom to top     |


### Other print forms


| Line                       | Parses as                            | Need to know                               |
| -------------------------- | ------------------------------------ | ------------------------------------------ |
| `bark my labrador is loud` | `Print` + `HasTrait(labrador, loud)` | Prints true/false; does **not** set trait  |
| `bark bark bark`           | triple paw print                     | Three same print verbs                     |
| `bark "woof" letters`      | `Length(...)`                        | String length; also `length`, `characters` |
| `bark add with 2, 3`       | `FunctionCall(add, 2, 3)`            | Trick call                                 |
| `bark ball`                | `Variable(ball)`                     | Global object count                        |


### When `when` is not a branch


| Line                                                | Parses as    | Need to know                             |
| --------------------------------------------------- | ------------ | ---------------------------------------- |
| `bark "The pack heads home when the jar is empty."` | `Print` only | `when` inside quotes or after print verb |


---

## 13. Value expressions (math and literals)

Used in print, assign right-hand sides, stash slot values, and trick returns.

### Numbers and math


| Fragment         | Parses as          | Synonyms                                                   |
| ---------------- | ------------------ | ---------------------------------------------------------- |
| `2 plus 3`       | `Binary(PLUS, …)`  | `plus`, `add`, or `+` token                                |
| `4 minus 1`      | `Binary(MINUS, …)` | `minus`, `spends`                                          |
| `2 times 3`      | `Binary(STAR, …)`  | `times`, `*`, `multiply`, `double`, `doubles`, `whelps`, … |
| `6 divided by 2` | `Binary(SLASH, …)` | `divided by`, `/`, `broke`, `divide`, `halves`             |
| `not false`      | `Not(false)`       | `not`, `never`                                             |
| `"a" plus "b"`   | string concat      | If both sides are numbers, adds; otherwise joins text      |


Division by zero throws a runtime error.

### Literals


| Fragment                                    | Parses as                                            |
| ------------------------------------------- | ---------------------------------------------------- |
| `true`, `yes`, `yeah`, `yep`                | `BooleanLiteral(true)`                               |
| `false`, `no`, `nope`                       | `BooleanLiteral(false)`                              |
| `nothing`, `empty`, `starving`, `hungry`, … | `NullLiteral` (see null-clear list in stash section) |
| `memory`                                    | `Variable(memory)`                                   |
| `ball`                                      | `Variable(ball)` (registered object)                 |


### Count phrases (use in math)


| Fragment                               | Parses as               | Need to know       |
| -------------------------------------- | ----------------------- | ------------------ |
| `how many toys she has`                | `Field(she, items)`     | Dog count          |
| `how many treats the labrador has`     | `Field(labrador, food)` |                    |
| `count of the items in the cookie jar` | `StashAccess(COUNT)`    | Jar or pile size   |
| `memory minus how many toys she has`   | `Binary(MINUS, …)`      | Example: 4 − 2 = 2 |


**Not in math:** `how many items are in the cookie jar` (print phrase only). In math use `count of the items in …`.

**Not valid:** `minus her treats` (use `minus how many treats she has`).

### Trick calls

`add with 2, 3` → `FunctionCall(add, [2, 3])`. Name must match a defined trick; `with` introduces arguments (comma-separated).

---

## 14. Conditions

Used after `when` / `if` / `while` / `until` / `otherwise when`.

### Exact count


| Fragment                   | Parses as                 |
| -------------------------- | ------------------------- |
| `she has 3 toys`           | `HasExact(she, items, 3)` |
| `she does not have 9 toys` | `Not(HasExact(…))`        |


### Comparisons


| Fragment                              | Parses as                  | Need to know                                                                                                                                             |
| ------------------------------------- | -------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `she has more than 2 toys`            | `Comparison(GREATER_THAN)` | `more`, `bigger`, `louder`, `over`, `above`, `exceeds` + `than`                                                                                          |
| `she sniffs less toys than 8`         | `Comparison(LESS_THAN)`    | **Must** use `sniffs` (or `smells`, `scents`, `checks`, `peeks`) + `less` (or `fewer`, `smaller`, `under`, `below`, `quieter`) + topic + `than` + number |
| `she does not sniff less toys than 8` | `Not(Comparison(…))`       | Negation works on comparisons                                                                                                                            |


**Not supported in conditions:** `growls louder than` on a field, `matches` / `equals` field comparisons, `either` / `or` between tests.

### Trait checks


| Fragment                    | Parses as                  |
| --------------------------- | -------------------------- |
| `my labrador is loud`       | `HasTrait(labrador, loud)` |
| `my labrador is not greedy` | `Not(HasTrait(…))`         |


### Combining tests


| Fragment                                       | Parses as         | Need to know                         |
| ---------------------------------------------- | ----------------- | ------------------------------------ |
| `she has 3 toys and she has more than 2 toys`  | `Logical(AND, …)` | Only `and` joins tests in conditions |
| `neither my labrador nor my beagle has 9 toys` | `Logical(NOR, …)` | Needs `nor` between two breed names  |


**Common mistake:** `while she toys sniffs less than 8` fails. Write `while she sniffs less toys than 8 then`.

---

## 15. Branches (`when` / `if`)

Branch starters: `if`, `when`, `whenever`, `should`. Must be heard at the start of a branch line (except `otherwise when`).

Then words: `then`, `do`. Else words: `otherwise`, `instead`, `else`.

Block end: `bury`, `end`, `goodnight`, `enough`, `bedtime`, `lightsout`, `hush`, `done`, `finished`.

### Inline then (one line, no body lines)


| Line                                                                                | Parses as               | Need to know                             |
| ----------------------------------------------------------------------------------- | ----------------------- | ---------------------------------------- |
| `when she has 1 toy then heel`                                                      | `IfChain` + `Break`     | No `bury` for single inline step         |
| `when she has 2 toys then she woof "hi"`                                            | `IfChain` + `Print`     |                                          |
| `when she has 3 toys then I bark "yes" otherwise I bark "no" bury`                  | `IfChain` + inline else |                                          |
| `when Bimba has 3 treats then the human growls "Nothing left." otherwise bark "no"` | inline if + else        | `bury` optional when all branches inline |


### Multiline then / else


| Line                                       | Parses as         | Need to know                     |
| ------------------------------------------ | ----------------- | -------------------------------- |
| `when she has 2 toys then` + body + `bury` | `IfChain`         | Body lines are normal statements |
| `otherwise when she has 1 toy then` …      | second `IfBranch` | Elif (`otherwise when`)          |
| `otherwise` + body + `bury`                | `elseSteps`       | Final else                       |


### Story glue before print in branches


| Line                                                            | Parses as                                              |
| --------------------------------------------------------------- | ------------------------------------------------------ |
| `when Bimba has 3 treats then the human growls "Nothing left."` | `IfChain` + `Print[GROWL]`; words before verb are glue |


---

## 16. Loops (`while` / `until`)

While starters: `while`, `during`. Until starters: `until`, `till`.


| Line                                            | Parses as   | Need to know                 |
| ----------------------------------------------- | ----------- | ---------------------------- |
| `while she has more than 0 toys then` … `bury`  | `WhileLoop` | Multiline body needs `bury`  |
| `until my labrador has 0 toys then` … `bury`    | `UntilLoop` | True condition → exit loop   |
| `while … when she has 1 toy then heel` … `bury` | nested      | `heel` breaks innermost loop |


Inline `then` (one `heel`, `again`, or print on same line) works without `bury` for that step only.

`chaser` trait: each loop iteration adds 1 toy before the body runs.

---

## 17. For each (stash items)

Starters: `for each … from …` or `for each … in …`. Each word: `each`, `every`.


| Line                                                  | Parses as                    | Need to know                        |
| ----------------------------------------------------- | ---------------------------- | ----------------------------------- |
| `for each snack from cookie_jar then` … `bury`        | `ForEach[snack, cookie_jar]` | Variable name is any identifier     |
| `for each treat from her cookie jar then` …           | same                         | Story jar phrase resolves           |
| `for each snack from cookie_jar then bark snack bury` | inline body                  | One print on same line after `then` |


Each stash item is bound to the variable for one iteration. Loop variable is the only other mutable name besides `memory` and `journal`.

---

## 18. Loop control


| Line    | Parses as    | Synonyms                              |
| ------- | ------------ | ------------------------------------- |
| `heel`  | `Break`      | `stop`, `stay`, `halt`                |
| `again` | `Continue`   | `repeat`, `resume`, `onward`          |
| `bury`  | closes block | See branch section for all bury words |


---

## 19. Stashes (lists)

Registry: `stashes.txt`. Story `cookie jar` → `cookie_jar`.

### Initialize (replace all)


| Line                                 | Parses as   | Need to know                                                                                      |
| ------------------------------------ | ----------- | ------------------------------------------------------------------------------------------------- |
| `her cookie jar holds "a", "b", "c"` | `StashInit` | `holds`, `hold`, `keeps`, `keep`, `stores`, `store`, `contains`, `hoards`, `guards`, `stockpiles` |
| `cookie_jar holds 1, 2, 3`           | `StashInit` | Numbers and strings allowed                                                                       |


### Append one item


| Line                          | Parses as     | Need to know                                                           |
| ----------------------------- | ------------- | ---------------------------------------------------------------------- |
| `cookie_jar stows "biscuit"`  | `StashAppend` | `stow`, `stows`, `snag`, `snags`, `get`, `gets`, `collect`, `collects` |
| `her cookie jar snag "treat"` | same          | Must not also have `from` (take phrase)                                |


### Set / remove / clear slot


| Line                                                        | Parses as               | Need to know                                                                              |
| ----------------------------------------------------------- | ----------------------- | ----------------------------------------------------------------------------------------- |
| `cookie_jar first is "y"`                                   | `StashSet[first, "y"]`  | Needs `is` glue; value can be expression                                                  |
| `cookie_jar first is 5`                                     | `StashSet[first, 5]`    | Numeric slot values                                                                       |
| `cookie_jar second is count of the items in the cookie jar` | `StashSet` + expression |                                                                                           |
| `her cookie jar drops first`                                | `StashRemove[first]`    | `drops`, `drop`, `loses`, `lose`, `misplaces`, `tosses`, `abandons`, `removes`, `forgets` |
| `her cookie jar is empty`                                   | `StashClear`            | Any null-clear word (below)                                                               |


### Stash slots (ordinals)

Slot words: `first`, `second`, `third`, … word ordinals up to `nineteenth`, `twentieth`, `twenty-first`, compounds with `hundred`/`thousand`, numeric `1`/`2`/`3`, and `last`.

Used in: `drops`, `is` set, take-from-stash, print `second item from the cookie jar`.

### Null-clear words (empty stash or disable trait)

`null`, `nothing`, `notreat`, `emptybowl`, `no-bone-here`, `nobodyhome`, `hungry`, `starving`, `empty`, `barebowl`, `emptykennel`, `allgone`, `vanished`, `missing`, `nowhere`, `treatless`, `boneless`, `squeakless`, `nofetch`

---

## 20. Piles (stacks)

Registry: `piles.txt`.


| Line                                             | Parses as   | Need to know                                                                                                          |
| ------------------------------------------------ | ----------- | --------------------------------------------------------------------------------------------------------------------- |
| `laundry_basket holds "a", "b"`                  | `PileInit`  | Replaces pile                                                                                                         |
| `the laundry basket is empty`                    | `PileClear` | Null-clear words                                                                                                      |
| `she puts "c" into the laundry basket`           | `PilePush`  | `put`, `puts`, `toss`, `tosses`, `drop`, `drops`, `throw`, `throws`, `plop`, `plops`, `store`, `stores` + `in`/`into` |
| `she takes the top item from the laundry basket` | `PilePop`   | `take`, `takes`, `grab`, `grabs`, `snatch`, `snatches`, `pull`, `pulls` + `top`/`peak`/`summit` + `from`              |


Print: `bark top of laundry_basket pile`, `bark how many items are in the laundry basket` (count), `bark laundry_basket pile` (all items).

---

## 21. Take from stash

Take words: `wants`, `want`, `takes`, `take`, `gets`, `get` (with `from` + stash).


| Line                                                              | Parses as                | Need to know                  |
| ----------------------------------------------------------------- | ------------------------ | ----------------------------- |
| `she wants the first biscuit from her cookie jar`                 | `TakeFromStash`          | Item → +1 treat for that dog  |
| `she takes the first biscuit from her cookie jar`                 | same                     | Without whimper               |
| `she whimpers as she wants the first biscuit from her cookie jar` | `LineGroup[Print, Take]` | Print mood + take on one line |


Slot/item before `from`: ordinal (`first`, `second`, `last`, …), quoted string, or number.

---

## 22. Share and pass

### Pass (exactly one toy to one friend)

Pass words: `pass`, `passes`, `passed`, `toss`, `tosses`, `tossed` + `to`.


| Line                             | Parses as             | Need to know |
| -------------------------------- | --------------------- | ------------ |
| `she passes a toy to the beagle` | `Pass[giver, beagle]` | Moves 1 toy  |


### Share toys

Share words: `share`, `shares`, `shared`, `split`, `splits` + `with`.


| Line                                            | Parses as    | Need to know                              |
| ----------------------------------------------- | ------------ | ----------------------------------------- |
| `the labrador shares with the beagle and husky` | `Share` toys | Integer split; remainder stays with giver |
| `Bimba shares with the dachshund`               | `Share`      | Pet names work                            |
| `the labrador shares with the golden retriever` | `Share`      | Multi-word breeds                         |


Recipients: comma-separated (`beagle, husky`) or `and`-joined (`beagle and husky`).

### Share stash contents


| Line                                                        | Parses as     | Need to know                                          |
| ----------------------------------------------------------- | ------------- | ----------------------------------------------------- |
| `the labrador shares her cookie jar with the beagle, husky` | `Share` stash | Items become treats, split round-robin; stash cleared |


---

## 23. Tricks (functions)

Define: `expects` / `expect` … `returns` / `return` … `bury` (or `end`, etc.).


| Line                                                       | Parses as                | Need to know          |
| ---------------------------------------------------------- | ------------------------ | --------------------- |
| `add expects a b returns a plus b bury`                    | `FunctionDef` one-liner  |                       |
| `add expects a b returns a plus b` + body + `bury`         | `FunctionDef` + steps    | Multiline body        |
| `greet expects name returns nothing` + `bark name` + `end` | returns null             | `nothing` return type |
| `bark add with 2, 3`                                       | `Print` + `FunctionCall` | Call with `with`      |


Parameters are single words between `expects` and `returns`. Return expression can be any value expression.

---

## 24. Input, wait, exit

### Listen (stdin)

Listen words: `listen`, `listens`, `listening`, `sniff`, `sniffs`, `perk`, `perks`, `hear`, `hears`, `eavesdrop`, `eavesdrops`, `ask`, `asks`, `prompt`, `prompts`, `await`, `awaits`.


| Line                   | Parses as          | Need to know                                      |
| ---------------------- | ------------------ | ------------------------------------------------- |
| `the labrador listens` | `Listen[labrador]` | Reads one stdin line into that dog's name storage |


### Wait

Wait words: `wait`, `waits`, `waiting`, `sleep`, `sleeps`, `sleeping`, `nap`, `naps`, `napping`, `doze`, `dozes`, `dozing`, `snooze`, `snoozes`, `snoozing`, `settle`, `settles`, `settling`, `rest`, `rests`, `resting`, `lounge`, `lounges`, `lounging`, `pause`, `pauses`, `pausing`, `linger`, `lingers`, `lingering`, `loiter`, `loiters`, `loitering`, `dream`, `dreams`, `dreaming`.


| Line             | Parses as | Need to know                      |
| ---------------- | --------- | --------------------------------- |
| `wait 2 seconds` | `Wait[2]` | Number optional; default 1 second |
| `sleep`          | `Wait[1]` | `lazy` trait doubles wait time    |


Negative wait time throws an error.

### Exit

Exit words: `escape`, `leave`, `depart`, `scram`, `wander`.


| Line     | Parses as |
| -------- | --------- |
| `escape` | `Exit`    |


---

## 25. Multiple statements on one line

When one line contains two heard actions, the parser may build a `LineGroup`:


| Line                                                              | Parses as                         | Need to know   |
| ----------------------------------------------------------------- | --------------------------------- | -------------- |
| `she whimpers as she wants the first biscuit from her cookie jar` | `LineGroup[Print, TakeFromStash]` | Whimper + take |


Most lines should stay one statement; this is mainly for take + mood print.

---

## 26. Colons and glue


| Line                                        | Parses as   | Need to know               |
| ------------------------------------------- | ----------- | -------------------------- |
| `bark: "hello"`                             | `Print`     | Colon before value ignored |
| `her cookie jar holds some items: "a", "b"` | `StashInit` | `some items` is glue       |


---

## 27. Story vs script


| Story                                              | Script                                         | Notes                          |
| -------------------------------------------------- | ---------------------------------------------- | ------------------------------ |
| `her cookie jar`                                   | `cookie_jar`                                   | Spaces → underscores           |
| `the laundry basket`                               | `laundry_basket`                               | No `pile` filler in script     |
| `her cookie jar holds "a", "b"`                    | `cookie_jar holds "a", "b"`                    | No `stash` filler in script    |
| `her cookie jar is empty`                          | `cookie_jar empty`                             | Prefer `empty`, not `starving` |
| `she whimpers … first biscuit from her cookie jar` | `whimper first from cookie_jar`                | Ordinal + `from` + name        |
| `she woof how many toys she has`                   | `bark how many toys the labrador has`          | Name the dog in script         |
| `when she has 2 toys then she woof "hi"`           | `when labrador has 2 toys then bark "hi" bury` |                                |
| `for each treat from her cookie jar then`          | `for each treat from cookie_jar then`          |                                |


Prefer story phrasing in `.woof` files under `examples/woof/`. Underscore script lines are fine in tests and `examples/scratch/`.

**Script anti-patterns:** `cookie_jar stash holds …`, `cookie_jar stash starving`, `whimper cookie_jar first` (parses as print-all, not first slot). See `[AUTHOR.md` § Story mode](AUTHOR.md#story-mode-vs-script-mode).

---

## 28. Examples to run

```bash
./gradlew run --args="examples/woof/goodboy.woof"
./gradlew run --args="examples/woof/bimba.woof"
./gradlew run --args="examples/woof/tutorial.woof"
./gradlew run --args="examples/woof/counter-tradition.woof"
./gradlew run --args="examples/woof/rebuild-if-while.woof"
```

---

