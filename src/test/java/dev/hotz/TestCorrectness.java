package dev.hotz;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import dev.hotz.Wordle.Correctness;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestCorrectness {

    @Test
    void testMaskOfValids() {
        assertArrayEquals(
                new Correctness[] { Correctness.WRONG, Correctness.WRONG, Correctness.WRONG, Correctness.WRONG,
                        Correctness.WRONG, }, Correctness.maskOf("WWWWW"));
        assertArrayEquals(
                new Correctness[] { Correctness.CORRECT, Correctness.WRONG, Correctness.MISPLACED, Correctness.WRONG,
                        Correctness.WRONG, }, Correctness.maskOf("CWMWW"));
        assertArrayEquals(
                new Correctness[] { Correctness.CORRECT, Correctness.WRONG, Correctness.MISPLACED, Correctness.WRONG,
                        Correctness.CORRECT, }, Correctness.maskOf("CWMWC"));
    }


    @Test
    void testMaskOfInvalids() {
        // one char too many
        assertThrows(IllegalArgumentException.class, () -> Correctness.maskOf("WWWWWW"));
        // one char too few
        assertThrows(IllegalArgumentException.class, () -> Correctness.maskOf("WWWW"));
        // empty
        assertThrows(IllegalArgumentException.class, () -> Correctness.maskOf(""));
        // null
        assertThrows(NullPointerException.class, () -> Correctness.maskOf(null));
    }

    @Test
    void testCompute() {
        assertArrayEquals(Correctness.maskOf("CCCCC"), Correctness.compute("abcde", "abcde"));
        assertArrayEquals(Correctness.maskOf("MMMMM"), Correctness.compute("abcde", "eabcd"));
        assertArrayEquals(Correctness.maskOf("WWWWW"), Correctness.compute("abcde", "fghij"));

        assertArrayEquals(Correctness.maskOf("CMCMC"), Correctness.compute("salet", "selat"));
        assertArrayEquals(Correctness.maskOf("CMCMM"), Correctness.compute("aabbc", "abbca"));
        assertArrayEquals(Correctness.maskOf("WWWWW"), Correctness.compute("aaabb", "eeeff"));
        assertArrayEquals(Correctness.maskOf("MMMWW"), Correctness.compute("abccc", "cabef"));
        assertArrayEquals(Correctness.maskOf("CCCWW"), Correctness.compute("cabef", "cabcc"));
        assertArrayEquals(Correctness.maskOf("CCWMW"), Correctness.compute("cacef", "cabcc"));
    }

    @Test
    void testMatches() {
        matches("salet", "CCCCC", "salet");
        matches("which", "CCCCC", "which");
        matches("aaabb", "WWWWW", "eeeff");
        matches("which", "WWWWW", "omovs");
    }

    @Test
    void testNoMatches() {
        noMatch("salet", "CCCCC", "salad");
        noMatch("aaabb", "WWWWW", "eeefa");
    }

    private void matches(final String guess, final String mask, final String answer) {
        final var m = Correctness.maskOf(mask);
        assertTrue(new Guesser.Guess(guess, m).matches(answer));
        assertArrayEquals(m, Correctness.compute(answer, guess));
    }

    private void noMatch(final String guess, final String mask, final String answer) {
        final var m = Correctness.maskOf(mask);
        assertFalse(new Guesser.Guess(guess, m).matches(answer));
        assertFalse(Arrays.equals(m, Correctness.compute(answer, guess)));
    }

}
