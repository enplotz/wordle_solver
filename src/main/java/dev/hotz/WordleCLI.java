package dev.hotz;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import dev.hotz.Wordle.Word;
import dev.hotz.alg.MostFreq;
import dev.hotz.alg.Naive;
import dev.hotz.alg.NaiveParallel;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "jordle", mixinStandardHelpOptions = true, version = "1.0",
        description = "Plays wordle!")
class WordleCLI implements Callable<Integer> {

    // TODO also improve this, generate-sources?
    private static final List<Word> GAMES;

    static {
        List<Word> d;
        try {
            d = loadGames();
        } catch (IOException e) {
            System.err.println("[ERROR] Could not load games!");
            d = null;
        }
        GAMES = d;
    }

    private static List<Word> loadGames() throws IOException {
        try (final var in = new BufferedReader(new InputStreamReader(
                Objects.requireNonNull(Wordle.class.getClassLoader().getResourceAsStream("answers.txt"))))) {
            return in.lines()
                    .filter(l -> !l.startsWith("#"))
                    .map(Word::new)
                    .collect(Collectors.toList());
        }
    }

    @CommandLine.Parameters(index = "0", arity = "0..1", description = "Number of games to run. If not present, plays all games.")
    private Optional<Integer> numGames;

    @Option(names = {"-a", "--algorithm"}, description = "naive, mostfreq, pnaive")
    private String algorithm = "naive";

    @Override
    public Integer call() {
        final int maxGames = numGames.orElse(Integer.MAX_VALUE);
        final var w = new Wordle();
        int score = 0;
        int played = 0;

        final List<AtomicInteger> histogram = new ArrayList<>();
        for (final var answer : GAMES) {
            final var r = w.play(answer, guesser(algorithm));
            if (r.isPresent()) {
                final int s = r.getAsInt();
                played += 1;
                score += s;
                if (s >= histogram.size()) {
                    IntStream.range(0, s - histogram.size() + 1).mapToObj(i -> new AtomicInteger()).forEachOrdered(histogram::add);
                }
                histogram.get(s).incrementAndGet();
                // System.err.printf("Guessed '%s' in %d%n", answer, s);
                // } else {
                //    System.err.printf("Failed to guess '%s'%n", answer);
            }
            if (++played > maxGames) {
                break;
            }
        }

        final var sum = histogram.stream().mapToInt(AtomicInteger::intValue).sum();
        Util.zip(IntStream.range(0, Integer.MAX_VALUE).boxed(), histogram.stream().map(AtomicInteger::get), Util.Pair::new)
                .skip(1)
                .forEachOrdered(p -> {
                    final var s = p.left();
                    final var c = p.right();
                    final var frac = 1.0 * c / sum;
                    final long hash = Math.round(30 * frac);
                    final long white = Math.round(30 * (1.0 - frac));
                    System.err.printf("%d: %s%s (%d)%n", s,
                            LongStream.range(0, hash).mapToObj(v -> "#").collect(Collectors.joining()),
                            LongStream.range(0, white).mapToObj(v -> " ").collect(Collectors.joining()), c);
        });

        System.err.printf(Locale.US, "avg score: %f%n", 1.0 * score / played);

        return 0;
    }

    public static void main(String... args) {
        int exitCode = new CommandLine(new WordleCLI()).execute(args);
        System.exit(exitCode);
    }

    private static Guesser guesser(final String name) {
        return switch (name) {
            case "naive" -> new Naive();
            case "pnaive" -> new NaiveParallel();
            case "mostfreq" -> new MostFreq();
            default -> throw new IllegalArgumentException("Unknown guesser: " + name);
        };
    }
}
