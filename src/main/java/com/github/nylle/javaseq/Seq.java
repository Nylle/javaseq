package com.github.nylle.javaseq;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Seq<T> extends List<T> {

    static <T> Seq<T> of() {
        return Nil.of();
    }

    static <T> Seq<T> of(T x) {
        return conj(x, of());
    }

    static <T> Seq<T> cons(T first, Supplier<Seq<T>> f) {
        return new Cons<>(first, f);
    }

    static <T> Seq<T> conj(T first, Seq<T> rest) {
        return new Conj<>(first, rest);
    }

    static <T> Seq<T> iterate(T initial, Function<T, T> f) {
        return cons(initial, () -> iterate(f.apply(initial), f));
    }

    T first();

    Seq<T> rest();

    Seq<T> take(long n);

}
