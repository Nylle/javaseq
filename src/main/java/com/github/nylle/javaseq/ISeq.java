package com.github.nylle.javaseq;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public interface ISeq<T> extends List<T> {

    @SuppressWarnings("unchecked")
    static <T> ISeq<T> of() {
        return Nil.empty();
    }

    @SafeVarargs
    static <T> ISeq<T> of(T... xs) {
        var result = ISeq.<T>of();
        for(var i = xs.length-1; i >= 0; i--) {
            result = ISeq.cons(xs[i], result);
        }
        return result;
    }

    static <T> ISeq<T> cons(T x, ISeq<T> seq) {
        return new Cons<>(x, seq);
    }

    static <T> ISeq<T> lazySeq(T x, Supplier<ISeq<T>> f) {
        return new LazySeq<>(x, f);
    }

    static <T> ISeq<T> sequence(T[] coll) {
        return coll == null ? ISeq.of() : ISeq.sequence(Arrays.asList(coll).iterator());
    }

    static <T> ISeq<T> sequence(Iterable<T> coll) {
        if (coll == null) {
            return ISeq.of();
        }
        return coll instanceof ISeq<T> seq ? seq : ISeq.sequence(coll.iterator());
    }

    static <T> ISeq<T> sequence(Iterator<T> coll) {
        return coll == null || !coll.hasNext() ? ISeq.of() : lazySeq(coll.next(), () -> ISeq.sequence(coll));
    }

    static <T> ISeq<T> sequence(Stream<T> coll) {
        return coll == null ? of() : ISeq.sequence(coll.iterator());
    }

    static <K, V> ISeq<Map.Entry<K, V>> sequence(Map<K, V> coll) {
        return coll == null ? ISeq.of() : ISeq.sequence(coll.entrySet().iterator());
    }

    static ISeq<Character> sequence(CharSequence coll) {
        if(coll == null || coll.isEmpty()) {
            return ISeq.of();
        }
        return new StringSeq(coll, 0);
    }

    static <T> ISeq<T> iterate(T x, Function<T, T> f) {
        return ISeq.lazySeq(x, () -> iterate(f.apply(x), f));
    }

    @SafeVarargs
    static <T> ISeq<T> concat(Iterable<T>... colls) {
        return ISeq.sequence(colls).mapcat(x -> x);
    }

    static ISeq<Integer> range() {
        return ISeq.iterate(0, x -> x + 1);
    }

    static ISeq<Integer> range(int end) {
        return ISeq.range(0, end);
    }

    static ISeq<Integer> range(int start, int end) {
        return ISeq.range(start, end, 1);
    }

    static ISeq<Integer> range(int start, int end, int step) {
        return ISeq.iterate(start, x -> x + step).takeWhile(x -> step >= 0 ? (x < end) : (x > end));
    }

    T first();

    ISeq<T> rest();

    boolean isRealized();

    ISeq<T> take(long n);

    ISeq<T> drop(long n);

    ISeq<T> filter(Predicate<? super T> pred);

    <R> ISeq<R> map(Function<? super T, ? extends R> f);

    <S, R> ISeq<R> map(Iterable<? extends S> coll, BiFunction<? super T, ? super S, ? extends R> f);

    <R> ISeq<R> mapcat(Function<? super T, ? extends Iterable<? extends R>> f);

    <S, R> ISeq<R> mapcat(Iterable<? extends S> coll, BiFunction<? super T, ? super S, Iterable<? extends R>> f);

    ISeq<T> takeWhile(Predicate<? super T> pred);

    ISeq<T> dropWhile(Predicate<? super T> pred);

    ISeq<List<T>> partition(int n);

    ISeq<List<T>> partition(int n, int step);

    ISeq<List<T>> partition(int n, int step, Iterable<T> pad);

    ISeq<List<T>> partitionAll(int n);

    ISeq<List<T>> partitionAll(int n, int step);

    ISeq<T> reductions(BinaryOperator<T> f);

    <U> ISeq<U> reductions(U init, BiFunction<U, ? super T, U> f);

    ISeq<T> cons(T x);

    Optional<T> reduce(BinaryOperator<T> f);

    <U> U reduce(U val, BiFunction<U, ? super T, U> f);

    void run(Consumer<? super T> proc);

    ISeq<T> distinct();

    ISeq<T> sorted();

    ISeq<T> sorted(Comparator<? super T> comp);

    boolean some(Predicate<? super T> pred);

    boolean every(Predicate<? super T> pred);

    boolean notAny(Predicate<? super T> pred);

    Optional<T> max(Comparator<? super T> comp);

    Optional<T> min(Comparator<? super T> comp);

    <C extends Comparable<? super C>> Optional<T> maxKey(Function<T, C> f);

    <C extends Comparable<? super C>> Optional<T> minKey(Function<T, C> f);

    T nth(int index);

    T nth(int index, T notFound);

    Optional<T> find(int i);

    Optional<T> findFirst();

    Optional<T> findFirst(Predicate<? super T> pred);

    ISeq<T> realize();

    <K, V> Map<K, V> toMap(Function<T, K> k, Function<T, V> v);

    <K, V> Map<K, V> toMap(Function<T, K> k, Function<T, V> v, BinaryOperator<V> m);

    <K, V> Map<K, V> toMap();

    List<T> toList();
}
