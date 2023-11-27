package com.github.nylle.javaseq;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
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

    <S, R> Seq<R> map(Seq<? extends S> other, BiFunction<? super T, ? super S, ? extends R> f);

    <R> Seq<R> mapcat(Function<? super T, ? extends Iterable<? extends R>> f);

    Seq<T> takeWhile(Predicate<? super T> pred);

    Seq<T> dropWhile(Predicate<? super T> pred);

    Seq<List<T>> partition(int n);

    Seq<List<T>> partition(int n, int step);

    Seq<List<T>> partition(int n, int step, Iterable<T> pad);

    Seq<List<T>> partitionAll(int n);

    Seq<List<T>> partitionAll(int n, int step);

    Seq<T> reductions(BinaryOperator<T> f);

    Seq<T> reductions(T init, BinaryOperator<T> f);

    Optional<T> reduce(BinaryOperator<T> f);

    <R> R reduce(R val, BiFunction<R, ? super T, R> f);

    Seq<T> distinct();

    @SuppressWarnings("unchecked")
    Seq<T> sorted();

    Seq<T> sorted(Comparator<? super T> comp);

    boolean some(Predicate<? super T> pred);

    boolean every(Predicate<? super T> pred);

    boolean notAny(Predicate<? super T> pred);

    Optional<T> max(Comparator<? super T> comparator);

    Optional<T> min(Comparator<? super T> comparator);

    <C extends Comparable<? super C>> Optional<T> maxKey(Function<T, C> f);

    <C extends Comparable<? super C>> Optional<T> minKey(Function<T, C> f);

    Optional<T> find(int i);

    Optional<T> findFirst();

    Optional<T> findFirst(Predicate<? super T> pred);

    List<T> toList();
}
