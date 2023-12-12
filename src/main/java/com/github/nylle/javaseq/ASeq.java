package com.github.nylle.javaseq;

import java.util.AbstractList;
import java.util.Comparator;
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
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public abstract class ASeq<T> extends AbstractList<T> implements ISeq<T> {

    public ISeq<T> filter(Predicate<? super T> pred) {
        return Fn.filter(pred, this);
    }

    public <R> ISeq<R> map(Function<? super T, ? extends R> f) {
        return Fn.map(f, this);
    }

    public <S, R> ISeq<R> map(ISeq<? extends S> coll, BiFunction<? super T, ? super S, ? extends R> f) {
        return Fn.map(f, this, coll);
    }

    public <S, R> ISeq<R> map(Iterable<? extends S> coll, BiFunction<? super T, ? super S, ? extends R> f) {
        return map(Fn.seq(coll), f);
    }

    public <S, R> ISeq<R> map(Iterator<? extends S> coll, BiFunction<? super T, ? super S, ? extends R> f) {
        return map(Fn.seq(coll), f);
    }

    public <S, R> ISeq<R> map(Stream<? extends S> coll, BiFunction<? super T, ? super S, ? extends R> f) {
        return map(Fn.seq(coll), f);
    }

    public <S, R> ISeq<R> map(S[] coll, BiFunction<? super T, ? super S, ? extends R> f) {
        return map(Fn.seq(coll), f);
    }

    public <R> ISeq<R> map(CharSequence coll, BiFunction<? super T, ? super Character, ? extends R> f) {
        return map(Fn.seq(coll), f);
    }

    public <R> ISeq<R> mapcat(Function<? super T, ? extends Iterable<? extends R>> f) {
        return Fn.mapcat(f, this);
    }

    public <S, R> ISeq<R> mapcat(ISeq<? extends S> coll, BiFunction<? super T, ? super S, Iterable<? extends R>> f) {
        return Fn.mapcat(f, this, coll);
    }

    public <S, R> ISeq<R> mapcat(Iterable<? extends S> coll, BiFunction<? super T, ? super S, Iterable<? extends R>> f) {
        return Fn.mapcat(f, this, Fn.seq(coll));
    }

    public <S, R> ISeq<R> mapcat(Iterator<? extends S> coll, BiFunction<? super T, ? super S, Iterable<? extends R>> f) {
        return Fn.mapcat(f, this, Fn.seq(coll));
    }

    public <S, R> ISeq<R> mapcat(Stream<? extends S> coll, BiFunction<? super T, ? super S, Iterable<? extends R>> f) {
        return Fn.mapcat(f, this, Fn.seq(coll));
    }

    public <S, R> ISeq<R> mapcat(S[] coll, BiFunction<? super T, ? super S, Iterable<? extends R>> f) {
        return Fn.mapcat(f, this, Fn.seq(coll));
    }

    public <R> ISeq<R> mapcat(CharSequence coll, BiFunction<? super T, ? super Character, Iterable<? extends R>> f) {
        return Fn.mapcat(f, this, Fn.seq(coll));
    }

    public ISeq<T> take(long n) {
        return Fn.take(n, this);
    }

    public ISeq<T> drop(long n) {
        return Fn.drop(n, this);
    }

    public ISeq<T> takeWhile(Predicate<? super T> pred) {
        return Fn.takeWhile(pred, this);
    }

    public ISeq<T> dropWhile(Predicate<? super T> pred) {
        return Fn.dropWhile(pred, this);
    }

    public ISeq<List<T>> partition(int n) {
        return Fn.partition(n, this);
    }

    public ISeq<List<T>> partition(int n, int step) {
        return Fn.partition(n, step, this);
    }

    public ISeq<List<T>> partition(int n, int step, Iterable<T> pad) {
        return Fn.partition(n, step, pad, this);
    }

    public ISeq<List<T>> partitionAll(int n) {
        return Fn.partitionAll(n, this);
    }

    public ISeq<List<T>> partitionAll(int n, int step) {
        return Fn.partitionAll(n, step, this);
    }

    public ISeq<T> reductions(BinaryOperator<T> f) {
        return Fn.reductions(f, this);
    }

    public <U> ISeq<U> reductions(U init, BiFunction<U, ? super T, U> f) {
        return Fn.reductions(init, f, this);
    }

    public ISeq<T> cons(T x) {
        return Fn.cons(x, this);
    }

    public Optional<T> reduce(BinaryOperator<T> f) {
        return Fn.reduce(f, this);
    }

    public <U> U reduce(U val, BiFunction<U, ? super T, U> f) {
        return Fn.reduce(val, f, this);
    }

    public void run(Consumer<? super T> proc) {
        Fn.run(proc, this);
    }

    public ISeq<T> distinct() {
        return Fn.distinct(this);
    }

    @SuppressWarnings("unchecked")
    public ISeq<T> sorted() {
        return Fn.sort(this);
    }

    public ISeq<T> sorted(Comparator<? super T> comp) {
        return Fn.sort(comp, this);
    }

    public boolean some(Predicate<? super T> pred) {
        return Fn.some(pred, this);
    }

    public boolean every(Predicate<? super T> pred) {
        return Fn.every(pred, this);
    }

    public boolean notAny(Predicate<? super T> pred) {
        return Fn.notAny(pred, this);
    }

    public Optional<T> max(Comparator<? super T> comp) {
        return Fn.max(comp, this);
    }

    public Optional<T> min(Comparator<? super T> comp) {
        return Fn.min(comp, this);
    }

    public <C extends Comparable<? super C>> Optional<T> maxKey(Function<T, C> f) {
        return Fn.maxKey(f, this);
    }

    public <C extends Comparable<? super C>> Optional<T> minKey(Function<T, C> f) {
        return Fn.minKey(f, this);
    }

    public T nth(int index) {
        return Fn.nth(this, index);
    }

    public T nth(int index, T notFound) {
        return Fn.nth(this, index, notFound);
    }

    public Optional<T> find(int i) {
        return Optional.ofNullable(nth(i, null));
    }

    public Optional<T> findFirst() {
        return Optional.ofNullable(first());
    }

    public Optional<T> findFirst(Predicate<? super T> pred) {
        return filter(pred).findFirst();
    }

    public <K, V> Map<K, V> toMap(Function<T, K> k, Function<T, V> v) {
        return Map.ofEntries(map(x -> Map.entry(k.apply(x), v.apply(x))).toArray(new Map.Entry[0]));
    }

    public <K, V> Map<K, V> toMap(Function<T, K> k, Function<T, V> v, BinaryOperator<V> m) {
        var entries = this
                .map(x -> Map.entry(k.apply(x), v.apply(x)))
                .reduce(
                        ISeq.<Map.Entry<K, V>>of(),
                        (acc, x) -> acc
                                .filter(y -> !x.getKey().equals(y.getKey()))
                                .cons(acc.findFirst(y -> y.getKey().equals(x.getKey())).map(y -> Map.entry(x.getKey(), m.apply(y.getValue(), x.getValue()))).orElse(x)));

        return Map.ofEntries(entries.toArray(new Map.Entry[0]));
    }

    public <K, V> Map<K, V> toMap() {
        if (isEmpty()) return Map.of();
        if (first() instanceof Map.Entry<?, ?>) {
            return toMap(k -> ((Map.Entry<K, V>) k).getKey(), v -> ((Map.Entry<K, V>) v).getValue(), (a, b) -> b);
        }
        throw new UnsupportedOperationException("ISeq is not of type Map.Entry. Provide key- and value-mappers");
    }

    public List<T> toList() {
        return List.copyOf(this.realize());
    }

    public Set<T> toSet() {
        return Set.copyOf(this.realize());
    }

    public ISeq<T> realize() {
        rest().realize();
        return this;
    }

    // Iterable

    @Override
    public void forEach(Consumer<? super T> action) {
        run(action);
    }

    // List

    @Override
    public T get(final int index) {
        return nth(index);
    }

    @Override
    public int size() {
        return 1 + rest().size();
    }

    @Override
    public Iterator<T> iterator() {
        return new SeqIterator<>(this);
    }

    // Collection

    @Override
    public Stream<T> stream() {
        return StreamSupport.stream(((Iterable<T>) () -> iterator()).spliterator(), false);
    }

    @Override
    public Stream<T> parallelStream() {
        return stream();
    }

    // Object

    @Override
    public String toString() {
        var result = new StringBuilder("[");
        ISeq<T> seq = this;
        while (!seq.isEmpty()) {
            result.append(seq.first());
            if (!seq.rest().isEmpty()) {
                result.append(", ");
            }
            seq = seq.rest();
        }
        return result.append("]").toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ISeq)) return false;

        var other = (ISeq) o;
        return isEmpty() && other.isEmpty() || !other.isEmpty() &&
                first().equals(other.first()) &&
                rest().equals(other.rest());
    }

    @Override
    public int hashCode() {
        return isEmpty() ? 0 : first().hashCode() + rest().hashCode() * 31;
    }
}


