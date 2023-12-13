package com.github.nylle.javaseq;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public class Fn {

    private Fn() {
    }

    private static final int CHUNK_SIZE = 32;
    static <T> ISeq<T> chunkIteratorSeq(Iterator<T> iterator) {
        if(iterator.hasNext()) {
            return new LazySeq<>(() -> {
                T[] arr = (T[]) new Object[CHUNK_SIZE];
                int n = 0;
                while(iterator.hasNext() && n < CHUNK_SIZE) {
                    arr[n++] = iterator.next();
                }
                return new ChunkedCons<>(new ArrayChunk<>(arr, 0, n), chunkIteratorSeq(iterator));
            });
        }
        return Nil.empty();
    }

    // util

    public static <T> Set<T> conj(Set<T> coll, T x) {
        var result = new HashSet<>(coll);
        result.add(x);
        return result;
    }

    // creation

    public static <T> ISeq<T> nil() {
        return Nil.empty();
    }

    public static <T> ISeq<T> cons(T x, Iterable<T> coll) {
        return new Cons<>(x, seq(coll));
    }

    public static <T> ISeq<T> lazySeq(Supplier<ISeq<T>> f) {
        return new LazySeq<>(f);
    }

    public static <T> ISeq<T> iterate(T x, UnaryOperator<T> f) {
        return lazySeq(() -> cons(x, iterate(f.apply(x), f)));
    }

    public static <T> ISeq<T> seq(T[] coll) {
        if (coll != null) {
            return seq(Arrays.asList(coll).iterator());
        }
        return nil();
    }

    public static <T> ISeq<T> seq(Iterable<T> coll) {
        if (coll != null) {
            return coll instanceof ISeq<T> seq ? seq : seq(coll.iterator());
        }
        return nil();
    }

    public static <T> ISeq<T> seq(Stream<T> coll) {
        if (coll != null) {
            return seq(coll.iterator());
        }
        return nil();
    }

    public static <K, V> ISeq<Map.Entry<K, V>> seq(Map<K, V> coll) {
        if (coll != null) {
            return seq(coll.entrySet().iterator());
        }
        return nil();
    }

    public static ISeq<Character> seq(CharSequence coll) {
        if (coll != null && !coll.isEmpty()) {
            return new StringSeq(coll, 0);
        }
        return nil();
    }

    public static <T> ISeq<T> seq(Iterator<T> coll) {
        if (coll != null && coll.hasNext()) {
            return lazySeq(() -> cons(coll.next(), seq(coll)));
        }
        return nil();
    }

    @SafeVarargs
    public static <T> ISeq<T> concat(Iterable<T>... colls) {
        return seq(colls).mapcat(x -> x);
    }

    public static <T> ISeq<T> concat(Iterator<? extends T> iterator, ISeq<T> seq) {
        return Fn.lazySeq(() -> {
            if (iterator.hasNext()) {
                return Fn.cons(iterator.next(), concat(iterator, seq));
            }
            return seq;
        });
    }

    // forwarding functions

    public static <T> ISeq<T> filter(Predicate<? super T> pred, ISeq<T> seq) {
        return seq.filter(pred);
    }

    public static <R, T> ISeq<R> map(Function<? super T, ? extends R> f, ISeq<T> seq) {
        return seq.map(f);
    }

    public static <R, S, T> ISeq<R> map(BiFunction<? super T, ? super S, ? extends R> f, ISeq<T> seq1, ISeq<? extends S> seq2) {
        return seq1.map(seq2, f);
    }

    public static <R, T> ISeq<R> mapcat(Function<? super T, ? extends Iterable<? extends R>> f, ISeq<T> seq) {
        return seq.mapcat(f);
    }

    public static <R, S, T> ISeq<R> mapcat(BiFunction<? super T, ? super S, Iterable<? extends R>> f, ISeq<T> seq1, ISeq<? extends S> seq2) {
        return seq1.mapcat(seq2, f);
    }

    public static <T> ISeq<T> take(long n, ISeq<T> seq) {
        return seq.take(n);
    }

    public static <T> ISeq<T> drop(long n, ISeq<T> seq) {
        return seq.drop(n);
    }

    public static <T> ISeq<T> takeWhile(Predicate<? super T> pred, ISeq<T> seq) {
        return seq.takeWhile(pred);
    }

    public static <T> ISeq<T> dropWhile(Predicate<? super T> pred, ISeq<T> seq) {
        return seq.dropWhile(pred);
    }

    public static <T> ISeq<List<T>> partition(int n, ISeq<T> seq) {
        return seq.partition(n);
    }

    public static <T> ISeq<List<T>> partition(int n, int step, ISeq<T> seq) {
        return seq.partition(n, step);
    }

    public static <T> ISeq<List<T>> partition(int n, int step, Iterable<T> pad, ISeq<T> seq) {
        return seq.partition(n, step, pad);
    }

    public static <T> ISeq<List<T>> partitionAll(int n, ISeq<T> seq) {
        return seq.partitionAll(n);
    }

    public static <T> ISeq<List<T>> partitionAll(int n, int step, ISeq<T> seq) {
        return seq.partitionAll(n, step);
    }

    public static <T> ISeq<T> reductions(BinaryOperator<T> f, ISeq<T> seq) {
        return seq.reductions(f);
    }

    public static <T, U> ISeq<U> reductions(U init, BiFunction<U, ? super T, U> f, ISeq<T> seq) {
        return seq.reductions(init, f);
    }

    public static <T> Optional<T> reduce(BinaryOperator<T> f, ISeq<T> seq) {
        return seq.reduce(f);
    }

    public static <T, U> U reduce(U val, BiFunction<U, ? super T, U> f, ISeq<T> seq) {
        return seq.reduce(val, f);
    }

    public static <T> void run(Consumer<? super T> proc, ISeq<T> seq) {
        seq.run(proc);
    }

    public static <T> ISeq<T> distinct(ISeq<T> seq) {
        return seq.distinct();
    }

    @SuppressWarnings("unchecked")
    public static <T> ISeq<T> sort(ISeq<T> seq) {
        return seq.sorted();
    }

    public static <T> ISeq<T> sort(Comparator<? super T> comp, ISeq<T> seq) {
        return seq.sorted(comp);
    }

    public static <T> ISeq<T> reverse(ISeq<T> seq) {
        var iter = seq.iterator();
        var acc = Fn.<T>nil();
        while(iter.hasNext()) {
            acc = Fn.cons(iter.next(), acc);
        }
        return acc;
    }

    public static <T> boolean some(Predicate<? super T> pred, ISeq<T> seq) {
        return seq.some(pred);
    }

    public static <T> boolean every(Predicate<? super T> pred, ISeq<T> seq) {
        return seq.every(pred);
    }

    public static <T> boolean notAny(Predicate<? super T> pred, ISeq<T> seq) {
        return seq.notAny(pred);
    }

    public static <T> Optional<T> max(Comparator<? super T> comp, ISeq<T> seq) {
        return seq.max(comp);
    }

    public static <T> Optional<T> min(Comparator<? super T> comp, ISeq<T> seq) {
        return seq.max(comp.reversed());
    }

    public static <T, C extends Comparable<? super C>> Optional<T> maxKey(Function<T, C> f, ISeq<T> seq) {
        return seq.max(Comparator.comparing(t -> f.apply(t)));
    }

    public static <T, C extends Comparable<? super C>> Optional<T> minKey(Function<T, C> f, ISeq<T> seq) {
        return seq.min(Comparator.comparing(t -> f.apply(t)));
    }

    public static <T> T nth(ISeq<T> seq, int index) {
        return seq.nth(index);
    }

    public static <T> T nth(ISeq<T> seq, int index, T notFound) {
        return seq.nth(index, notFound);
    }
}
