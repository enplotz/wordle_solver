package dev.hotz;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
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
    private static final Word[] GAMES = loadGames();

    private static Word[] loadGames() {
        try (final var in = new BufferedReader(new InputStreamReader(
                Objects.requireNonNull(Wordle.class.getClassLoader().getResourceAsStream("answers.txt"))))) {
            return in.lines()
                    .filter(l -> !l.startsWith("#"))
                    .map(Word::new)
                    .toArray(Word[]::new);
        } catch (IOException e) {
            throw new RuntimeException("Could not load games!");
        }
    }

    @CommandLine.Parameters(index = "0", arity = "0..1", description = "Number of games to run. If not present, plays all games.")
    private int maxGames = Integer.MAX_VALUE;

    @Option(names = {"-a", "--algorithm"}, description = "naive, mostfreq, pnaive")
    private String algorithm = "naive";

    @Option(names = {"-p", "--progress"}, description = "Report avg. score during run")
    private boolean progress = false;

    @Override
    public Integer call() {
        final var w = new Wordle();
        int score = 0;
        int solved = 0;

        final List<AtomicInteger> histogram = new ArrayList<>();
        final int max = Math.min(maxGames, GAMES.length);
        for (int i = 0; i < max; i++) {
            final var r = w.play(GAMES[i], guesser(algorithm));
            if (r.isPresent()) {
                final int s = r.getAsInt();
                score += s;
                solved += 1;
                if (s >= histogram.size()) {
                    IntStream.range(0, s - histogram.size() + 1).mapToObj(_unused -> new AtomicInteger()).forEachOrdered(histogram::add);
                }
                histogram.get(s).incrementAndGet();
            }
            if (progress) {
                final int curr = i + 1;
                int percent = (int) (1.0 * curr * 100 / max);
                final String string = "\r"
                        + String.format(Locale.US, "avg. score: %.3f ", 1.0 * score / solved)
                        + " ".repeat(percent == 0 ? 2 : 2 - (int) (Math.log10(percent)))
                        + " %d%% [".formatted(percent) + "=".repeat((int) (percent * .42)) + '>' + " ".repeat(
                        (int) ((100 - percent) * .42)) + ']'
                        + " ".repeat((int) (((int) (Math.log10(max)) - (int) (Math.log10(curr))) * .42))
                        + " %d/%d".formatted(curr, max);
                System.err.print(string);
            }
        }
        System.err.println();

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
        System.err.printf(Locale.US, "avg score: %f%n", 1.0 * score / solved);
        System.err.printf(Locale.US, "solved: %.2f%%%n", 1.0 * solved / max * 100);

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
