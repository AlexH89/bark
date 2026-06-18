package dev.klomptech.jbark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.klomptech.jbark.cli.DogArt;
import dev.klomptech.jbark.errors.BarkError;
import dev.klomptech.jbark.parser.Keywords;
import org.junit.jupiter.api.Test;

class BarkFeatureTest {
  @Test
  void printBarkAndBareBark() throws Exception {
    assertEquals("Hello", BarkTestSupport.runJoined("bark \"Hello\""));
    assertEquals("woof", BarkTestSupport.runJoined("bark"));
  }

  @Test
  void naturalHowManyPhrasing() throws Exception {
    assertEquals(
        "3",
        BarkTestSupport.runJoined(
            """
            labrador
            my labrador has 3 toys
            she woof how many toys she has
            """));
  }

  @Test
  void bareNumberSetsAge() throws Exception {
    assertEquals(
        "5\n0",
        BarkTestSupport.runJoined(
            """
            labrador
            labrador 5
            he woof he age
            he woof how many toys he has
            """));
  }

  @Test
  void singleLineOtherwise() throws Exception {
    assertEquals(
        "yes",
        BarkTestSupport.runJoined(
            """
            my labrador has 3 toys
            when my labrador has 3 toys then I bark "yes" otherwise I bark "no" bury
            """));
    assertEquals(
        "no",
        BarkTestSupport.runJoined(
            """
            my labrador has 2 toys
            when my labrador has 3 toys then I bark "yes" otherwise I bark "no" bury
            """));
  }

  @Test
  void passToyShortcut() throws Exception {
    assertEquals(
        "1\n1",
        BarkTestSupport.runJoined(
            """
            labrador
            beagle
            my labrador has 2 toys
            the labrador passes a toy to the beagle
            bark how many toys my labrador has
            bark how many toys the beagle has
            """));
  }

  @Test
  void barePrintUsesStyleDefault() throws Exception {
    assertEquals("woof", BarkTestSupport.runJoined("bark"));
    assertEquals("woof", BarkTestSupport.runJoined("say"));
    assertEquals("WOOF", BarkTestSupport.runJoined("woof"));
    assertEquals("GROWL", BarkTestSupport.runJoined("growl"));
    assertEquals("hhhhhowl", BarkTestSupport.runJoined("howl"));
    assertEquals("whimper", BarkTestSupport.runJoined("whimper"));
    assertEquals("whine", BarkTestSupport.runJoined("whine"));
    assertEquals("yap yap", BarkTestSupport.runJoined("yapping"));
    assertEquals("w h i n e", BarkTestSupport.runJoined("whining"));
  }

  @Test
  void tripleBarkPaws() throws Exception {
    assertTrue(BarkTestSupport.runJoined("bark bark bark").contains(DogArt.PAW));
  }

  @Test
  void printVoices() throws Exception {
    assertEquals("HI", BarkTestSupport.runJoined("woof \"hi\""));
    assertEquals("HI", BarkTestSupport.runJoined("growl \"hi\""));
    assertEquals("hhhhhi", BarkTestSupport.runJoined("howl \"hi\""));
    assertEquals("hi", BarkTestSupport.runJoined("whine \"HI\""));
    assertEquals("hi hi", BarkTestSupport.runJoined("yapping \"hi\""));
    assertEquals("h i", BarkTestSupport.runJoined("whining \"hi\""));
  }

  @Test
  void assignObjectAndBreed() throws Exception {
    assertEquals(
        "5",
        BarkTestSupport.runJoined(
            """
            ball 5
            I bark ball
            """));
    assertEquals(
        "0",
        BarkTestSupport.runJoined(
            """
            corgi
            bark how many toys the corgi has
            """));
  }

  @Test
  void dogAttributesAndAdjustments() throws Exception {
    assertEquals(
        "3\nBimba",
        BarkTestSupport.runJoined(
            """
            my labrador has 3 toys
            labrador name "Bimba"
            bark how many toys my labrador has
            I bark my labrador name
            """));

    assertEquals(
        "3\n" + BarkTestSupport.FETCHY_REPEAT + "\n3",
        BarkTestSupport.runJoined(
            """
            my labrador has 3 toys
            my labrador feeds 2 treats
            bark how many toys my labrador has
            my labrador gulps a treat
            bark how many toys my labrador has
            """));

    assertEquals(
        "2\n1",
        BarkTestSupport.runJoined(
            """
            my labrador has 3 toys
            my labrador feeds 2 treats
            bark how many treats my labrador has
            my labrador gulps a treat
            bark how many treats my labrador has
            """));

    assertEquals(
        "4",
        BarkTestSupport.runJoined(
            """
            my labrador has 3 toys
            my labrador grows 1 toy
            bark how many toys my labrador has
            """));

    assertEquals(
        "2",
        BarkTestSupport.runJoined(
            """
            my labrador has 3 toys
            my labrador misplaces a toy
            bark how many toys my labrador has
            """));
  }

  @Test
  void whenOtherwise() throws Exception {
    assertEquals(
        "YES",
        BarkTestSupport.runJoined(
            """
            my labrador has 3 toys
            when my labrador has 3 toys then
                I woof "yes"
            otherwise
                I woof "no"
            bury
            """));
    assertEquals(
        "no",
        BarkTestSupport.runJoined(
            """
            my labrador has 2 toys
            when my labrador has 3 toys then
                I bark "yes"
            otherwise
                I bark "no"
            bury
            """));
  }

  @Test
  void whileAndUntil() throws Exception {
    assertEquals(
        "3\n2\n1",
        BarkTestSupport.runJoined(
            """
            my labrador has 3 toys
            while she has more than 0 toys then
                bark how many toys she has
                she misplaces a toy
            bury
            """));

    assertEquals(
        "1",
        BarkTestSupport.runJoined(
            """
            my labrador has 1 toy
            until my labrador has 0 toys then
                bark how many toys my labrador has
                my labrador misplaces a toy
            bury
            """));
  }

  @Test
  void forEachStash() throws Exception {
    assertEquals(
        "a\nb",
        BarkTestSupport.runJoined(
            """
            cookie_jar holds "a", "b"
            for each snack from cookie_jar then
                I whimper snack
            bury
            """));
  }

  @Test
  void stashOperations() throws Exception {
    assertEquals(
        "3\ny\n2",
        BarkTestSupport.runJoined(
            """
            cookie_jar holds 1, 2, 3
            bark how many items are in the cookie jar
            cookie_jar second is "y"
            I whimper the second item from the cookie jar
            cookie_jar drops first
            bark how many items are in the cookie jar
            """));
  }

  @Test
  void pileOperations() throws Exception {
    assertEquals(
        "c",
        BarkTestSupport.runJoined(
            """
            laundry_basket holds "a", "b"
            she puts "c" into the laundry basket
            I bark top of laundry_basket pile
            """));
  }

  @Test
  void functions() throws Exception {
    assertEquals(
        "5",
        BarkTestSupport.runJoined(
            """
            add expects a b returns a plus b bury
            bark add with 2, 3
            """));
  }

  @Test
  void mathAndLogic() throws Exception {
    assertEquals("5", BarkTestSupport.runJoined("I bark 2 plus 3"));
    assertEquals("true", BarkTestSupport.runJoined("I bark not false"));
    assertEquals(
        "Three and plenty.",
        BarkTestSupport.runJoined(
            "my labrador has 3 toys\n"
                + "when my labrador has 3 toys and she has more than 2 toys "
                + "then I bark \"Three and plenty.\" bury"));
  }

  @Test
  void stringOps() throws Exception {
    assertEquals("4", BarkTestSupport.runJoined("I bark \"woof\" letters"));
    assertEquals(
        "a, b",
        BarkTestSupport.runJoined(
            """
            cookie_jar holds "a", "b"
            bark her cookie jar
            """));
  }

  @Test
  void sharesToysAndStash() throws Exception {
    assertEquals(
        "2\n2\n2",
        BarkTestSupport.runJoined(
            """
            labrador
            beagle
            husky
            my labrador has 6 toys
            the labrador shares with the beagle and husky
            bark how many toys my labrador has
            bark how many toys the beagle has
            bark how many toys the husky has
            """));

    assertEquals(
        "1\n1\n1",
        BarkTestSupport.runJoined(
            """
            labrador
            beagle
            husky
            her cookie jar holds "a", "b", "c"
            the labrador shares her cookie jar with the beagle, husky
            bark how many treats my labrador has
            bark how many treats the beagle has
            bark how many treats the husky has
            """));
  }

  @Test
  void traitPlayfulReversesDogSpeech() throws Exception {
    assertEquals(
        "olleh",
        BarkTestSupport.runJoined(
            """
            border_collie
            border_collie barks "hello"
            """));
    assertEquals(
        "hello",
        BarkTestSupport.runJoined(
            """
            border_collie
            I bark "hello"
            """));
  }

  @Test
  void traitsLoudAndGreedy() throws Exception {
    assertEquals(
        "HELLO",
        BarkTestSupport.runJoined(
            """
            husky
            husky barks "hello"
            """));
    assertEquals(
        "still yapping still yapping",
        BarkTestSupport.runJoined(
            """
            labrador
            I yap "still yapping"
            """));

    assertEquals(
        "5\n2",
        BarkTestSupport.runJoined(
            """
            beagle
            my beagle has 5 toys
            my beagle feeds 1 treat
            my beagle feeds 1 treat
            my beagle feeds 1 treat
            bark how many toys the beagle has
            bark how many treats the beagle has
            """));
  }

  @Test
  void setTraitRuntime() throws Exception {
    assertEquals(
        "LOUD",
        BarkTestSupport.runJoined(
            """
            corgi
            my corgi is loud
            corgi barks "loud"
            """));
  }

  @Test
  void storyIntroAndPronouns() throws Exception {
    assertEquals(
        "2\n" + BarkTestSupport.FETCHY_REPEAT + "\n2\nBIMBA",
        BarkTestSupport.runJoined(
            """
            I have a labrador.
            She is 2.
            She has 2 toys.
            Her name is "Bimba".
            she woof how many toys she has
            she woof her age
            she woof her name
            """));

    assertEquals(
        "1\n1",
        BarkTestSupport.runJoined(
            """
            I have a labrador.
            She has 2 toys.
            I have a beagle.
            she passes a toy to the beagle
            she woof how many toys she has
            she woof how many toys the beagle has
            """));
  }

  @Test
  void pronouns() throws Exception {
    assertEquals(
        "2",
        BarkTestSupport.runJoined(
            """
            my labrador has 3 toys
            she misplaces a toy
            bark how many toys my labrador has
            """));
  }

  @Test
  void controlSignals() throws Exception {
    assertEquals(
        "3\n2",
        BarkTestSupport.runJoined(
            """
            labrador
            my labrador has 3 toys
            while she has more than 0 toys then
                bark how many toys she has
                she misplaces a toy
                when she has 1 toy then heel
            bury
            """));
  }

  @Test
  void shareRemainderStaysWithGiver() throws Exception {
    assertEquals(
        "3\n2\n2",
        BarkTestSupport.runJoined(
            """
            labrador
            beagle
            husky
            my labrador has 7 toys
            the labrador shares with the beagle and husky
            bark how many toys my labrador has
            bark how many toys the beagle has
            bark how many toys the husky has
            """));
  }

  @Test
  void storyTakeFromStashWithWhimper() throws Exception {
    assertEquals(
        "whimper whimper\n3\n1",
        BarkTestSupport.runJoined(
            """
            labrador
            her cookie jar holds "cookie", "cookie", "cookie", "cookie"
            she whimpers as she wants and takes the first biscuit from her cookie jar
            bark how many items are in her cookie jar
            she woof how many treats she has
            """));
  }

  @Test
  void storyTakeFromStashWithoutWhimper() throws Exception {
    assertEquals(
        "3\n1",
        BarkTestSupport.runJoined(
            """
            labrador
            her cookie jar holds "cookie", "cookie", "cookie", "cookie"
            she wants and takes the first biscuit from her cookie jar
            bark how many items are in her cookie jar
            she woof how many treats she has
            """));
  }

  @Test
  void storyTakeFromStashWantsOnly() throws Exception {
    assertEquals(
        "whimper whimper\n1\n" + BarkTestSupport.FETCHY_REPEAT + "\n1",
        BarkTestSupport.runJoined(
            """
            labrador
            her cookie jar holds "biscuit", "stick"
            she whimpers as she wants the first biscuit from her cookie jar
            bark how many items are in her cookie jar
            she woof how many treats she has
            """));
  }

  @Test
  void storyStashClear() throws Exception {
    assertEquals(
        "0",
        BarkTestSupport.runJoined(
            """
            her cookie jar holds "a", "b"
            her cookie jar is empty
            bark how many items are in her cookie jar
            """));
  }

  @Test
  void colonStoryGlue() throws Exception {
    assertEquals(
        "hello\n3",
        BarkTestSupport.runJoined(
            """
            bark: "hello"
            her cookie jar holds some items: "a", "b", "c"
            bark how many items are in her cookie jar
            """));
  }

  @Test
  void petNameResolvesToBreed() throws Exception {
    assertEquals(
        "2\n2",
        BarkTestSupport.runJoined(
            """
            I have a labrador.
            She has 4 toys.
            Her name is "Bimba".
            I have a dachshund.
            my dachshund has 2 toys
            Bimba shares with the dachshund
            bark how many toys my labrador has
            bark how many toys the dachshund has
            """));

    assertEquals(
        "1",
        BarkTestSupport.runJoined(
            """
            labrador
            Her name is "Bimba"
            Bimba pinches a treat
            she woof how many treats she has
            """));

    assertEquals(
        "no",
        BarkTestSupport.runJoined(
            """
            labrador
            Her name is "Bimba"
            Bimba pinches a treat
            Bimba pinches a treat
            when Bimba has 1 treats then bark "yes" otherwise bark "no"
            """));
    assertEquals(
        "yes",
        BarkTestSupport.runJoined(
            """
            labrador
            Her name is "Bimba"
            Bimba pinches a treat
            when Bimba has 1 treats then bark "yes" otherwise bark "no"
            """));
  }

  @Test
  void shareWithMultiWordBreed() throws Exception {
    assertEquals(
        "3\n" + BarkTestSupport.FETCHY_REPEAT + "\n3",
        BarkTestSupport.runJoined(
            """
            labrador
            golden retriever
            my labrador has 6 toys
            the labrador shares with the golden retriever
            bark how many toys my labrador has
            bark how many toys the golden retriever has
            """));
  }

  @Test
  void greedyLosesFoodAfterThreshold() throws Exception {
    assertEquals(
        "5\n2",
        BarkTestSupport.runJoined(
            """
            beagle
            my beagle is greedy
            my beagle has 5 toys
            my beagle feeds 1 treat
            my beagle feeds 1 treat
            my beagle feeds 1 treat
            bark how many toys the beagle has
            bark how many treats the beagle has
            """));
  }

  @Test
  void whenInsidePrintIsNotABranch() throws Exception {
    assertEquals(
        "The pack heads home when the jar is empty.",
        BarkTestSupport.runJoined(
            """
                bark "The pack heads home when the jar is empty."
                """));
  }

  @Test
  void counterTraditionDemo() throws Exception {
    assertEquals(
        "The count is:\n8",
        BarkTestSupport.runJoined(
            """
            I have a corgi.
            while she sniffs less toys than 8 then
                she snags a toy
            bury
            bark "The count is:"
            she woof how many toys she has
            """));
  }

  @Test
  void sheFindsToyIncrements() throws Exception {
    assertEquals(
        "1",
        BarkTestSupport.runJoined(
            """
            beagle
            she finds a toy
            she woof how many toys she has
            """));
  }

  @Test
  void sheSnagsToyIncrements() throws Exception {
    assertEquals(
        "1",
        BarkTestSupport.runJoined(
            """
            beagle
            she snags a toy
            she woof how many toys she has
            """));
  }

  @Test
  void invalidWhileConditionFails() {
    BarkError error =
        assertThrows(
            BarkError.class,
            () ->
                BarkTestSupport.runLines(
                    """
            corgi
            while my corgi toys growls less than 8 then
            bury
            """));
    assertTrue(error.getMessage().contains("no clue"));
  }

  @Test
  void storyAliasesMapToCanonicalFields() throws Exception {
    assertEquals(
        "1\n2\nPepon",
        BarkTestSupport.runJoined(
            """
            labrador
            my labrador has 1 toy
            my labrador feeds 2 treats
            bark how many toys my labrador has
            bark how many treats my labrador has
            labrador nickname "Pepon"
            I bark my labrador name
            """));

    assertEquals(
        "Bimba\nAlex",
        BarkTestSupport.runJoined(
            """
            labrador
            Her name is "Bimba"
            her owner is "Alex"
            I bark my labrador name
            I bark my labrador owner
            """));
  }

  @Test
  void breedIsNotAStoredField() {
    assertFalse(Keywords.isAttributeKeyword("breed"));
  }

  @Test
  void storyConstantFromStashCount() throws Exception {
    assertEquals(
        "4",
        BarkTestSupport.runJoined(
            """
            her cookie jar holds "cookie", "cookie", "cookie", "cookie"
            memory is count of the items in the cookie jar
            bark memory
            """));
  }

  @Test
  void storyJournal() throws Exception {
    assertEquals(
        "walk time",
        BarkTestSupport.runJoined(
            """
            journal is "walk time"
            bark journal
            """));
  }

  @Test
  void storyConstant() throws Exception {
    assertEquals(
        "2",
        BarkTestSupport.runJoined(
            """
            labrador
            memory is 4
            my labrador has 2 toys
            bark memory minus how many toys the labrador has
            """));
  }

  @Test
  void storyConstantWithPronounField() throws Exception {
    assertEquals(
        "2",
        BarkTestSupport.runJoined(
            """
            labrador
            memory is 4
            my labrador has 2 toys
            bark memory minus how many toys she has
            """));
  }

  @Test
  void humanStoryGlueBeforePrint() throws Exception {
    assertEquals(
        "NOTHING LEFT.",
        BarkTestSupport.runJoined(
            """
            labrador
            Her name is "Bimba"
            She has 3 treats
            when Bimba has 3 treats then the human growls "Nothing left." otherwise bark "no"
            """));
  }

  @Test
  void errorsAreDogThemed() {
    BarkError error =
        assertThrows(
            BarkError.class,
            () ->
                BarkTestSupport.runLines(
                    """
            bark memory minus how many toys phantom has
            """));
    assertTrue(error.getMessage().contains("No scent"));
  }
}
