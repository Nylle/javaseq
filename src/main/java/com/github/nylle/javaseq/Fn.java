package com.github.nylle.javaseq;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public class Fn {
    private Fn() {
    }

//    public static <T> ISeq<T> rest(ISeq<T> coll) {
//        if (coll != null) {
//            return coll.rest();
//        }
//        return Nil.empty();
//    }
//
//    public static <T> boolean isEmpty(ISeq<T> coll) {
//        if (coll != null) {
//            return coll.isEmpty();
//        }
//        return true;
//    }

    public static <T> Set<T> conj(Set<T> seen, T x) {
        var result = new HashSet<>(seen);
        result.add(x);
        return result;
    }

    public static <T> ISeq<T> filter(Predicate<? super T> pred, ISeq<T> coll) {
        return new LazySeq2<>(() -> {
            if (!coll.isEmpty()) {
                return pred.test(coll.first())
                        ? ISeq.cons(coll.first(), filter(pred, coll.rest()))
                        : filter(pred, coll.rest());
            }
            return null;
        });
    }

    public static <R, T> ISeq<R> map(Function<? super T, ? extends R> f, ISeq<T> coll) {
        return new LazySeq2<>(() -> {
            if (!coll.isEmpty()) {
                return ISeq.cons(f.apply(coll.first()), map(f, coll.rest()));
            }
            return null;
        });
    }

    public static <R, S, T> ISeq<R> map(BiFunction<? super T, ? super S, ? extends R> f, ISeq<? extends T> coll1, ISeq<? extends S> coll2) {
        return new LazySeq2<>(() -> {
            if (!coll1.isEmpty() && !coll2.isEmpty()) {
                return ISeq.cons(f.apply(coll1.first(), coll2.first()), map(f, coll1.rest(), coll2.rest()));
            }
            return null;
        });
    }

    public static <R, T> ISeq<R> mapcat(Function<? super T, ? extends Iterable<? extends R>> f, ISeq<? extends T> coll) {
        return new LazySeq2<>(() -> {
            if (!coll.isEmpty()) {
                return concat(f.apply(coll.first()).iterator(), mapcat(f, coll.rest()));
            }
            return null;
        });
    }

    public static <S, T, R> ISeq<R> mapcat(BiFunction<? super T, ? super S, Iterable<? extends R>> f, ISeq<? extends T> coll1, ISeq<? extends S> coll2) {
        return new LazySeq2<>(() -> {
            if (!coll1.isEmpty() && !coll2.isEmpty()) {
                return concat(f.apply(coll1.first(), coll2.first()).iterator(), mapcat(f, coll1.rest(), coll2.rest()));
            }
            return null;
        });
    }

    private static <T> ISeq<T> concat(Iterator<? extends T> iterator, ISeq<T> seq) {
        return new LazySeq2<>(() -> {
            if (!iterator.hasNext()) {
                return seq;
            }
            return ISeq.cons(iterator.next(), concat(iterator, seq));
        });
    }

    public static <T> ISeq<T> distinct(ISeq<T> coll) {
        return step(coll, new HashSet<>());
    }

    private static <T> ISeq<T> step(ISeq<T> seq, Set<T> seen) {
        return new LazySeq2<>(() -> {
            var result = filter(x -> !seen.contains(x), seq);
            if (result.isEmpty()) {
                return ISeq.of();
            }
            var first = result.first();
            return ISeq.cons(first, step(result.rest(), conj(seen, first)));
        });
    }
}
