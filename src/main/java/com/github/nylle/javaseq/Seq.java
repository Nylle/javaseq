package com.github.nylle.javaseq;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface Seq<T> extends List<T> {

    static <T> Seq<T> of() {
        return Nil.of();
    }

    @SafeVarargs
    static <T> Seq<T> of(T... xs) {
        return of(Arrays.asList(xs).iterator());
    }

    static <T> Seq<T> of(Iterable<T> coll) {
        return of(coll.iterator());
    }

    static <T> Seq<T> of(Iterator<T> coll) {
        return coll.hasNext() ? cons(coll.next(), () -> of(coll)) : of();
    }

    static <T> Seq<T> cons(T first, Supplier<Seq<T>> f) {
        return new Cons<>(first, f);
    }

    static <T> Seq<T> iterate(T initial, Function<T, T> f) {
        return cons(initial, () -> iterate(f.apply(initial), f));
    }

    static <T> Seq<T> concat(Iterable<T> coll, Supplier<Seq<T>> f) {
        return concat(coll.iterator(), f);
    }

    static <T> Seq<T> concat(Iterator<T> coll, Supplier<Seq<T>> f) {
        if (coll.hasNext()) {
            T next = coll.next();
            return coll.hasNext() ? cons(next, () -> concat(coll, f)) : cons(next, f);
        }
        return f.get();
    }

    T first();

    Seq<T> rest();

    Seq<T> take(long n);

    Seq<T> drop(long n);

    Seq<T> filter(Predicate<? super T> pred);

    <R> Seq<R> map(Function<? super T, ? extends R> f);

    <R> Seq<R> mapcat(Function<? super T, ? extends Iterable<? extends R>> f);

    Seq<T> takeWhile(Predicate<? super T> pred);

    Seq<T> dropWhile(Predicate<? super T> pred);
}
