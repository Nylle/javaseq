package com.github.nylle.javaseq;

import java.nio.CharBuffer;
import java.util.ArrayList;
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

    static <T> ISeq<T> arraySeq(T[] array) {
        if (array != null && array.length > 0) return new ArraySeq<>(array);
        return nil();
    }

    static <T> ISeq<T> chunkIteratorSeq(final Iterator<T> iterator) {
        if (iterator.hasNext()) {
            return lazySeq(() -> {
                T[] arr = (T[]) new Object[CHUNK_SIZE];
                int n = 0;
                while (iterator.hasNext() && n < CHUNK_SIZE) {
                    arr[n++] = iterator.next();
                }
                return new ChunkedCons<>(new ArrayChunk<>(arr, 0, n), chunkIteratorSeq(iterator));
            });
        }
        return nil();
    }

    // util

    /**
     * conj[oin]. Returns a new Set with the {@code xs} 'added'. The 'addition' may happen anywhere.
     * <p>
     * <b>Note: </b>This will not return a persistent data structure, instead a full copy with the item included.
     *
     * @param coll the set to add to
     * @param xs   the items to add
     * @param <T>  the type of the items in the set
     * @return a new Set with the xs added
     */
    @SafeVarargs
    public static <T> Set<T> conj(Set<T> coll, T... xs) {
        var result = new HashSet<>(coll);
        result.addAll(Arrays.asList(xs));
        return Set.copyOf(result);
    }

    /**
     * conj[oin]. Returns a new List with the {@code xs} 'added'. The 'addition' will happen at the end.
     * <p>
     * <b>Note: </b>This will not return a persistent data structure, instead a full copy with the item included.
     *
     * @param coll the list to add to
     * @param xs   the items to add
     * @param <T>  the type of the items in the list
     * @return a new List with the xs added
     */
    @SafeVarargs
    public static <T> List<T> conj(List<T> coll, T... xs) {
        var result = new ArrayList<>(coll);
        result.addAll(Arrays.asList(xs));
        return List.copyOf(result);
    }

    /**
     * conj[oin]. Returns a new ISeq with the {@code xs} 'added'.  The 'addition' will happen at the beginning.
     *
     * @param coll the seq to add to
     * @param xs   the items to add
     * @param <T>  the type of the items in the seq
     * @return a new ISeq with the xs added
     */
    @SafeVarargs
    public static <T> ISeq<T> conj(ISeq<T> coll, T... xs) {
        var result = coll;
        for (var x : xs) {
            result = Fn.cons(x, result);
        }
        return result;
    }

    // creation

    /**
     * Returns an empty seq.
     *
     * @param <T> the type of the items in the seq
     * @return an empty seq
     */
    public static <T> ISeq<T> nil() {
        return Nil.empty();
    }

    /**
     * Returns a new seq where {@code x} is the first item and {@code seq} is the rest.
     *
     * @param x   the item to add
     * @param seq the seq to add to
     * @param <T> the type of the items in the seq
     * @return a new seq where x is the first item and seq is the rest
     */
    public static <T> ISeq<T> cons(T x, Iterable<T> seq) {
        return new Cons<>(x, seq(seq));
    }

    /**
     * Takes a body of expressions that returns an ISeq or Nil, and yields an ISeq that will invoke the body only the
     * first time it is accessed, and will cache the result and return it on all subsequent calls.
     *
     * @param f   the supplier
     * @param <T> the type of the items in the lazy seq
     * @return a lazy seq
     * @see ISeq#isRealized
     */
    public static <T> ISeq<T> lazySeq(Supplier<ISeq<T>> f) {
        return new LazySeq<>(f);
    }

    /**
     * Coerces {@code coll} to a (possibly empty) seq, if it is not already one. Will not force a lazy collection
     * like {@code Stream} or {@code Iterator}. If {@code coll} is a {@code String}, the returned seq will contain items
     * of type {@code Character}. If {@code coll} is a {@code Map} the returned seq will contain items of type
     * {@code Map.Entry} (see {@link ISeq#toMap()}). Yields empty seq if {@code coll} is null or empty.
     *
     * @param coll a collection to be coerced to a seq
     * @param <T>  the type of items in the seq
     * @return a seq of items in coll
     */
    public static <T> ISeq<T> seq(T[] coll) {
        return arraySeq(coll);
    }

    /**
     * Coerces {@code coll} to a (possibly empty) seq, if it is not already one. Will not force a lazy collection
     * like {@code Stream} or {@code Iterator}. If {@code coll} is a {@code String}, the returned seq will contain items
     * of type {@code Character}. If {@code coll} is a {@code Map} the returned seq will contain items of type
     * {@code Map.Entry} (see {@link ISeq#toMap()}). Yields empty seq if {@code coll} is null or empty.
     *
     * @param coll a collection to be coerced to a seq
     * @param <T>  the type of items in the seq
     * @return a seq of items in coll
     */
    public static <T> ISeq<T> seq(Iterable<T> coll) {
        if (coll == null) return nil();
        if (coll instanceof ISeq<T> seq) return seq;
        if (coll instanceof ArrayList<T> arrayList) return arraySeq((T[]) arrayList.toArray());
        return seq(coll.iterator());
    }

    /**
     * Coerces {@code coll} to a (possibly empty) seq, if it is not already one. Will not force a lazy collection
     * like {@code Stream} or {@code Iterator}. If {@code coll} is a {@code String}, the returned seq will contain items
     * of type {@code Character}. If {@code coll} is a {@code Map} the returned seq will contain items of type
     * {@code Map.Entry} (see {@link ISeq#toMap()}). Yields empty seq if {@code coll} is null or empty.
     *
     * @param coll a collection to be coerced to a seq
     * @param <T>  the type of items in the seq
     * @return a seq of items in coll
     */
    public static <T> ISeq<T> seq(Stream<T> coll) {
        if (coll != null) {
            return seq(coll.iterator());
        }
        return nil();
    }

    /**
     * Coerces {@code coll} to a (possibly empty) seq, if it is not already one. Will not force a lazy collection
     * like {@code Stream} or {@code Iterator}. If {@code coll} is a {@code String}, the returned seq will contain items
     * of type {@code Character}. If {@code coll} is a {@code Map} the returned seq will contain items of type
     * {@code Map.Entry} (see {@link ISeq#toMap()}). Yields empty seq if {@code coll} is null or empty.
     *
     * @param coll a collection to be coerced to a seq
     * @param <K>  the type of key in entry in the seq
     * @param <V>  the type of value in entry in the seq
     * @return a seq of items in coll
     */
    public static <K, V> ISeq<Map.Entry<K, V>> seq(Map<K, V> coll) {
        if (coll != null) {
            return seq(coll.entrySet().iterator());
        }
        return nil();
    }

    /**
     * Coerces {@code coll} to a (possibly empty) seq, if it is not already one. Will not force a lazy collection
     * like {@code Stream} or {@code Iterator}. If {@code coll} is a {@code String}, the returned seq will contain items
     * of type {@code Character}. If {@code coll} is a {@code Map} the returned seq will contain items of type
     * {@code Map.Entry} (see {@link ISeq#toMap()}). Yields empty seq if {@code coll} is null or empty.
     *
     * @param coll a collection to be coerced to a seq
     * @return a seq of items in coll
     */
    public static ISeq<Character> seq(char[] coll) {
        if (coll != null && coll.length > 0) {
            return new StringSeq(CharBuffer.wrap(coll), 0, coll.length);
        }
        return nil();
    }

    /**
     * Coerces {@code coll} to a (possibly empty) seq, if it is not already one. Will not force a lazy collection
     * like {@code Stream} or {@code Iterator}. If {@code coll} is a {@code String}, the returned seq will contain items
     * of type {@code Character}. If {@code coll} is a {@code Map} the returned seq will contain items of type
     * {@code Map.Entry} (see {@link ISeq#toMap()}). Yields empty seq if {@code coll} is null or empty.
     *
     * @param coll a collection to be coerced to a seq
     * @return a seq of items in coll
     */
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

    /**
     * Coerces {@code coll} to a (possibly empty) seq, if it is not already one. Will not force a lazy collection
     * like {@code Stream} or {@code Iterator}. If {@code coll} is a {@code String}, the returned seq will contain items
     * of type {@code Character}. If {@code coll} is a {@code Map} the returned seq will contain items of type
     * {@code Map.Entry} (see {@link ISeq#toMap()}). Yields empty seq if {@code coll} is null or empty.
     *
     * @param coll a collection to be coerced to a seq
     * @return a seq of items in coll
     */
    public static ISeq<Character> seq(CharSequence coll) {
        if (coll != null && !coll.isEmpty()) {
            return new StringSeq(coll, 0, coll.length());
        }
        return nil();
    }

    /**
     * Coerces {@code coll} to a (possibly empty) seq, if it is not already one. Will not force a lazy collection
     * like {@code Stream} or {@code Iterator}. If {@code coll} is a {@code String}, the returned seq will contain items
     * of type {@code Character}. If {@code coll} is a {@code Map} the returned seq will contain items of type
     * {@code Map.Entry} (see {@link ISeq#toMap()}). Yields empty seq if {@code coll} is null or empty.
     *
     * @param coll a collection to be coerced to a seq
     * @param <T>  the type of items in the seq
     * @return a seq of items in coll
     */
    public static <T> ISeq<T> seq(Iterator<T> coll) {
        if (coll != null && coll.hasNext()) {
            return chunkIteratorSeq(coll);
        }
        return nil();
    }

    /**
     * Returns a lazy seq of {@code x}, {@code f(x)}, {@code f(f(x))} etc. {@code f} must be free of side-effects.
     *
     * @param x   initial value
     * @param f   function to apply to x
     * @param <T> the type of items in the seq
     * @return a lazy seq of x, the result of applying f to x, the result of applying f to that, etc.
     */
    public static <T> ISeq<T> iterate(T x, UnaryOperator<T> f) {
        return lazySeq(() -> cons(x, iterate(f.apply(x), f)));
    }

    /**
     * Returns a lazy (infinite!) seq of {@code x}s.
     *
     * @param x   the item to repeat
     * @param <T> the type of x
     * @return a lazy (infinite!) seq of xs
     */
    static <T> ISeq<T> repeat(T x) {
        return Fn.iterate(x, i -> x);
    }

    /**
     * Returns a lazy seq of {@code x}s with length {@code n}.
     *
     * @param n   the number of times to repeat x
     * @param x   the item to repeat
     * @param <T> the type of x
     * @return a lazy seq of xs with length n
     */
    static <T> ISeq<T> repeat(int n, T x) {
        return Fn.iterate(x, i -> x).take(n);
    }

    /**
     * Returns a lazy seq of numbers from 0 (inclusive) to infinity, by step 1.
     *
     * @return a lazy seq of numbers from 0 (inclusive) to infinity, by step 1
     */
    static ISeq<Integer> range() {
        return Fn.iterate(0, x -> x + 1);
    }

    /**
     * Returns a lazy seq of numbers from 0 (inclusive) to {@code end} (exclusive), by step 1.
     * Returns empty seq when {@code end} is equal to 0.
     *
     * @param end exclusive end of the range
     * @return a lazy seq of numbers from 0 (inclusive) to end (exclusive)
     */
    static ISeq<Integer> range(int end) {
        return Fn.range(0, end);
    }

    /**
     * Returns a lazy seq of numbers from {@code start} (inclusive) to {@code end} (exclusive), by step 1.
     * Returns empty seq when {@code start} is equal to {@code end}.
     *
     * @param start inclusive start of the range
     * @param end   exclusive end of the range
     * @return a lazy seq of numbers from start (inclusive) to end (exclusive)
     */
    static ISeq<Integer> range(int start, int end) {
        return Fn.range(start, end, 1);
    }

    /**
     * Returns a lazy seq of numbers from {@code start} (inclusive) to {@code end} (exclusive), by {@code step}.
     * Returns infinite seq of {@code start} when {@code step} is equal to 0.
     * Returns empty seq when {@code start} is equal to {@code end}.
     *
     * @param start inclusive start of the range
     * @param end   exclusive end of the range
     * @param step  step by which to increase the next number
     * @return a lazy seq of numbers from start (inclusive) to end (exclusive), by step
     */
    static ISeq<Integer> range(int start, int end, int step) {
        return Fn.iterate(start, x -> x + step).takeWhile(x -> step >= 0 ? (x < end) : (x > end));
    }

    /**
     * Returns a lazy seq representing the concatenation of the items in {@code coll} and {@code x}.
     *
     * @param coll the collection to concat to
     * @param x    the item to append
     * @param <T>  the type of the items in the returned seq
     * @return a lazy seq representing the concatenation of the items in coll and x
     */
    public static <T> ISeq<T> concat(Iterable<T> coll, T x) {
        return concat(seq(coll), ISeq.of(x));
    }

    /**
     * Returns a lazy seq representing the concatenation of the items in {@code coll} and {@code x}.
     *
     * @param coll the CharSequence to concat to
     * @param x    the character to append
     * @return a lazy seq representing the concatenation of the items in coll and x
     */
    public static ISeq<Character> concat(CharSequence coll, Character x) {
        return concat(seq(coll), ISeq.of(x));
    }

    /**
     * Returns a lazy seq representing the concatenation of the items in the supplied {@code colls}.
     *
     * @param colls the collections to concatenate into a seq
     * @param <T>   the type of the items in the returned seq
     * @return a lazy seq representing the concatenation of the items in the supplied colls
     */
    @SafeVarargs
    public static <T> ISeq<T> concat(Iterable<T>... colls) {
        return seq(colls).mapcat(x -> x);
    }

    /**
     * Returns a lazy seq representing the concatenation of the items in the supplied {@code colls}.
     *
     * @param colls the strings to concatenate into a seq
     * @return a lazy seq representing the concatenation of the items in the supplied colls
     */
    public static ISeq<Character> concat(CharSequence... colls) {
        var sb = new StringBuilder();
        for (var coll : colls) {
            sb.append(coll);
        }
        return seq(sb.toString());
    }

    /**
     * Returns a lazy seq representing the concatenation of the items in {@code iterator} and {@code seq}.
     *
     * @param iterator the iterator to concatenate to
     * @param seq      the seq whose items to append
     * @param <T>      the type of the items in the returned seq
     * @return a lazy seq representing the concatenation of the items in iterator and seq
     */
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


//    public static <T> Function<Iterable<T>, ISeq<T>> filter(Predicate<? super T> pred) {
//        return (coll) -> seq(coll).filter(pred);
//    }

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
        if (o instanceof Iterable coll) {
            return seq(coll).str();
        }
        return o.toString();
    }
}
