package dev.hotz;

import java.util.Arrays;
import java.util.Deque;
import java.util.Optional;

/**
 * Guesser trying to solve wordle.
 * The guesser gets repeatedly tasked to guess the word, while being provided with the history,
 * like on the website.
 */
public interface Guesser {

    /**
     * Make a guess.
     * @param history similarity to answer of previous guess words
     * @return next guess
     */
    Optional<String> guess(Deque<Guess> history);

    default void finish(long guesses) {
        // empty default impl
    }

    /**
     * A guess (word) and an accompanying correctness mask wrt. the answer.
     */
    record Guess(String word, Wordle.Correctness[] mask) {

        /**
         * Check if the guess is a possible match to the correct answer {@code word}.
         * @param word assumed correct answer
         * @return {@code true}, if this guess is a possible match to the answer {@code word}, {@code false} otherwise
         */
        public boolean matches(final String word, final Wordle.Correctness[] out) {
            // TODO this can potentially be optimized with early-return since not necessarily the whole mask
            //      has to be computed
            // TODO check if providing output parameters leads to perf improvement
            return Arrays.equals(Wordle.Correctness.compute(word, this.word, out), this.mask);
        }

        public boolean matches(final String word) {
            return matches(word, null);
        }
    }

}
