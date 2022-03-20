package dev.hotz.alg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

import dev.hotz.Guesser;
import dev.hotz.Util;
import dev.hotz.Wordle;
import dev.hotz.Wordle.Correctness;
import dev.hotz.Wordle.Word;

/**
 * Naîve guesser for 3blue1brown algorithm.
 */
public class Entropy implements Guesser {

    private final List<Util.Pair<Word, Long>> remaining;

    private static final int NUM_PATTERNS = (int) Math.pow(Correctness.values().length, Wordle.LENGTH);

    public Entropy() {
        this.remaining = new ArrayList<>(Wordle.DICTIONARY.entrySet()
                .stream()
                .map(e -> new Util.Pair<>(e.getKey(), e.getValue()))
                .toList());
    }

    @Override
    public Optional<Word> guess(final Deque<Guess> history) {
        // avoid allocations in inner-most loop
        final var corr = Correctness.initMask();
        if (!history.isEmpty()) {
            final var last = history.getLast();
            this.remaining.removeIf(e -> !last.matches(e.left(), corr));
        } else {
            return Optional.of(new Word("tares"));
        }

        final long remaining_count = this.remaining.stream().mapToLong(Util.Pair::right).sum();
        Optional<Candidate> best = Optional.empty();

        for (final var p : this.remaining) {
            // hypothetical "old guess"
            final var word = p.left();
            // instead of iterating over each pattern and all next guesses
            // and checking for compatibility with the old guess (matches)
            // we observe that each combination of next guess and old guess
            // produces exactly one correctness pattern.
            // since we have 3^5 possible patterns, we can just index them.
            final long[] in_pattern_total = new long[NUM_PATTERNS];
            for (final var e : this.remaining) {
                in_pattern_total[Correctness.idx(Correctness.compute(e.left(), word))] += e.right();
            }

            final double sum = Arrays.stream(in_pattern_total)
                    .filter(c -> c > 0)
                    .mapToDouble(total -> {
                        final double p_of_this_pattern = 1.0 * total / remaining_count;
                        return p_of_this_pattern * (Math.log(p_of_this_pattern) / Math.log(2));
                    }).sum();

            final double p_word = 1.0 * p.right() / remaining_count;
            final double entropy = -sum;
            final double goodness = p_word * entropy;
            best = best.filter(currBest -> goodness < currBest.goodness).or(() -> Optional.of(new Candidate(word, goodness)));
        }

        return best.map(Candidate::word);
    }

    private record Candidate(Word word, double goodness) {
    }
}
