package com.github.nylle.javaseq;

import java.nio.CharBuffer;
import java.util.ArrayList;
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
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public interface ISeq<T> extends List<T> {

    /**
     * Returns an empty seq.
     *
     * @param <T> the type of items in the seq
     * @return an empty seq
     */
    @SuppressWarnings("unchecked")
    static <T> ISeq<T> of() {
        return Util.nil();
    }

    /**
     * Returns a seq of all supplied {@code xs}.
     *
     * @param xs  items to be contained in the seq
     * @param <T> the type of items in the seq
     * @return a seq of all supplied xs
     */
    @SafeVarargs
    static <T> ISeq<T> of(T... xs) {
        return Util.arraySeq(xs);
    }

    /**
     * Returns a seq where {@code x} is the first item and {@code coll} is the rest.
     *
     * @param x    the first item
     * @param coll the remaining items
     * @param <T>  the type of the items
     * @return a seq where x is the first item and coll is the rest
     */
    static <T> ISeq<T> cons(T x, Iterable<T> coll) {
        return Util.cons(x, ISeq.seq(coll));
    }

    /**
     * Takes a {@code body} of expressions (supplier) that returns an ISeq or Nil that will invoke the body only the
     * first time it is accessed, and will cache the result and return it on all subsequent calls.
     *
     * @param body the supplier
     * @param <T>  the type of the items in the seq
     * @return a lazy seq
     * @see ISeq#isRealized
     */
    static <T> ISeq<T> lazySeq(Supplier<ISeq<T>> body) {
        return Util.lazySeq(body);
    }

    /**
     * Coerces {@code coll} to a (possibly empty) seq.
     *
     * @param coll an array to be coerced to a seq
     * @param <T>  the type of items in coll
     * @return a seq of items in coll
     */
    static <T> ISeq<T> seq(T[] coll) {
        return Util.arraySeq(coll);
    }

    /**
     * Coerces {@code coll} to a (possibly empty) lazy seq, if it is not already one. Will not force a lazy collection.
     *
     * @param coll an iterator to be coerced to a seq
     * @param <T>  the type of items in coll
     * @return a seq of items in coll
     */
    static <T> ISeq<T> seq(Iterable<T> coll) {
        if (coll == null) return Util.nil();
        if (coll instanceof ISeq<T> seq) return seq;
        if (coll instanceof ArrayList<T> arrayList) return Util.arraySeq((T[]) arrayList.toArray());
        if (coll.getClass().getName().equals("java.util.ImmutableCollections$ListN")) return Util.arraySeq((T[]) ((List<T>)(coll)).toArray());
        if (coll.getClass().getName().equals("java.util.ImmutableCollections$List12")) return Util.arraySeq((T[]) ((List<T>)(coll)).toArray());
        return seq(coll.iterator());
    }

    /**
     * Coerces {@code coll} to a (possibly empty) lazy seq. Will not force the {@code Iterator}.
     *
     * @param coll a collection to be coerced to a seq
     * @param <T>  the type of items in the seq
     * @return a seq of items in coll
     */
    static <T> ISeq<T> seq(Iterator<T> coll) {
        if (coll != null && coll.hasNext()) {
            return Util.chunkIteratorSeq(coll);
        }
        return Util.nil();
    }

    /**
     * Coerces {@code coll} to a (possibly empty) seq. Will not force the {@code Stream}.
     *
     * @param coll a stream to be coerced to a seq
     * @param <T>  the type of items in the stream
     * @return a seq of items in coll
     */
    static <T> ISeq<T> seq(Stream<T> coll) {
        if (coll != null) {
            return seq(coll.iterator());
        }
        return Util.nil();
    }

    /**
     * Coerces {@code coll} to a (possibly empty) seq. The returned seq will contain items of type {@code Character}.
     *
     * @param coll an array to be coerced to a seq
     * @return a seq of items in coll
     * @see #str
     */
    static ISeq<Character> seq(char[] coll) {
        if (coll != null && coll.length > 0) {
            return Util.stringSeq(CharBuffer.wrap(coll));
        }
        return Util.nil();
    }

    /**
     * Coerces {@code coll} to a (possibly empty) seq. The returned seq will contain items of type {@code Character}.
     *
     * @param coll an array to be coerced to a seq
     * @return a seq of items in coll
     * @see #str
     */
    static ISeq<Character> seq(Character[] coll) {
        if (coll != null && coll.length > 0) {
            var s = new StringBuilder(coll.length);
            for (Character c : coll) {
                s.append(c.charValue());
            }
            return Util.stringSeq(s.toString());
        }
        return Util.nil();
    }

    /**
     * Coerces {@code coll} to a (possibly empty) seq. The returned seq will contain items of type {@code Character}.
     *
     * @param coll a string to be coerced to a seq
     * @return a seq of items in coll
     * @see #str
     */
    static ISeq<Character> seq(CharSequence coll) {
        if (coll != null && !coll.isEmpty()) {
            return Util.stringSeq(coll);
        }
        return Util.nil();
    }

    /**
     * Coerces {@code coll} to a (possibly empty) seq. Will not force a lazy collection. The returned seq will contain
     * items of type {@code Map.Entry}.
     *
     * @param coll a map to be coerced to a seq
     * @param <K>  the type of key in the map
     * @param <V>  the type of value in the map
     * @return a seq of items in coll
     * @see #toMap
     */
    static <K, V> ISeq<Map.Entry<K, V>> seq(Map<K, V> coll) {
        if (coll != null) {
            return seq(coll.entrySet().iterator());
        }
        return Util.nil();
    }

    /**
     * Returns a lazy seq of {@code x}, {@code f(x)}, {@code f(f(x))} etc. {@code f} must be free of side-effects.
     *
     * @param x   initial value
     * @param f   function to apply to x
     * @param <T> the type of items in the seq
     * @return a lazy seq of x, the result of applying f to x, the result of applying f to that, etc.
     */
    static <T> ISeq<T> iterate(T x, UnaryOperator<T> f) {
        return Util.lazySeq(() -> Util.cons(x, iterate(f.apply(x), f)));
    }

    /**
     * Returns a lazy seq of numbers from 0 (inclusive) to infinity, by step 1.
     *
     * @return a lazy seq of numbers from 0 (inclusive) to infinity, by step 1
     */
    static ISeq<Integer> range() {
        return ISeq.iterate(0, x -> x + 1);
    }

    /**
     * Returns a lazy seq of numbers from 0 (inclusive) to {@code end} (exclusive), by step 1.
     * Returns empty seq when {@code end} is equal to 0.
     *
     * @param end exclusive end of the range
     * @return a lazy seq of numbers from 0 (inclusive) to end (exclusive)
     */
    static ISeq<Integer> range(int end) {
        return ISeq.range(0, end);
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
        return ISeq.range(start, end, 1);
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
        return ISeq.iterate(start, x -> x + step).takeWhile(x -> step >= 0 ? (x < end) : (x > end));
    }

    /**
     * Returns a lazy (infinite!) seq of {@code x}s.
     *
     * @param x   the item to repeat
     * @param <T> the type of x
     * @return a lazy (infinite!) seq of xs
     */
    static <T> ISeq<T> repeat(T x) {
        return ISeq.iterate(x, i -> x);
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
        return ISeq.iterate(x, i -> x).take(n);
    }

    /**
     * Returns a lazy seq representing the concatenation of the items in {@code coll} and {@code x}.
     *
     * @param coll a collection to concat to
     * @param x    the item to append
     * @param <T>  the type of the items in the returned seq
     * @return a lazy seq representing the concatenation of the items in coll and x
     */
    static <T> ISeq<T> concat(Iterable<T> coll, T x) {
        return concat(seq(coll), ISeq.of(x));
    }

    /**
     * Returns a lazy seq representing the concatenation of the items in {@code coll} and {@code x}.
     *
     * @param coll the CharSequence to concat to
     * @param x    the character to append
     * @return a lazy seq representing the concatenation of the items in coll and x
     */
    static ISeq<Character> concat(CharSequence coll, Character x) {
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
    static <T> ISeq<T> concat(Iterable<T>... colls) {
        return seq(colls).mapcat(x -> x);
    }

    /**
     * Returns a lazy seq representing the concatenation of the items in the supplied {@code colls}.
     *
     * @param colls the strings to concatenate into a seq
     * @return a lazy seq representing the concatenation of the items in the supplied colls
     */
    static ISeq<Character> concat(CharSequence... colls) {
        var sb = new StringBuilder();
        for (var coll : colls) {
            sb.append(coll);
        }
        return seq(sb.toString());
    }

    // Members

    /**
     * Returns the first item in this seq or null if it is empty.
     *
     * @return the first item in this seq
     * @see #findFirst
     */
    T first();

    /**
     * Returns the second item in this seq or null if it has less than two items.
     *
     * @return the second item in this seq
     */
    T second();

    /**
     * Returns a seq of the items in this seq after the first.
     *
     * @return a seq of the items in this seq after the first
     */
    ISeq<T> rest();

    /**
     * Returns true if a value has been produced for this seq.
     *
     * @return true if a value has been produced for this seq
     */
    boolean isRealized();

    /**
     * Returns a lazy seq with the first {@code n} items in this seq.
     *
     * @param n number of items to return
     * @return a seq with the first n items of this seq
     */
    ISeq<T> take(long n);

    /**
     * Returns a lazy seq with all but the first {@code n} items in this seq.
     *
     * @param n number of items to drop
     * @return a seq with all but the first n items in this seq
     */
    ISeq<T> drop(long n);

    /**
     * Returns a lazy seq of the items in this seq for which {@code pred} returns true.
     *
     * @param pred predicate to test items with
     * @return a seq of the items in this seq for which pred returns true
     */
    ISeq<T> filter(Predicate<? super T> pred);

    /**
     * Returns a lazy seq of the result of applying {@code f} to each item in this seq.
     *
     * @param f   mapping function to apply to items
     * @param <R> the type of items in the returned seq
     * @return a seq of the result of applying f to each item in this seq
     */
    <R> ISeq<R> map(Function<? super T, ? extends R> f);

    /**
     * Returns a lazy seq of the result of applying {@code f} to the set of first items in both this seq and {@code coll},
     * followed by applying {@code f} to the set of second items in this seq and {@code coll}, until any one of the
     * collections is exhausted. Any remaining items in either collection are ignored.
     * <p>
     * <i>This is similar to zipping two collections in C# or Kotlin.</i>
     *
     * @param coll the collection to map this seq with
     * @param f    a function that accepts two parameters to map the corresponding items in both collections
     * @param <S>  the type of items in coll
     * @param <R>  the type of items in this seq
     * @return a seq of the result of applying f to the set of n items in both this seq and coll
     */
    <S, R> ISeq<R> map(Iterable<? extends S> coll, BiFunction<? super T, ? super S, ? extends R> f);

    /**
     * Returns a lazy seq of the result of applying {@code f} to the set of first items in both this seq and {@code coll},
     * followed by applying {@code f} to the set of second items in this seq and {@code coll}, until any one of the
     * collections is exhausted. Any remaining items in either collection are ignored.
     * <p>
     * <i>This is similar to zipping two collections in C# or Kotlin.</i>
     *
     * @param coll the iterator to map this seq with
     * @param f    a function that accepts two parameters to map the corresponding items in both collections
     * @param <S>  the type of items in coll
     * @param <R>  the type of items in this seq
     * @return a seq of the result of applying f to the set of n items in both this seq and coll
     */
    <S, R> ISeq<R> map(Iterator<? extends S> coll, BiFunction<? super T, ? super S, ? extends R> f);

    /**
     * Returns a lazy seq of the result of applying {@code f} to the set of first items in both this seq and {@code coll},
     * followed by applying {@code f} to the set of second items in this seq and {@code coll}, until any one of the
     * collections is exhausted. Any remaining items in either collection are ignored.
     * <p>
     * <i>This is similar to zipping two collections in C# or Kotlin.</i>
     *
     * @param coll the stream to map this seq with
     * @param f    a function that accepts two parameters to map the corresponding items in both collections
     * @param <S>  the type of items in coll
     * @param <R>  the type of items in this seq
     * @return a seq of the result of applying f to the set of n items in both this seq and coll
     */
    <S, R> ISeq<R> map(Stream<? extends S> coll, BiFunction<? super T, ? super S, ? extends R> f);

    /**
     * Returns a lazy seq of the result of applying {@code f} to the set of first items in both this seq and {@code coll},
     * followed by applying {@code f} to the set of second items in this seq and {@code coll}, until any one of the
     * collections is exhausted. Any remaining items in either collection are ignored.
     * <p>
     * <i>This is similar to zipping two collections in C# or Kotlin.</i>
     *
     * @param coll the array to map this seq with
     * @param f    a function that accepts two parameters to map the corresponding items in both collections
     * @param <S>  the type of items in coll
     * @param <R>  the type of items in this seq
     * @return a seq of the result of applying f to the set of n items in both this seq and coll
     */
    <S, R> ISeq<R> map(S[] coll, BiFunction<? super T, ? super S, ? extends R> f);

    /**
     * Returns a lazy seq of the result of applying {@code f} to the set of first items in both this seq and {@code coll},
     * followed by applying {@code f} to the set of second items in this seq and {@code coll}, until any one of the
     * collections is exhausted. Any remaining items in either collection are ignored.
     * <p>
     * <i>This is similar to zipping two collections in C# or Kotlin.</i>
     *
     * @param coll the character sequence to map this seq with
     * @param f    a function that accepts two parameters to map the corresponding items in both collections
     * @param <R>  the type of items in this seq
     * @return a seq of the result of applying f to the set of n items in both this seq and coll
     */
    <R> ISeq<R> map(CharSequence coll, BiFunction<? super T, ? super Character, ? extends R> f);

    /**
     * Returns a lazy seq of the result of applying {@link #concat} to the result of applying {@link #map} to {@code f} and
     * the items in this seq. Function {@code f} should return a collection.
     * <p>
     * <i>This is similar to {@link java.util.stream.Stream#flatMap}.</i>
     *
     * @param f   the mapping function to apply to each item
     * @param <R> the type of items in the returned seq
     * @return a seq of the result of applying concat to the result of applying map to f and the items in this seq
     */
    <R> ISeq<R> mapcat(Function<? super T, ? extends Iterable<? extends R>> f);

    /**
     * Returns a lazy seq of the result of applying {@link #concat} to the result of applying {@link #map} to {@code f} and
     * {@code coll} and the items in this seq. Function {@code f} should return a collection.
     *
     * @param coll the collection to mapcat this seq with
     * @param f    the mapping function to apply to each pair of items
     * @param <S>  the type of items in coll
     * @param <R>  the type of items in the returned seq
     * @return a seq of the result of applying concat to the result of applying map to f and coll and the items in this seq
     */
    <S, R> ISeq<R> mapcat(Iterable<? extends S> coll, BiFunction<? super T, ? super S, Iterable<? extends R>> f);

    /**
     * Returns a lazy seq of the result of applying {@link #concat} to the result of applying {@link #map} to {@code f} and
     * {@code coll} and the items in this seq. Function {@code f} should return a collection.
     *
     * @param coll the iterator to mapcat this seq with
     * @param f    the mapping function to apply to each pair of items
     * @param <S>  the type of items in coll
     * @param <R>  the type of items in the returned seq
     * @return a seq of the result of applying concat to the result of applying map to f and coll and the items in this seq
     */
    <S, R> ISeq<R> mapcat(Iterator<? extends S> coll, BiFunction<? super T, ? super S, Iterable<? extends R>> f);

    /**
     * Returns a lazy seq of the result of applying {@link #concat} to the result of applying {@link #map} to {@code f} and
     * {@code coll} and the items in this seq. Function {@code f} should return a collection.
     *
     * @param coll the stream to mapcat this seq with
     * @param f    the mapping function to apply to each pair of items
     * @param <S>  the type of items in coll
     * @param <R>  the type of items in the returned seq
     * @return a seq of the result of applying concat to the result of applying map to f and coll and the items in this seq
     */
    <S, R> ISeq<R> mapcat(Stream<? extends S> coll, BiFunction<? super T, ? super S, Iterable<? extends R>> f);

    /**
     * Returns a lazy seq of the result of applying {@link #concat} to the result of applying {@link #map} to {@code f} and
     * {@code coll} and the items in this seq. Function {@code f} should return a collection.
     *
     * @param coll the array to mapcat this seq with
     * @param f    the mapping function to apply to each pair of items
     * @param <S>  the type of items in coll
     * @param <R>  the type of items in the returned seq
     * @return a seq of the result of applying concat to the result of applying map to f and coll and the items in this seq
     */
    <S, R> ISeq<R> mapcat(S[] coll, BiFunction<? super T, ? super S, Iterable<? extends R>> f);

    /**
     * Returns a lazy seq of the result of applying {@link #concat} to the result of applying {@link #map} to {@code f} and
     * {@code coll} and the items in this seq. Function {@code f} should return a collection.
     *
     * @param coll the character sequence to mapcat this seq with
     * @param f    the mapping function to apply to each pair of items
     * @param <R>  the type of items in the returned seq
     * @return a seq of the result of applying concat to the result of applying map to f and coll and the items in this seq
     */
    <R> ISeq<R> mapcat(CharSequence coll, BiFunction<? super T, ? super Character, Iterable<? extends R>> f);

    /**
     * Returns a lazy seq of successive items from this seq while {@code pred} returns true.
     *
     * @param pred predicate to test items with
     * @return a seq of successive items from this seq while pred returns true
     */
    ISeq<T> takeWhile(Predicate<? super T> pred);

    /**
     * Returns a lazy seq of the items in this seq starting from the first item for which {@code pred} returns false.
     *
     * @param pred predicate to test items with
     * @return a seq of the items in this seq starting from the first item for which pred returns false
     */
    ISeq<T> dropWhile(Predicate<? super T> pred);

    /**
     * Returns a lazy seq of seqs of {@code n} items each.
     *
     * @param n the number of items per partition
     * @return a seq of seqs of n items each
     */
    ISeq<ISeq<T>> partition(int n);

    /**
     * Returns a lazy seq of seqs of {@code n} items each, at offsets {@code step} apart.
     *
     * @param n    the number of items per partition
     * @param step the offset of each partition
     * @return a seq of seqs of n items each, at offsets step apart
     */
    ISeq<ISeq<T>> partition(int n, int step);

    /**
     * Returns a lazy seq of seqs of {@code n} items each, at offsets {@code step} apart. The items in {@code pad} are
     * used as necessary to complete the last partition up to {@code n} items. In case there are not enough padding
     * items, returns a partition with less than {@code n} items.
     *
     * @param n    the number of items per partition
     * @param step the offset of each partition
     * @param pad  a list of items to pad a potentially incomplete last partition
     * @return a seq of seqs of n items each, at offsets step apart and the last partition padded with pad
     */
    ISeq<ISeq<T>> partition(int n, int step, Iterable<T> pad);

    /**
     * Returns a lazy seq of seqs of {@code n} items each, like {@link #partition}, but may include partitions with
     * fewer than {@code n} items at the end.
     *
     * @param n the number of items per partition
     * @return a seq of seqs of n items each
     */
    ISeq<ISeq<T>> partitionAll(int n);

    /**
     * Returns a lazy seq of seqs of {@code n} items each, at offsets {@code step} apart, like {@link #partition}, but
     * may include partitions with fewer than {@code n} items at the end.
     *
     * @param n    the number of items per partition
     * @param step the offset of each partition
     * @return a seq of seqs of n items each, at offsets step apart
     */
    ISeq<ISeq<T>> partitionAll(int n, int step);

    /**
     * Returns a lazy seq of the intermediate values of the reduction (as per {@link #reduce}) of this seq by {@code f}.
     *
     * @param f the function to reduce by
     * @return a seq of the intermediate values of the reduction of this seq by f
     */
    ISeq<T> reductions(BinaryOperator<T> f);

    /**
     * Returns a lazy seq of the intermediate values of the reduction (as per {@link #reduce}) of this seq by {@code f},
     * starting with {@code init}.
     *
     * @param init the initial value for the reduction
     * @param f    the function to reduce by
     * @param <U>  the type of the items in the resulting seq
     * @return a seq of the intermediate values of the reduction of this seq by f, starting with init
     */
    <U> ISeq<U> reductions(U init, BiFunction<U, ? super T, U> f);

    /**
     * Returns a new seq where {@code x} is the first item and this seq is the rest.
     *
     * @param x the item to be prepended to this seq
     * @return a new seq where x is the first item and this seq is the rest
     */
    ISeq<T> cons(T x);

    /**
     * Returns a new seq representing the concatenation of the items in this seq and {@code xs}.
     *
     * @param xs one or more items to be concatenated to this seq
     * @return a new seq representing the concatenation of the items in this seq and xs
     */
    ISeq<T> concat(T... xs);

    /**
     * Returns a new seq representing the concatenation of the items in this seq and {@code coll}.
     *
     * @param coll the collection to be concatenated to this seq
     * @return a new seq representing the concatenation of the items in this seq and coll
     */
    ISeq<T> concat(Iterable<T> coll);

    /**
     * Returns a new seq representing the concatenation of the items in this seq and {@code coll}.
     *
     * @param coll the collection to be concatenated to this seq
     * @return a new seq representing the concatenation of the items in this seq and coll
     */
    ISeq<T> concat(Iterator<T> coll);

    /**
     * Returns a new seq representing the concatenation of the items in this seq and {@code coll}.
     *
     * @param coll the collection to be concatenated to this seq
     * @return a new seq representing the concatenation of the items in this seq and coll
     */
    ISeq<T> concat(Stream<T> coll);

    /**
     * Returns an {@code Optional} of the result of applying {@code f} to the first 2 items in this seq, then applying
     * {@code f} to that result and the 3rd item, etc. If this seq has only 1 item, it is returned and {@code f} is not
     * called.
     * Returns an empty {@code Optional} if this seq contains no items.
     * <p>
     * <b>Caution:</b> The seq will be fully realized. If this seq is infinite, it will run infinitely or until system
     * resources are exhausted.
     *
     * @param f a function taking two arguments to reduce the seq
     * @return the result of the reduction or empty
     */
    Optional<T> reduce(BinaryOperator<T> f);

    /**
     * Returns the result of applying {@code f} to {@code val} and the first item in this seq, then applying {@code f}
     * to that result and the 2nd item, etc.
     * Returns {@code val} if this seq is empty. {@code f} is not called.
     * <p>
     * <b>Caution:</b> The seq will be fully realized. If this seq is infinite, it will run infinitely or until system
     * resources are exhausted.
     *
     * @param val the initial value for the reduction
     * @param f   a function taking two arguments to reduce the seq
     * @param <U> the type of the result
     * @return the result of the reduction or val
     */
    <U> U reduce(U val, BiFunction<U, ? super T, U> f);

    /**
     * Calls {@code proc} for each item in this seq for purposes of side effects, {@code proc} being a consumer function
     * taking one argument and returning void.
     * <p>
     * <b>Caution:</b> The seq will be fully realized. If this seq is infinite, it will run infinitely or until system
     * resources are exhausted.
     *
     * @param proc procedure
     */
    void run(Consumer<? super T> proc);

    /**
     * Returns a seq of the items of this seq with duplicates removed.
     * <p>
     * <b>Caution:</b> The seq will be fully realized. If this seq is infinite, it will run infinitely or until system
     * resources are exhausted.
     *
     * @return a seq of the items of this seq with duplicates removed
     */
    ISeq<T> distinct();

    /**
     * Returns a seq of the items of this seq sorted by using compare.
     * <p>
     * <b>Caution:</b> The seq will be fully realized. If this seq is infinite, it will run infinitely or until system
     * resources are exhausted.
     *
     * @return a seq of the items of this seq sorted by using compare
     */
    ISeq<T> sorted();

    /**
     * Returns a seq of the items of this seq sorted by using supplied comparator {@code comp}.
     * <p>
     * <b>Caution:</b> The seq will be fully realized. If this seq is infinite, it will run infinitely or until system
     * resources are exhausted.
     *
     * @param comp the comparator
     * @return a seq of the items of this seq sorted by using supplied comparator
     */
    ISeq<T> sorted(Comparator<? super T> comp);

    /**
     * Returns a seq of the items of this seq in reversed order.
     * <p>
     * <b>Caution:</b> The seq will be fully realized. If this seq is infinite, it will run infinitely or until system
     * resources are exhausted.
     *
     * @return a seq of the items of this seq in reversed order
     */
    ISeq<T> reverse();

    /**
     * Returns the first true value of {@code pred} for any item in this seq, or false if none of the items in this seq
     * return true for {@code pred}.
     * <p>
     * <b>Caution:</b> The seq may be fully realized. If this seq is infinite, it will run infinitely or until system
     * resources are exhausted.
     * <p>
     * <i>This is similar to {@link java.util.stream.Stream#anyMatch}.</i>
     *
     * @param pred the predicate to test items against
     * @return the first true value of pred for any item in this seq, or false
     */
    boolean some(Predicate<? super T> pred);

    /**
     * Returns the first false value of {@code pred} for any item in this seq or true if all the items in this seq
     * return true for {@code pred}.
     * <p>
     * <b>Caution:</b> The seq may be fully realized. If this seq is infinite, it will run infinitely or until system
     * resources are exhausted.
     * <p>
     * <i>This is similar to {@link java.util.stream.Stream#allMatch}.</i>
     *
     * @param pred the predicate to test items against
     * @return the first false value of pred for any item in this seq, or true
     */
    boolean every(Predicate<? super T> pred);

    /**
     * Returns false if {@code pred} is true for any item in this seq or true if none of the items in this seq return
     * true for {@code pred}.
     * <p>
     * <b>Caution:</b> The seq may be fully realized. If this seq is infinite, it will run infinitely or until system
     * resources are exhausted.
     * <p>
     * <i>This is similar to {@link java.util.stream.Stream#noneMatch}.</i>
     *
     * @param pred the predicate to test items against
     * @return false if pred is true for any item in this seq or true
     */
    boolean notAny(Predicate<? super T> pred);

    /**
     * Returns the item in this seq for which {@code comp} determines is greatest. If there are multiple such items,
     * the last one is returned.
     * <p>
     * <b>Caution:</b> The seq will be fully realized. If this seq is infinite, it will run infinitely or until system
     * resources are exhausted.
     *
     * @param comp the comparator
     * @return the item in this seq for which comp determines is greatest
     */
    Optional<T> max(Comparator<? super T> comp);

    /**
     * Returns the item in this seq for which {@code comp} determines is least. If there are multiple such items,
     * the last one is returned.
     * <p>
     * <b>Caution:</b> The seq will be fully realized. If this seq is infinite, it will run infinitely or until system
     * resources are exhausted.
     *
     * @param comp the comparator
     * @return the item in this seq for which comp determines is least
     */
    Optional<T> min(Comparator<? super T> comp);

    /**
     * Returns the item in this seq for which {@code f}, a number, is greatest. If there are multiple such items, the
     * last one is returned.
     * <p>
     * <b>Caution:</b> The seq will be fully realized. If this seq is infinite, it will run infinitely or until system
     * resources are exhausted.
     *
     * @param f   a function returning a number for comparison
     * @param <C> the type of the values to be compared
     * @return the item in this seq for which f, a number, is greatest
     */
    <C extends Comparable<? super C>> Optional<T> maxKey(Function<T, C> f);

    /**
     * Returns the item in this seq for which {@code f}, a number, is least. If there are multiple such items, the
     * last one is returned.
     * <p>
     * <b>Caution:</b> The seq will be fully realized. If this seq is infinite, it will run infinitely or until system
     * resources are exhausted.
     *
     * @param f   a function returning a number for comparison
     * @param <C> the type of the values to be compared
     * @return the item in this seq for which f, a number, is least
     */
    <C extends Comparable<? super C>> Optional<T> minKey(Function<T, C> f);

    /**
     * Returns the item at {@code index} in this seq or throws if {@code index} is out of bounds.
     * <p>
     * <b>Caution:</b> All items up to that index will be realized.
     *
     * @param index the index of the item to be returned
     * @return the item at index in this seq
     * @throws IndexOutOfBoundsException if index is out of bounds
     * @see #find
     */
    T nth(int index);

    /**
     * Returns the item at {@code index} in this seq or {@code notFound} if index is out of bounds.
     * <p>
     * <b>Caution:</b> All items up to {@code index} will be realized.
     *
     * @param index    the index of the item to be returned
     * @param notFound the default value to be returned if index is out of bounds
     * @return the item at index in this seq, or notFount
     * @see #find
     */
    T nth(int index, T notFound);

    /**
     * Returns the concatenation of {@code x.toString()} of all items x in this seq, or an empty string if the seq is
     * empty.
     * <p>
     * <b>Caution:</b> The seq will be fully realized. If this seq is infinite, it will run infinitely or until system
     * resources are exhausted.
     *
     * @return the concatenation of the string-representation of all items in this seq
     */
    String str();

    /**
     * Returns the number of items in this seq.
     * <p>
     * <b>Caution:</b> The seq will be fully realized. If this seq is infinite, it will run infinitely or until system
     * resources are exhausted.
     *
     * @return the number of items in this seq
     */
    int count();

    /**
     * Returns an {@code Optional} of the item at {@code index} in this seq, or an empty {@code Optional} if this seq
     * does not contain any item at {@code index}.
     * <p>
     * <b>Caution:</b> All items up to {@code index} will be realized.
     *
     * @param index the index of the item to be returned
     * @return the item at index in this seq, or an empty Optional
     * @see #nth
     */
    Optional<T> find(int index);

    /**
     * Returns an {@code Optional} of the first item in this seq, or an empty {@code Optional} if this seq contains
     * no items.
     *
     * @return the first item, or an empty Optional
     * @see #first
     */
    Optional<T> findFirst();

    /**
     * Returns an {@code Optional} of the first item in this seq for which {@code pred} returns true, or an empty
     * {@code Optional} if {@code pred} returns false for all items in this seq.
     * <p>
     * <b>Caution:</b> The seq may be fully realized. If this seq is infinite, it may run infinitely or until system
     * resources are exhausted.
     *
     * @param pred predicate to test items with
     * @return the first item in this seq for which pred returns true, or an empty Optional
     */
    Optional<T> findFirst(Predicate<? super T> pred);

    /**
     * Returns a new {@code Map} with the keys and values of the items in this seq of {@link java.util.Map.Entry}s.
     * Keeps last value on key-collision. If the seq is of any other type than {@code Map.Entry}, an exception is thrown.
     * <p>
     * <b>Caution:</b> The seq will be fully realized. If this seq is infinite, it will run infinitely or until system
     * resources are exhausted.
     *
     * @param <K> the type of the keys
     * @param <V> the type of the values
     * @return a new map with the keys and values of the items in this seq
     * @throws UnsupportedOperationException if the seq is not of type {@code java.util.Map.Entry}
     * @see #seq(Map)
     */
    <K, V> Map<K, V> toMap();

    /**
     * Returns a new Map with keys as a result of {@code k} applied to x and values as a result of {@code v} applied
     * to x of all xs in this seq. Throws on key-collision.
     * <p>
     * <b>Caution:</b> The seq will be fully realized. If this seq is infinite, it will run infinitely or until system
     * resources are exhausted.
     *
     * @param k   the function to generate they key
     * @param v   the function to generate the value
     * @param <K> the type of they keys
     * @param <V> the type of the values
     * @return a new Map with entries generated by supplied functions k and v
     * @throws IllegalArgumentException if there are any duplicate keys
     * @throws NullPointerException     if any key or value is {@code null}
     */
    <K, V> Map<K, V> toMap(Function<T, K> k, Function<T, V> v);

    /**
     * Returns a new Map with keys as a result of {@code k} applied to x and values as a result of {@code v} applied
     * to x of all xs in this seq, using {@code m} applied to values for duplicate keys to resolve key-collision.
     * <p>
     * <b>Caution:</b> The seq will be fully realized. If this seq is infinite, it will run infinitely or until system
     * resources are exhausted.
     *
     * @param k   the function to generate they key
     * @param v   the function to generate the value
     * @param m   the function to merge values for duplicate keys
     * @param <K> the type of they keys
     * @param <V> the type of the values
     * @return a new Map with entries generated by supplied functions k and v
     * @throws NullPointerException if any key or value is {@code null}
     */
    <K, V> Map<K, V> toMap(Function<T, K> k, Function<T, V> v, BinaryOperator<V> m);

    /**
     * Returns an immutable {@code java.util.List} with all items in this seq. Throws a {@code java.lang.NullPointerException}
     * if any nulls are in this seq.
     * <p>
     * <b>Caution:</b> The seq will be fully realized. If this seq is infinite, it will run infinitely or until system
     * resources are exhausted.
     *
     * @return a List with all items in this seq
     */
    List<T> reify();

    /**
     * Returns an immutable {@code java.util.Map} from distinct items in this seq to the number of times they appear.
     * <p>
     * <b>Caution:</b> The seq will be fully realized. If this seq is infinite, it will run infinitely or until system
     * resources are exhausted.
     *
     * @return a Map from distinct items in this seq to the number of times they appear
     */
    Map<T, Integer> frequencies();
}
