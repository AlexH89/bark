from pbark.cli.dog_art import paws_art
from tests.conftest import run_joined


PAWS = "\n".join(line.strip() for line in paws_art().splitlines())


def test_years_old_sets_age_not_toys():
    assert run_joined(
        """
        dachshund
        He is 5 years old
        he woof how many toys he has
        he woof he age
        """
    ) == "0\n5"


def test_woofs_and_howls_work_like_singular():
    assert run_joined(
        """
        labrador
        he woofs "hi"
        he howls "hi"
        """
    ) == "HI\nhhhhhi"


def test_goodboy_hello_world_output(examples_dir):
    source = (examples_dir / "goodboy.woof").read_text(encoding="utf-8")
    assert run_joined(source) == (
        "My wife's dog Pepon:\n"
        "WWWWWHOOOO!\n"
        "PEPON"
    )


def test_bimba_story_output(examples_dir):
    source = (examples_dir / "bimba.woof").read_text(encoding="utf-8")
    expected = """Narrator: Bimba waits by her food bowl while the house still sleeps.
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
""" + PAWS
    assert run_joined(source) == expected


def test_tutorial_story_output(examples_dir):
    source = (examples_dir / "tutorial.woof").read_text(encoding="utf-8")
    expected = """The gate opens and Bimba trots into the sun.
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
""" + PAWS
    assert run_joined(source) == expected
