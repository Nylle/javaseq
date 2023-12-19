package com.github.nylle.javaseq;

import java.nio.CharBuffer;
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
    static <T> ISeq<T> chunkIteratorSeq(final Iterator<T> iterator) {
        if(iterator.hasNext()) {
            return lazySeq(() -> {
                T[] arr = (T[]) new Object[CHUNK_SIZE];
                int n = 0;
                while(iterator.hasNext() && n < CHUNK_SIZE) {
                    arr[n++] = iterator.next();
                }
                return new ChunkedCons<>(new ArrayChunk<>(arr, 0, n), chunkIteratorSeq(iterator));
            });
        }
        return nil();
    }

    // util

    public static <T> Set<T> conj(Set<T> coll, T x) {
        var result = new HashSet<>(coll);
        result.add(x);
        return Set.copyOf(result);
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

    @SafeVarargs
    public static <T> ISeq<T> arraySeq(T... items) {
        return new ArraySeq<>(items);
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

    public static ISeq<Character> seq(char[] coll) {
        if (coll != null && coll.length > 0) {
            return new StringSeq(CharBuffer.wrap(coll), 0, coll.length);
        }
        return nil();
    }

    public static ISeq<Character> seq(Character[] coll) {
        if (coll != null && coll.length > 0) {
            var s = new StringBuilder(coll.length);
            for (Character c : coll) {
                s.append(c.charValue());
            }
            return new StringSeq(s.toString(), 0, coll.length);
        }
        return nil();
    }

    public static ISeq<Character> seq(CharSequence coll) {
        if (coll != null && !coll.isEmpty()) {
            return new StringSeq(coll, 0, coll.length());
        }
        return nil();
    }

    public static <T> ISeq<T> seq(Iterator<T> coll) {
        if (coll != null && coll.hasNext()) {
            return chunkIteratorSeq(coll);
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


    //TODO: overloads for
    // Iterator<T>
    // Map<K, V>

    public static <T> T first(Iterable<T> coll) {
        return seq(coll).first();
    }

    public static <T> T first(Stream<T> coll) {
        return seq(coll).first();
    }

    public static <T> T first(T[] coll) {
        return seq(coll).first();
    }

    public static Character first(CharSequence coll) {
        return seq(coll).first();
    }


    public static <T> T second(Iterable<T> coll) {
        return seq(coll).second();
    }

    public static <T> T second(Stream<T> coll) {
        return seq(coll).second();
    }

    public static <T> T second(T[] coll) {
        return seq(coll).second();
    }

    public static Character second(CharSequence coll) {
        return seq(coll).second();
    }


    public static <T> ISeq<T> filter(Predicate<? super T> pred, Iterable<T> coll) {
        return seq(coll).filter(pred);
    }

    public static <T> ISeq<T> filter(Predicate<? super T> pred, Stream<T> coll) {
        return seq(coll).filter(pred);
    }

    public static <T> ISeq<T> filter(Predicate<? super T> pred, T[] coll) {
        return seq(coll).filter(pred);
    }

    public static ISeq<Character> filter(Predicate<? super Character> pred, CharSequence coll) {
        return seq(coll).filter(pred);
    }


    public static <R, T> ISeq<R> map(Function<? super T, ? extends R> f, Iterable<? extends T> coll) {
        return seq(coll).map(f);
    }

    public static <R, T> ISeq<R> map(Function<? super T, ? extends R> f, Stream<? extends T> coll) {
        return seq(coll).map(f);
    }

    public static <R, T> ISeq<R> map(Function<? super T, ? extends R> f, T[] coll) {
        return seq(coll).map(f);
    }

    public static <R> ISeq<R> map(Function<? super Character, ? extends R> f, CharSequence coll) {
        return seq(coll).map(f);
    }


    public static <R, S, T> ISeq<R> map(BiFunction<? super T, ? super S, ? extends R> f, Iterable<? extends T> coll1, Iterable<? extends S> coll2) {
        return seq(coll1).map(coll2, f);
    }


    public static <R, T> ISeq<R> mapcat(Function<? super T, ? extends Iterable<? extends R>> f, Iterable<? extends T> coll) {
        return seq(coll).mapcat(f);
    }

    public static <R, T> ISeq<R> mapcat(Function<? super T, ? extends Iterable<? extends R>> f, Stream<? extends T> coll) {
        return seq(coll).mapcat(f);
    }

    public static <R, T> ISeq<R> mapcat(Function<? super T, ? extends Iterable<? extends R>> f, T[] coll) {
        return seq(coll).mapcat(f);
    }

    public static <R, T> ISeq<R> mapcat(Function<? super Character, ? extends Iterable<? extends R>> f, CharSequence coll) {
        return seq(coll).mapcat(f);
    }


    public static <R, S, T> ISeq<R> mapcat(BiFunction<? super T, ? super S, Iterable<? extends R>> f, Iterable<? extends T> coll1, Iterable<? extends S> coll2) {
        return seq(coll1).mapcat(coll2, f);
    }


    public static <T> ISeq<T> take(long n, Iterable<T> coll) {
        return seq(coll).take(n);
    }

    public static <T> ISeq<T> take(long n, Stream<T> coll) {
        return seq(coll).take(n);
    }

    public static <T> ISeq<T> take(long n, T[] coll) {
        return seq(coll).take(n);
    }

    public static ISeq<Character> take(long n, CharSequence coll) {
        return seq(coll).take(n);
    }


    public static <T> ISeq<T> drop(long n, Iterable<T> coll) {
        return seq(coll).drop(n);
    }

    public static <T> ISeq<T> drop(long n, Stream<T> coll) {
        return seq(coll).drop(n);
    }

    public static <T> ISeq<T> drop(long n, T[] coll) {
        return seq(coll).drop(n);
    }

    public static ISeq<Character> drop(long n, CharSequence coll) {
        return seq(coll).drop(n);
    }


    public static <T> ISeq<T> takeWhile(Predicate<? super T> pred, Iterable<T> coll) {
        return seq(coll).takeWhile(pred);
    }

    public static <T> ISeq<T> takeWhile(Predicate<? super T> pred, Stream<T> coll) {
        return seq(coll).takeWhile(pred);
    }

    public static <T> ISeq<T> takeWhile(Predicate<? super T> pred, T[] coll) {
        return seq(coll).takeWhile(pred);
    }

    public static ISeq<Character> takeWhile(Predicate<? super Character> pred, CharSequence coll) {
        return seq(coll).takeWhile(pred);
    }


    public static <T> ISeq<T> dropWhile(Predicate<? super T> pred, Iterable<T> coll) {
        return seq(coll).dropWhile(pred);
    }

    public static <T> ISeq<T> dropWhile(Predicate<? super T> pred, Stream<T> coll) {
        return seq(coll).dropWhile(pred);
    }

    public static <T> ISeq<T> dropWhile(Predicate<? super T> pred, T[] coll) {
        return seq(coll).dropWhile(pred);
    }

    public static ISeq<Character> dropWhile(Predicate<? super Character> pred, CharSequence coll) {
        return seq(coll).dropWhile(pred);
    }


    public static <T> ISeq<List<T>> partition(int n, Iterable<T> coll) {
        return seq(coll).partition(n);
    }

    public static <T> ISeq<List<T>> partition(int n, Stream<T> coll) {
        return seq(coll).partition(n);
    }

    public static <T> ISeq<List<T>> partition(int n, T[] coll) {
        return seq(coll).partition(n);
    }

    public static ISeq<List<Character>> partition(int n, CharSequence coll) {
        return seq(coll).partition(n);
    }


    public static <T> ISeq<List<T>> partition(int n, int step, Iterable<T> coll) {
        return seq(coll).partition(n, step);
    }

    public static <T> ISeq<List<T>> partition(int n, int step, Stream<T> coll) {
        return seq(coll).partition(n, step);
    }

    public static <T> ISeq<List<T>> partition(int n, int step, T[] coll) {
        return seq(coll).partition(n, step);
    }

    public static ISeq<List<Character>> partition(int n, int step, CharSequence coll) {
        return seq(coll).partition(n, step);
    }


    public static <T> ISeq<List<T>> partition(int n, int step, Iterable<T> pad, Iterable<T> coll) {
        return seq(coll).partition(n, step, pad);
    }

    public static <T> ISeq<List<T>> partition(int n, int step, Iterable<T> pad, Stream<T> coll) {
        return seq(coll).partition(n, step, pad);
    }

    public static <T> ISeq<List<T>> partition(int n, int step, Iterable<T> pad, T[] coll) {
        return seq(coll).partition(n, step, pad);
    }

    public static ISeq<List<Character>> partition(int n, int step, Iterable<Character> pad, CharSequence coll) {
        return seq(coll).partition(n, step, pad);
    }


    public static <T> ISeq<List<T>> partitionAll(int n, Iterable<T> coll) {
        return seq(coll).partitionAll(n);
    }

    public static <T> ISeq<List<T>> partitionAll(int n, Stream<T> coll) {
        return seq(coll).partitionAll(n);
    }

    public static <T> ISeq<List<T>> partitionAll(int n, T[] coll) {
        return seq(coll).partitionAll(n);
    }

    public static ISeq<List<Character>> partitionAll(int n, CharSequence coll) {
        return seq(coll).partitionAll(n);
    }


    public static <T> ISeq<List<T>> partitionAll(int n, int step, Iterable<T> coll) {
        return seq(coll).partitionAll(n, step);
    }

    public static <T> ISeq<List<T>> partitionAll(int n, int step, Stream<T> coll) {
        return seq(coll).partitionAll(n, step);
    }

    public static <T> ISeq<List<T>> partitionAll(int n, int step, T[] coll) {
        return seq(coll).partitionAll(n, step);
    }

    public static ISeq<List<Character>> partitionAll(int n, int step, CharSequence coll) {
        return seq(coll).partitionAll(n, step);
    }


    public static <T> ISeq<T> reductions(BinaryOperator<T> f, Iterable<T> coll) {
        return seq(coll).reductions(f);
    }

    public static <T> ISeq<T> reductions(BinaryOperator<T> f, Stream<T> coll) {
        return seq(coll).reductions(f);
    }

    public static <T> ISeq<T> reductions(BinaryOperator<T> f, T[] coll) {
        return seq(coll).reductions(f);
    }

    public static ISeq<Character> reductions(BinaryOperator<Character> f, CharSequence coll) {
        return seq(coll).reductions(f);
    }


    public static <T, U> ISeq<U> reductions(U init, BiFunction<U, ? super T, U> f, Iterable<? extends T> coll) {
        return seq(coll).reductions(init, f);
    }

    public static <T, U> ISeq<U> reductions(U init, BiFunction<U, ? super T, U> f, Stream<? extends T> coll) {
        return seq(coll).reductions(init, f);
    }

    public static <T, U> ISeq<U> reductions(U init, BiFunction<U, ? super T, U> f, T[] coll) {
        return seq(coll).reductions(init, f);
    }

    public static <U> ISeq<U> reductions(U init, BiFunction<U, ? super Character, U> f, CharSequence coll) {
        return seq(coll).reductions(init, f);
    }


    public static <T> Optional<T> reduce(BinaryOperator<T> f, Iterable<T> coll) {
        return seq(coll).reduce(f);
    }

    public static <T> Optional<T> reduce(BinaryOperator<T> f, Stream<T> coll) {
        return seq(coll).reduce(f);
    }

    public static <T> Optional<T> reduce(BinaryOperator<T> f, T[] coll) {
        return seq(coll).reduce(f);
    }

    public static Optional<Character> reduce(BinaryOperator<Character> f, CharSequence coll) {
        return seq(coll).reduce(f);
    }


    public static <T, U> U reduce(U val, BiFunction<U, ? super T, U> f, Iterable<? extends T> coll) {
        return seq(coll).reduce(val, f);
    }

    public static <T, U> U reduce(U val, BiFunction<U, ? super T, U> f, Stream<? extends T> coll) {
        return seq(coll).reduce(val, f);
    }

    public static <T, U> U reduce(U val, BiFunction<U, ? super T, U> f, T[] coll) {
        return seq(coll).reduce(val, f);
    }

    public static <U> U reduce(U val, BiFunction<U, ? super Character, U> f, CharSequence coll) {
        return seq(coll).reduce(val, f);
    }


    public static <T> void run(Consumer<? super T> proc, Iterable<? extends T> coll) {
        seq(coll).run(proc);
    }

    public static <T> void run(Consumer<? super T> proc, Stream<? extends T> coll) {
        seq(coll).run(proc);
    }

    public static <T> void run(Consumer<? super T> proc, T[] coll) {
        seq(coll).run(proc);
    }

    public static void run(Consumer<? super Character> proc, CharSequence coll) {
        seq(coll).run(proc);
    }


    public static <T> ISeq<T> distinct(Iterable<T> coll) {
        return seq(coll).distinct();
    }

    public static <T> ISeq<T> distinct(Stream<T> coll) {
        return seq(coll).distinct();
    }

    public static <T> ISeq<T> distinct(T[] coll) {
        return seq(coll).distinct();
    }

    public static ISeq<Character> distinct(CharSequence coll) {
        return seq(coll).distinct();
    }


    public static <T> ISeq<T> sort(Iterable<T> coll) {
        return seq(coll).sorted();
    }

    public static <T> ISeq<T> sort(Stream<T> coll) {
        return seq(coll).sorted();
    }

    public static <T> ISeq<T> sort(T[] coll) {
        return seq(coll).sorted();
    }

    public static ISeq<Character> sort(CharSequence coll) {
        return seq(coll).sorted();
    }


    public static <T> ISeq<T> sort(Comparator<? super T> comp, Iterable<T> coll) {
        return seq(coll).sorted(comp);
    }

    public static <T> ISeq<T> sort(Comparator<? super T> comp, Stream<T> coll) {
        return seq(coll).sorted(comp);
    }

    public static <T> ISeq<T> sort(Comparator<? super T> comp, T[] coll) {
        return seq(coll).sorted(comp);
    }

    public static ISeq<Character> sort(Comparator<? super Character> comp, CharSequence coll) {
        return seq(coll).sorted(comp);
    }


    public static <T> ISeq<T> reverse(Iterable<T> coll) {
        return seq(coll).reverse();
    }

    public static <T> ISeq<T> reverse(Stream<T> coll) {
        return seq(coll).reverse();
    }

    public static <T> ISeq<T> reverse(T[] coll) {
        return seq(coll).reverse();
    }

    public static ISeq<Character> reverse(CharSequence coll) {
        return seq(coll).reverse();
    }


    public static <T> boolean some(Predicate<? super T> pred, Iterable<T> coll) {
        return seq(coll).some(pred);
    }

    public static <T> boolean some(Predicate<? super T> pred, Stream<T> coll) {
        return seq(coll).some(pred);
    }

    public static <T> boolean some(Predicate<? super T> pred, T[] coll) {
        return seq(coll).some(pred);
    }

    public static boolean some(Predicate<? super Character> pred, CharSequence coll) {
        return seq(coll).some(pred);
    }


    public static <T> boolean every(Predicate<? super T> pred, Iterable<T> coll) {
        return seq(coll).every(pred);
    }

    public static <T> boolean every(Predicate<? super T> pred, Stream<T> coll) {
        return seq(coll).every(pred);
    }

    public static <T> boolean every(Predicate<? super T> pred, T[] coll) {
        return seq(coll).every(pred);
    }

    public static boolean every(Predicate<? super Character> pred, CharSequence coll) {
        return seq(coll).every(pred);
    }


    public static <T> boolean notAny(Predicate<? super T> pred, Iterable<T> coll) {
        return seq(coll).notAny(pred);
    }

    public static <T> boolean notAny(Predicate<? super T> pred, Stream<T> coll) {
        return seq(coll).notAny(pred);
    }

    public static <T> boolean notAny(Predicate<? super T> pred, T[] coll) {
        return seq(coll).notAny(pred);
    }

    public static boolean notAny(Predicate<? super Character> pred, CharSequence coll) {
        return seq(coll).notAny(pred);
    }


    public static <T> Optional<T> max(Comparator<? super T> comp, Iterable<T> coll) {
        return seq(coll).max(comp);
    }

    public static <T> Optional<T> max(Comparator<? super T> comp, Stream<T> coll) {
        return seq(coll).max(comp);
    }

    public static <T> Optional<T> max(Comparator<? super T> comp, T[] coll) {
        return seq(coll).max(comp);
    }

    public static Optional<Character> max(Comparator<? super Character> comp, CharSequence coll) {
        return seq(coll).max(comp);
    }


    public static <T> Optional<T> min(Comparator<? super T> comp, Iterable<T> coll) {
        return seq(coll).max(comp.reversed());
    }

    public static <T> Optional<T> min(Comparator<? super T> comp, Stream<T> coll) {
        return seq(coll).max(comp.reversed());
    }

    public static <T> Optional<T> min(Comparator<? super T> comp, T[] coll) {
        return seq(coll).max(comp.reversed());
    }

    public static Optional<Character> min(Comparator<? super Character> comp, CharSequence coll) {
        return seq(coll).max(comp.reversed());
    }


    public static <T, C extends Comparable<? super C>> Optional<T> maxKey(Function<T, C> f, Iterable<T> coll) {
        return seq(coll).max(Comparator.comparing(t -> f.apply(t)));
    }

    public static <T, C extends Comparable<? super C>> Optional<T> maxKey(Function<T, C> f, Stream<T> coll) {
        return seq(coll).max(Comparator.comparing(t -> f.apply(t)));
    }

    public static <T, C extends Comparable<? super C>> Optional<T> maxKey(Function<T, C> f, T[] coll) {
        return seq(coll).max(Comparator.comparing(t -> f.apply(t)));
    }

    public static <C extends Comparable<? super C>> Optional<Character> maxKey(Function<Character, C> f, CharSequence coll) {
        return seq(coll).max(Comparator.comparing(t -> f.apply(t)));
    }


    public static <T, C extends Comparable<? super C>> Optional<T> minKey(Function<T, C> f, Iterable<T> coll) {
        return seq(coll).min(Comparator.comparing(t -> f.apply(t)));
    }

    public static <T, C extends Comparable<? super C>> Optional<T> minKey(Function<T, C> f, Stream<T> coll) {
        return seq(coll).min(Comparator.comparing(t -> f.apply(t)));
    }

    public static <T, C extends Comparable<? super C>> Optional<T> minKey(Function<T, C> f, T[] coll) {
        return seq(coll).min(Comparator.comparing(t -> f.apply(t)));
    }

    public static <C extends Comparable<? super C>> Optional<Character> minKey(Function<Character, C> f, CharSequence coll) {
        return seq(coll).min(Comparator.comparing(t -> f.apply(t)));
    }


    public static <T> T nth(Iterable<T> coll, int index) {
        return seq(coll).nth(index);
    }

    public static <T> T nth(Stream<T> coll, int index) {
        return seq(coll).nth(index);
    }

    public static <T> T nth(T[] coll, int index) {
        return seq(coll).nth(index);
    }

    public static Character nth(CharSequence coll, int index) {
        return seq(coll).nth(index);
    }


    public static <T> T nth(Iterable<T> coll, int index, T notFound) {
        return seq(coll).nth(index, notFound);
    }

    public static <T> T nth(Stream<T> coll, int index, T notFound) {
        return seq(coll).nth(index, notFound);
    }

    public static <T> T nth(T[] coll, int index, T notFound) {
        return seq(coll).nth(index, notFound);
    }

    public static Character nth(CharSequence coll, int index, Character notFound) {
        return seq(coll).nth(index, notFound);
    }


    public static String str(Object o) {
        if(o instanceof Iterable coll) {
            return seq(coll).str();
        }
        return o.toString();
    }
}
