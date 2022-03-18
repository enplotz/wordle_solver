package dev.hotz;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.Stream;

public final class Util {

    private Util() {
        // hidden
    }

    static <L, R, O> Stream<O> zip(final Stream<L> left, final Stream<R> right, final BiFunction<L, R, O> zip) {
        final var r = right.iterator();
        return left.map(x1 -> r.hasNext() ? zip.apply(x1, r.next()) : null).takeWhile(Objects::nonNull);
    }

    public record Pair<L,R>(L left, R right) {

    }
}
