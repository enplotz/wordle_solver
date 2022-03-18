package dev.hotz.alg;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

import dev.hotz.Guesser;
import dev.hotz.Util;
import dev.hotz.Wordle;
import dev.hotz.Wordle.Word;

/**
 * Naîve guesser for 3blue1brown algorithm.
 */
public class Naive implements Guesser {

    private final List<Util.Pair<Word, Long>> remaining;

    public Naive() {
        this.remaining = new ArrayList<>(Wordle.DICTIONARY.entrySet()
                .stream()
                .map(e -> new Util.Pair<>(e.getKey(), e.getValue()))
                .toList());
    }

    @Override
    public Optional<Word> guess(final Deque<Guess> history) {
        // avoid allocations in inner-most loop
        final var corr = Wordle.Correctness.initMask();
        if (!history.isEmpty()) {
            final var last = history.getLast();
            this.remaining.removeIf(e -> !last.matches(e.left(), corr));
        } else {
            return Optional.of(new Word("tares"));
        }

        final long remaining_count = this.remaining.stream().mapToLong(Util.Pair::right).sum();
        Optional<Candidate> best = Optional.empty();

        for (final var p : this.remaining) {
            final var word = p.left();
            double sum = 0;
            for (final Wordle.Correctness[] pattern : Wordle.Correctness.ALL_PATTERNS) {
                long in_pattern_total = 0;
                for (final var e : this.remaining) {
                    if (new Guess(word, pattern).matches(e.left(), corr)) {
                        in_pattern_total += e.right();
                    }
                }
                if (in_pattern_total == 0) {
                    continue;
                }
                double p_of_this_pattern = 1.0 * in_pattern_total / remaining_count;
                sum += p_of_this_pattern * (Math.log(p_of_this_pattern) / Math.log(2));
            }
            double goodness = -sum;
            best = best.filter(currBest -> goodness < currBest.goodness).or(() -> Optional.of(new Candidate(word, goodness)));
        }

        return best.map(Candidate::word);
    }

    private record Candidate(Word word, double goodness) {
    }
}
