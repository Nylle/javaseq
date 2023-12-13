package com.github.nylle.javaseq;

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

    public T second() {
        return nth(1, null);
    }

    public ISeq<T> cons(T x) {
        return Fn.cons(x, this);
    }

    public ISeq<T> filter(Predicate<? super T> pred) {
        return Fn.lazySeq(() -> {
            if (!isEmpty()) {
                return pred.test(first())
                        ? Fn.lazySeq(() -> rest().filter(pred)).cons(first())
                        : rest().filter(pred);
            }
            return Fn.nil();
        });
    }

    public <R> ISeq<R> map(Function<? super T, ? extends R> f) {
        return Fn.lazySeq(() -> {
            if (!isEmpty()) {
                return Fn.cons(f.apply(first()), rest().map(f));
            }
            return Fn.nil();
        });
    }

    public <S, R> ISeq<R> map(ISeq<? extends S> coll, BiFunction<? super T, ? super S, ? extends R> f) {
        return Fn.lazySeq(() -> {
            if (!isEmpty() && !coll.isEmpty()) {
                return Fn.cons(f.apply(first(), coll.first()), rest().map(coll.rest(), f));
            }
            return Fn.nil();
        });
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
        return Fn.lazySeq(() -> {
            if (!isEmpty()) {
                return Fn.concat(f.apply(first()).iterator(), rest().mapcat(f));
            }
            return Fn.nil();
        });
    }

    public <S, R> ISeq<R> mapcat(ISeq<? extends S> coll, BiFunction<? super T, ? super S, Iterable<? extends R>> f) {
        return Fn.lazySeq(() -> {
            if (!isEmpty() && !coll.isEmpty()) {
                return Fn.concat(f.apply(first(), coll.first()).iterator(), rest().mapcat(coll.rest(), f));
            }
            return Fn.nil();
        });
    }

    public <S, R> ISeq<R> mapcat(Iterable<? extends S> coll, BiFunction<? super T, ? super S, Iterable<? extends R>> f) {
        return mapcat(Fn.seq(coll), f);
    }

    public <S, R> ISeq<R> mapcat(Iterator<? extends S> coll, BiFunction<? super T, ? super S, Iterable<? extends R>> f) {
        return mapcat(Fn.seq(coll), f);
    }

    public <S, R> ISeq<R> mapcat(Stream<? extends S> coll, BiFunction<? super T, ? super S, Iterable<? extends R>> f) {
        return mapcat(Fn.seq(coll), f);
    }

    public <S, R> ISeq<R> mapcat(S[] coll, BiFunction<? super T, ? super S, Iterable<? extends R>> f) {
        return mapcat(Fn.seq(coll), f);
    }

    public <R> ISeq<R> mapcat(CharSequence coll, BiFunction<? super T, ? super Character, Iterable<? extends R>> f) {
        return mapcat(Fn.seq(coll), f);
    }

    public ISeq<T> take(long n) {
        return Fn.lazySeq(() -> {
            if (!isEmpty() && n > 0) {
                return n == 1
                        ? ISeq.of(first())
                        : rest().take(n - 1).cons(first());
            }
            return Fn.nil();
        });
    }

    public ISeq<T> drop(long n) {
        return Fn.lazySeq(() -> {
            if (!isEmpty()) {
                return n > 0
                        ? rest().drop(n - 1)
                        : this;
            }
            return Fn.nil();
        });
    }

    public ISeq<T> takeWhile(Predicate<? super T> pred) {
        return Fn.lazySeq(() -> {
            if (!isEmpty() && pred.test(first())) {
                return rest().takeWhile(pred).cons(first());
            }
            return Fn.nil();
        });
    }

    public ISeq<T> dropWhile(Predicate<? super T> pred) {
        return Fn.lazySeq(() -> {
            if (!isEmpty()) {
                return pred.test(first())
                        ? rest().dropWhile(pred)
                        : this;
            }
            return Fn.nil();
        });
    }

    public ISeq<List<T>> partition(int n) {
        return partition(n, n);
    }

    public ISeq<List<T>> partition(int n, int step) {
        return partition(n, step, null);
    }

    public ISeq<List<T>> partition(int n, int step, Iterable<T> pad) {
        return Fn.lazySeq(() -> {
            if (n < 0 || isEmpty()) {
                return Fn.nil();
            }
            var part = take(n).toList();
            if (part.size() < n) {
                if (pad == null) {
                    return Fn.nil();
                }
                return Fn.cons(
                        Fn.concat(part, Fn.seq(pad).take(n - (long) part.size())).toList(),
                        drop(step).partition(n, step, pad));
            }
            return Fn.cons(part, drop(step).partition(n, step, pad));
        });
    }

    public ISeq<List<T>> partitionAll(int n) {
        return partition(n, n, List.of());
    }

    public ISeq<List<T>> partitionAll(int n, int step) {
        return partition(n, step, List.of());
    }

    public ISeq<T> reductions(BinaryOperator<T> f) {
        return Fn.lazySeq(() -> {
            if (!isEmpty()) {
                return rest().reductions(first(), f);
            }
            return Fn.nil();
        });
    }

    public <U> ISeq<U> reductions(U init, BiFunction<U, ? super T, U> f) {
        return Fn.lazySeq(() -> {
            if (!isEmpty()) {
                return rest().reductions(f.apply(init, first()), f).cons(init);
            }
            return ISeq.of(init);
        });
    }

    public Optional<T> reduce(BinaryOperator<T> f) {
        if (!isEmpty() && !rest().isEmpty()) {
            return Optional.of(rest().reduce(first(), f));
        }
        return Optional.empty();
    }

    public <U> U reduce(U val, BiFunction<U, ? super T, U> f) {
        var result = val;
        ISeq<T> s = this;
        while (!s.isEmpty()) {
            result = f.apply(result, s.first());
            s = s.rest();
        }
        return result;
    }

    public void run(Consumer<? super T> proc) {
        if (!isEmpty()) {
            proc.accept(first());
            rest().run(proc);
        }
    }

    public ISeq<T> distinct() {
        return step(this, new HashSet<>());
    }

    private static <T> ISeq<T> step(final ISeq<T> seq, final Set<T> seen) {
        return Fn.lazySeq(() -> {
            var result = seq.filter(x -> !seen.contains(x));
            if (result.isEmpty()) {
                return Fn.nil();
            }
            var first = result.first();
            return step(result.rest(), Fn.conj(seen, first)).cons(first);
        });
    }

    @SuppressWarnings("unchecked")
    public ISeq<T> sorted() {
        return sorted((o1, o2) -> ((Comparable<T>) o1).compareTo(o2));
    }

    public ISeq<T> sorted(Comparator<? super T> comp) {
        var result = new ArrayList<>(this);
        result.sort(comp);
        return Fn.seq(result);
    }

    public ISeq<T> reverse() {
        var iter = iterator();
        var acc = Fn.<T>nil();
        while(iter.hasNext()) {
            acc = Fn.cons(iter.next(), acc);
        }
        return acc;
    }

    public boolean some(Predicate<? super T> pred) {
        return !isEmpty() && (pred.test(first()) || rest().some(pred));
    }

    public boolean every(Predicate<? super T> pred) {
        return isEmpty() || pred.test(first()) && rest().every(pred);
    }

    public boolean notAny(Predicate<? super T> pred) {
        return isEmpty() || every(pred.negate());
    }

    public Optional<T> max(Comparator<? super T> comp) {
        if (isEmpty()) {
            return Optional.empty();
        }
        if (rest().isEmpty()) {
            return Optional.of(first());
        }
        var result = first();
        var s = rest();
        while (!s.isEmpty()) {
            var next = s.first();
            result = comp.compare(result, next) > 0 ? result : next;
            s = s.rest();
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
        if (index < 0 || isEmpty()) {
            return notFound;
        }
        ISeq<T> s = this;
        for (int i = index; i > 0; --i) {
            if (s.rest().isEmpty()) {
                return notFound;
            }
            s = s.rest();
        }
        return s.first();
    }

    public String str() {
        return reduce("", (a, b) -> a + b);
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


