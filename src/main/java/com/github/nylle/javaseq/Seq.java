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

    static <T> Seq<T> of(Iterator<T> coll) {
        return coll.hasNext() ? cons(coll.next(), () -> of(coll)) : of();
    }

    static <T> Seq<T> cons(T first, Supplier<Seq<T>> f) {
        return new Cons<>(first, f);
    }

    static <T> Seq<T> iterate(T initial, Function<T, T> f) {
        return cons(initial, () -> iterate(f.apply(initial), f));
    }

    T first();

    Seq<T> rest();

    Seq<T> take(long n);

    Seq<T> filter(Predicate<? super T> pred);

    <R> Seq<R> map(Function<? super T, ? extends R> f);
}
