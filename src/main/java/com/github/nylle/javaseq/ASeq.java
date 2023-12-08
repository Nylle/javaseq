package com.github.nylle.javaseq;

import com.github.nylle.javaseq.prototype.Seq;

import java.util.AbstractList;
import java.util.ArrayList;
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
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public abstract class ASeq<T> extends AbstractList<T> implements ISeq<T> {

    public ISeq<T> take(long n) {
        if(n <= 0) {
            return ISeq.of();
        }
        if (n == 1) {
            return ISeq.of(first());
        }
        return rest().take(n - 1).cons(first());
    }

    public ISeq<T> drop(long n) {
        return n > 0 ? rest().drop(n - 1) : this;
    }

    public <S, R> ISeq<R> map(Iterable<? extends S> coll, BiFunction<? super T, ? super S, ? extends R> f) {
        return map(ISeq.sequence(coll), f);
    }

    public <S, R> ISeq<R> map(Iterator<? extends S> coll, BiFunction<? super T, ? super S, ? extends R> f) {
        return map(ISeq.sequence(coll), f);
    }

    public <S, R> ISeq<R> map(Stream<? extends S> coll, BiFunction<? super T, ? super S, ? extends R> f) {
        return map(ISeq.sequence(coll), f);
    }

    public <S, R> ISeq<R> map(S[] coll, BiFunction<? super T, ? super S, ? extends R> f) {
        return map(ISeq.sequence(coll), f);
    }

    public <R> ISeq<R> map(CharSequence coll, BiFunction<? super T, ? super Character, ? extends R> f) {
        return map(ISeq.sequence(coll), f);
    }

    public <S, R> ISeq<R> mapcat(Iterable<? extends S> coll, BiFunction<? super T, ? super S, Iterable<? extends R>> f) {
        return mapcat(ISeq.sequence(coll), f);
    }

    public <S, R> ISeq<R> mapcat(Iterator<? extends S> coll, BiFunction<? super T, ? super S, Iterable<? extends R>> f) {
        return mapcat(ISeq.sequence(coll), f);
    }

    public <S, R> ISeq<R> mapcat(Stream<? extends S> coll, BiFunction<? super T, ? super S, Iterable<? extends R>> f) {
        return mapcat(ISeq.sequence(coll), f);
    }

    public <S, R> ISeq<R> mapcat(S[] coll, BiFunction<? super T, ? super S, Iterable<? extends R>> f) {
        return mapcat(ISeq.sequence(coll), f);
    }

    public <R> ISeq<R> mapcat(CharSequence coll, BiFunction<? super T, ? super Character, Iterable<? extends R>> f) {
        return mapcat(ISeq.sequence(coll), f);
    }

    public ISeq<T> takeWhile(Predicate<? super T> pred) {
        if (pred.test(first())) {
            return ISeq.lazySeq(first(), () -> rest().takeWhile(pred));
        } else {
            return ISeq.of();
        }
    }

    public ISeq<T> dropWhile(Predicate<? super T> pred) {
        if (pred.test(first())) {
            return rest().dropWhile(pred);
        } else {
            return this;
        }
    }

    public ISeq<List<T>> partition(int n) {
        return partition(n, n);
    }

    public ISeq<List<T>> partition(int n, int step) {
        return partition(n, step, null);
    }

    public ISeq<List<T>> partition(int n, int step, Iterable<T> pad) {
        if (n < 0) {
            return ISeq.of();
        }
        var partition = take(n).toList();
        if (partition.size() < n) {
            return pad == null
                    ? ISeq.of()
                    : ISeq.lazySeq(ISeq.concat(partition, ISeq.sequence(pad).take(n - (long) partition.size())).toList(), () -> drop(step).partition(n, step, pad));
        }
        return ISeq.lazySeq(partition, () -> drop(step).partition(n, step, pad));
    }

    public ISeq<List<T>> partitionAll(int n) {
        return partition(n, n, List.of());
    }

    public ISeq<List<T>> partitionAll(int n, int step) {
        return partition(n, step, List.of());
    }

    public ISeq<T> reductions(BinaryOperator<T> f) {
        return rest().reductions(first(), f);
    }

    public <U> ISeq<U> reductions(U init, BiFunction<U, ? super T, U> f) {
        return ISeq.lazySeq(init, () -> rest().reductions(f.apply(init, first()), f));
    }

    public ISeq<T> cons(T x) {
        return ISeq.cons(x, this);
    }

    public Optional<T> reduce(BinaryOperator<T> f) {
        if (isEmpty() || rest().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(rest().reduce(first(), f));
    }

    public <U> U reduce(U val, BiFunction<U, ? super T, U> f) {
        var result = val;
        ISeq<T> seq = this;
        while (!seq.isEmpty()) {
            result = f.apply(result, seq.first());
            seq = seq.rest();
        }
        return result;
    }

    public void run(Consumer<? super T> proc) {
        proc.accept(first());
        rest().run(proc);
    }

    public ISeq<T> distinct() {
        return distinct(this, new HashSet<>());
    }

    @SuppressWarnings("unchecked")
    public ISeq<T> sorted() {
        return sorted((o1, o2) -> ((Comparable<T>) o1).compareTo(o2));
    }

    public ISeq<T> sorted(Comparator<? super T> comp) {
        var result = new ArrayList<>(this);
        result.sort(comp);
        return ISeq.sequence(result);
    }

    public boolean some(Predicate<? super T> pred) {
        return pred.test(first()) || rest().some(pred);
    }

    public boolean every(Predicate<? super T> pred) {
        return pred.test(first()) && rest().every(pred);
    }

    public boolean notAny(Predicate<? super T> pred) {
        return every(pred.negate());
    }

    public Optional<T> max(Comparator<? super T> comp) {
        if (rest().isEmpty()) {
            return Optional.of(first());
        }
        var result = first();
        var seq = this.rest();
        while (!seq.isEmpty()) {
            var next = seq.first();
            result = comp.compare(result, next) > 0 ? result : next;
            seq = seq.rest();
        }
        return Optional.of(result);
    }

    public Optional<T> min(Comparator<? super T> comp) {
        return max(comp.reversed());
    }

    public <C extends Comparable<? super C>> Optional<T> maxKey(Function<T, C> f) {
        return max(Comparator.comparing(t -> f.apply(t)));
    }

    public <C extends Comparable<? super C>> Optional<T> minKey(Function<T, C> f) {
        return min(Comparator.comparing(t -> f.apply(t)));
    }

    public T nth(int index) {
        var result = nth(index, null);
        if (result == null) {
            throw new IndexOutOfBoundsException(index);
        }
        return result;
    }

    public T nth(int index, T notFound) {
        if (index < 0) {
            return notFound;
        }
        ISeq<T> seq = this;
        for (int i = index; i > 0; --i) {
            if (seq.rest().isEmpty()) {
                return notFound;
            }
            seq = seq.rest();
        }
        return seq.first();
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
                        Seq.<Map.Entry<K, V>>of(),
                        (acc, x) -> acc
                                .filter(y -> !x.getKey().equals(y.getKey()))
                                .cons(acc.findFirst(y -> y.getKey().equals(x.getKey())).map(y -> Map.entry(x.getKey(), m.apply(y.getValue(), x.getValue()))).orElse(x)));

        return Map.ofEntries(entries.toArray(new Map.Entry[0]));
    }

    public <K, V> Map<K, V> toMap() {
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

    private static <T> ISeq<T> distinct(ISeq<T> seq, Set<T> exclude) {
        var result = seq.filter(x -> !exclude.contains(x));
        if (result.isEmpty()) {
            return ISeq.of();
        }
        var next = result.first();
        exclude.add(next);
        return ISeq.lazySeq(next, () -> distinct(result.rest(), exclude));
    }

    protected static <R> ArrayList<R> copy(Iterable<? extends R> res) {
        var result = new ArrayList<R>();
        res.forEach(x -> result.add(x));
        return result;
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
            if (seq.isRealized()) {
                if (!seq.rest().isEmpty()) {
                    result.append(", ");
                }
                seq = seq.rest();
            } else {
                result.append(", ").append("?");
                break;
            }
        }
        return result.append("]").toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ISeq)) return false;

        var other = (ISeq) o;
        return !other.isEmpty() &&
                first().equals(other.first()) &&
                rest().equals(other.rest());
    }

    @Override
    public int hashCode() {
        return first().hashCode() + rest().hashCode() * 31;
    }
}


