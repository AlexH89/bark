package dev.klomptech.jbark;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.klomptech.jbark.cli.DogArt;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

// Locks flagship example output so story demos do not silently drift
class BarkGoldenTest {

  private static final Path EXAMPLES = Path.of("examples/woof");

  private static final String PAWS =
      String.join("\n", DogArt.pawsArt().lines().map(String::trim).toList());

  @Test
  void yearsOldSetsAgeNotToys() throws Exception {
    assertEquals(
        "0\n5",
        BarkTestSupport.runJoined(
            """
            dachshund
            He is 5 years old
            he woof how many toys he has
            he woof he age
            """));
  }

  @Test
  void woofsAndHowlsWorkLikeSingular() throws Exception {
    assertEquals(
        "HI\nhhhhhi",
        BarkTestSupport.runJoined(
            """
            labrador
            he woofs "hi"
            he howls "hi"
            """));
  }

  @Test
  void goodboyHelloWorldOutput() throws Exception {
    String source = Files.readString(EXAMPLES.resolve("goodboy.woof"));
    assertEquals(
        """
            My wife's dog Pepon:
            WWWWWHOOOO!
            PEPON""",
        BarkTestSupport.runJoined(source));
  }

  @Test
  void bimbaStoryOutput() throws Exception {
    String source = Files.readString(EXAMPLES.resolve("bimba.woof"));
    assertEquals(
        """
            Narrator: Bimba waits by her food bowl while the house still sleeps.
            Narrator: Bimba's cookie jar sits on the counter, tempting her nose...
            Narrator: with the jar out of her reach, she decides to focus on her toys.
            i want more toys! i want more toys!
            Narrator: with the humans still asleep it is time to play, but first Bimba needs more toys!
            Narrator: Greedy dogs always take more than one toy, three in this case!
            Narrator: Bimba now has this many toys:
            5
            Narrator: The toy box now has this many toys:
            0
            Narrator: But after playing the cookies are now too temping, Bimba knocks the cookie jar onto the ground
            whimper whimper
            Narrator: Bimba is not alone, there is also the dachshund Pepon
            Narrator: He is a bit simple and enters the room, tail already wagging.
            Narrator: Bimba shared toys with Pepon (a miracle) and now looks at her remaining toys and says:
            TTTTThat was fine
            Narrator: She decided to inform Pepon of how many toys she had left by barking.
            3
            Narrator: Bimba also informed Pepon of how many toys he now has...
            2
            Narrator: a human is finally awake shouts how how many toys are left in the box:
            0
            Narrator: they made the mistake of putting the jar on the ground and looked away for a second
            Narrator: The human checks how many cookies are left and shouts:
            0
            Narrator: The human checks how many snacks Bimba stole:
            1 MISSING!
            Narrator: I wonder who took the other cookie... The naughty dogs run away with their little stomping feet
            """
            + PAWS,
        BarkTestSupport.runJoined(source));
  }

  @Test
  void tutorialStoryOutput() throws Exception {
    String source = Files.readString(EXAMPLES.resolve("tutorial.woof"));
    assertEquals(
        """
            The gate opens and Bimba trots into the sun.
            She checks her toys before the others arrive.
            2
            READY TO PLAY.
            Hazel shows up late, as always.
            Someone left treats in the jar by the bench.
            whimper
            BIMBA
            The pack heads home when the jar is empty.
            1
            1
            """
            + PAWS,
        BarkTestSupport.runJoined(source));
  }
}
