package com.github.nylle.javaseq;

import java.util.Iterator;
import java.util.function.BiFunction;
import java.util.function.Function;

public class Fn {
    private Fn() {
    }

    public static <R, T> ISeq<R> mapcat(Function<? super T, ? extends Iterable<? extends R>> f, ISeq<T> coll) {
        return new LazySeq<>(() -> {
            if (!coll.isEmpty()) {
                return concat(f.apply(coll.first()).iterator(), mapcat(f, coll.rest()));
            }
            return ISeq.of();
        });
    }

    public static <R, S, T> ISeq<R> mapcat(BiFunction<? super T, ? super S, Iterable<? extends R>> f, ISeq<T> coll1, ISeq<? extends S> coll2) {
        return new LazySeq<>(() -> {
            if (!coll1.isEmpty() && !coll2.isEmpty()) {
                return concat(f.apply(coll1.first(), coll2.first()).iterator(), mapcat(f, coll1.rest(), coll2.rest()));
            }
            return ISeq.of();
        });
    }

    private static <T> ISeq<T> concat(Iterator<? extends T> iterator, ISeq<T> seq) {
        return new LazySeq<>(() -> {
            if (iterator.hasNext()) {
                return ISeq.cons(iterator.next(), concat(iterator, seq));
            }
            return seq;
        });
    }
}
