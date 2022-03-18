package dev.hotz;

import java.util.Optional;
import java.util.OptionalInt;

import org.junit.jupiter.api.Test;

import dev.hotz.Wordle.Word;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestWordle {

    @Test
    void testWordleOne() {
        final var w = new Wordle();
        final Guesser g = history -> Optional.of(new Word("right"));
        assertEquals(OptionalInt.of(1), w.play(new Word("right"), g));
    }

    @Test
    void testWordleTwo() {
        final var w = new Wordle();
        final Guesser g = history -> Optional.of(history.size() == 1 ? new Word("right") : new Word("wrong"));
        assertEquals(OptionalInt.of(2), w.play(new Word("right"), g));
    }

    @Test
    void testWordleThree() {
        final var w = new Wordle();
        final Guesser g = history -> Optional.of(history.size() == 2 ? new Word("right") : new Word("wrong"));
        assertEquals(OptionalInt.of(3), w.play(new Word("right"), g));
    }

    @Test
    void testWordleFour() {
        final var w = new Wordle();
        final Guesser g = history -> Optional.of(history.size() == 3 ? new Word("right") : new Word("wrong"));
        assertEquals(OptionalInt.of(4), w.play(new Word("right"), g));
    }

    @Test
    void testWordleFive() {
        final var w = new Wordle();
        final Guesser g = history -> Optional.of(history.size() == 4 ? new Word("right") : new Word("wrong"));
        assertEquals(OptionalInt.of(5), w.play(new Word("right"), g));
    }

    @Test
    void testWordleSix() {
        final var w = new Wordle();
        final Guesser g = history -> Optional.of(history.size() == 5 ? new Word("right") : new Word("wrong"));
        assertEquals(OptionalInt.of(6), w.play(new Word("right"), g));
    }

}
