package dev.hotz;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.stream.Collectors;

/**
 * Hello wordle!
 * A Java version following the roget implementation stream.
 *
 */
public final class Wordle {

    // max allowed guesses until failed
    private static final int MAX_GUESSES = 6;

    // 5 letters
    public static final int LENGTH = 5;

    // TODO better dictionary handling, ideally this could be done in the generate-sources phase?
    public static final Map<Word, Long> DICTIONARY = loadDict();

    private static Map<Word, Long> loadDict() {
        try (final var in = new BufferedReader(new InputStreamReader(
                Objects.requireNonNull(Wordle.class.getClassLoader().getResourceAsStream("dictionary.txt"))))) {
            return in.lines()
                    .filter(l -> !l.startsWith("#"))
                    .map(l -> l.split(" "))
                    .collect(Collectors.toMap(l -> new Word(l[0]), l -> Long.valueOf(l[1])));
        } catch (IOException e) {
            throw new RuntimeException("Could not load dictionary!");
        }
    }

    public OptionalInt play(final Word answer, final Guesser guesser) {
        Objects.requireNonNull(answer);
        Objects.requireNonNull(guesser);

        final var hist = new ArrayDeque<Guesser.Guess>();
        for (int i = 1; i <= MAX_GUESSES; i++) {
            final var guess = guesser.guess(hist).orElseThrow();
            if (answer.equals(guess)) {
                guesser.finish(i);
                return OptionalInt.of(i);
            }
            assert DICTIONARY.containsKey(guess) : "Guessed word '" + guess + "' is not in dictionary!";
            hist.addLast(new Guesser.Guess(guess, Correctness.compute(answer, guess)));
        }
        return OptionalInt.empty();
    }

    /**
     * A byte[]-wrapper class that works as expected in a HashMap and has very lightweight "charAt"-like comparison for the given String.
     */
    // TODO The object overhead is a bit sad, ideally we would like to use something like a type alias for byte[] (value types when...)
    //      that also handles HashMap comparing like e.g. for Strings (where it works), not like byte[] where Objects.equals(ba1, ba2) -> false...
    public static class Word {

        private final byte[] value;

        public Word(final String word) {
            this.value = Objects.requireNonNull(word).getBytes();
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof final Word word)) {
                return false;
            }
            return Arrays.equals(value, word.value);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(value);
        }

        public byte valueAt(final int i) {
            return this.value[i];
        }

        @Override
        public String toString() {
            return new String(this.value);
        }
    }

    public enum Correctness {
        // Green
        CORRECT,
        // Yellow
        MISPLACED,
        // Red
        WRONG;

        public static final Correctness[][] ALL_PATTERNS = genPatterns();

        static public Correctness[] compute(final Word answer, final Word guess) {
            return compute(answer, guess, null);
        }

        static public Correctness[] compute(final Word answer, final Word guess, Correctness[] correctness) {
            if (correctness == null) {
                correctness = new Correctness[LENGTH];
            } else if (correctness.length != LENGTH) {
                throw new IllegalArgumentException(String.format("Output parameter 'correctness' must be exactly %d long, was: %d", LENGTH, correctness.length));
            }
            Arrays.fill(correctness, WRONG);

            final var misplaced = new int['z' - 'a' + 1];
            // check correct chars
            for (int i = 0; i < correctness.length; i++) {
                final var a = answer.valueAt(i);
                final var g = guess.valueAt(i);
                if (a == g) {
                    correctness[i] = CORRECT;
                } else {
                    // provisionally count as misplaced
                    misplaced[a - 'a'] += 1;
                }
            }
            // verify misplaced characters
            for (int i = 0; i < correctness.length; i++) {
                final int g = guess.valueAt(i);
                if (WRONG == correctness[i] && misplaced[g - 'a'] > 0) {
                    correctness[i] = MISPLACED;
                    misplaced[g - 'a'] -= 1;
                }
            }

            return correctness;
        }

        public static int idx(final Correctness[] mask) {
            return Arrays.stream(mask).reduce(0, (acc, c) -> acc * 3 + switch (c) {
                case CORRECT -> 0;
                case MISPLACED -> 1;
                case WRONG -> 2;
            }, Integer::sum);
        }

        public static Correctness[] initMask() {
            final var c = new Correctness[LENGTH];
            Arrays.fill(c, Wordle.Correctness.WRONG);
            return c;
        }

        public static Correctness[] maskOf(final String shorthand) {
            Objects.requireNonNull(shorthand);
            final int len = shorthand.length();
            if (len != LENGTH) {
                throw new IllegalArgumentException(String.format("Mask must have length %d, has: %d%n", LENGTH, len));
            }
            return shorthand.chars().mapToObj(c -> switch (c) {
                case 'C' -> CORRECT;
                case 'M' -> MISPLACED;
                case 'W' -> WRONG;
                default -> throw new IllegalArgumentException("Invalid shorthand character: " + c);
            }).toArray(Correctness[]::new);
        }

        private static Correctness[][] genPatterns() {
            return Arrays.stream(Correctness.values())
                    .flatMap((Correctness a) -> Arrays.stream(Correctness.values())
                            .flatMap((Correctness b) -> Arrays.stream(Correctness.values())
                                    .flatMap((Correctness c) -> Arrays.stream(Correctness.values())
                                            .flatMap((Correctness d) -> Arrays.stream(Correctness.values())
                                                    .map((Correctness e) -> new Correctness[] { a, b, c, d, e } )))))
                    .toArray(Correctness[][]::new);
        }
    }
}
