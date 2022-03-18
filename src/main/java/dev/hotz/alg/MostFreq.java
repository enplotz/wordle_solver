package dev.hotz.alg;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.Map;
import java.util.Optional;

import dev.hotz.Guesser;
import dev.hotz.Util;
import dev.hotz.Wordle;

/**
 * A guesser that chooses as next guess the most-frequent word remaining in the dictionary.
 */
public class MostFreq implements Guesser {

    private final ArrayList<Util.Pair<String, Long>> remaining;

    public MostFreq() {
        this.remaining = new ArrayList<>(Wordle.DICTIONARY.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .map(e -> new Util.Pair<>(e.getKey(), e.getValue()))
                .toList());
    }

    @Override
    public Optional<String> guess(final Deque<Guess> history) {
        if (!history.isEmpty()) {
            final var corr = Wordle.Correctness.initMask();
            final var last = history.getLast();
            this.remaining.removeIf(e -> !last.matches(e.left(), corr));
        }
        return this.remaining.stream().findFirst().map(Util.Pair::left);
    }
}
