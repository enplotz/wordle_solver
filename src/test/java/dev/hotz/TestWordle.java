package dev.hotz;

import java.util.Optional;
import java.util.OptionalInt;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestWordle {

    @Test
    void testWordleOne() {
        final var w = new Wordle();
        final Guesser g = history -> Optional.of("right");
        Assertions.assertEquals(OptionalInt.of(1), w.play("right", g));
    }

    @Test
    void testWordleTwo() {
        final var w = new Wordle();
        final Guesser g = history -> Optional.of(history.size() == 1 ? "right" : "wrong");
        Assertions.assertEquals(OptionalInt.of(2), w.play("right", g));
    }

    @Test
    void testWordleThree() {
        final var w = new Wordle();
        final Guesser g = history -> Optional.of(history.size() == 2 ? "right" : "wrong");
        Assertions.assertEquals(OptionalInt.of(3), w.play("right", g));
    }

    @Test
    void testWordleFour() {
        final var w = new Wordle();
        final Guesser g = history -> Optional.of(history.size() == 3 ? "right" : "wrong");
        Assertions.assertEquals(OptionalInt.of(4), w.play("right", g));
    }

    @Test
    void testWordleFive() {
        final var w = new Wordle();
        final Guesser g = history -> Optional.of(history.size() == 4 ? "right" : "wrong");
        Assertions.assertEquals(OptionalInt.of(5), w.play("right", g));
    }

    @Test
    void testWordleSix() {
        final var w = new Wordle();
        final Guesser g = history -> Optional.of(history.size() == 5 ? "right" : "wrong");
        Assertions.assertEquals(OptionalInt.of(6), w.play("right", g));
    }

}
