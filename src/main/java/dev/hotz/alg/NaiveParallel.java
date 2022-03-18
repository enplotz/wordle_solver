package dev.hotz.alg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

import dev.hotz.Guesser;
import dev.hotz.Util;
import dev.hotz.Wordle;

/**
 * Parallel version of naîve guesser for 3blue1brown algorithm.
 */
public class NaiveParallel implements Guesser {

    private final List<Util.Pair<String, Long>> remaining;

    public NaiveParallel() {
        this.remaining = new ArrayList<>(Wordle.DICTIONARY.entrySet()
                .stream()
                .map(e -> new Util.Pair<>(e.getKey(), e.getValue()))
                .toList());
    }

    @Override
    public Optional<String> guess(final Deque<Guess> history) {
        final var corr = Wordle.Correctness.initMask();
        if (!history.isEmpty()) {
            final var last = history.getLast();
            this.remaining.removeIf(e -> !last.matches(e.left(), corr));
        } else {
            return Optional.of("tares");
        }
        final long remaining_count = this.remaining.stream().mapToLong(Util.Pair::right).sum();
        return this.remaining.parallelStream().map(p -> {
            final var word = p.left();
            final double sum = Arrays.stream(Wordle.Correctness.ALL_PATTERNS).parallel().mapToDouble(pattern -> {
                long in_pattern_total = 0;
                // parallelizing this loop actually made things worse
                for (final var e : this.remaining) {
                    if (new Guess(word, pattern).matches(e.left(), corr)) {
                        in_pattern_total += e.right();
                    }
                }
                if (in_pattern_total == 0) {
                    return 0;
                }
                double p_of_this_pattern = 1.0 * in_pattern_total / remaining_count;
                return p_of_this_pattern * (Math.log(p_of_this_pattern) / Math.log(2));
            }).sum();
            double goodness = -sum;
            return new Candidate(word, goodness);
        }).reduce((l, r) ->
            l.goodness > r.goodness ? l : r
        ).map(Candidate::word);
    }

    private record Candidate(String word, double goodness) {
    }
}
