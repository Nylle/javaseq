package com.github.nylle.javaseq;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public abstract class ASeq<T> extends AList<T> implements ISeq<T> {

    public T second() {
        return nth(1, (T)null);
    }

    public ISeq<T> cons(T x) {
        return ISeq.cons(x, this);
    }

    public ISeq<T> concat(T... xs) {
        return ISeq.concat(this, ISeq.seq(xs));
    }

    public ISeq<T> concat(Iterable<T> coll) {
        return ISeq.concat(this, ISeq.seq(coll));
    }

    public ISeq<T> concat(Iterator<T> coll) {
        return ISeq.concat(this, ISeq.seq(coll));
    }

    public ISeq<T> concat(Stream<T> coll) {
        return ISeq.concat(this, ISeq.seq(coll));
    }

    public ISeq<T> filter(Predicate<? super T> pred) {
        return ISeq.lazySeq(() -> {
            if (!isEmpty()) {
                return pred.test(first())
                        ? ISeq.cons(first(), rest().filter(pred))
                        : rest().filter(pred);
            }
            return ISeq.of();
        });
    }

    public <R> ISeq<R> map(Function<? super T, ? extends R> f) {
        return ISeq.lazySeq(() -> {
            if (!isEmpty()) {
                return ISeq.cons(f.apply(first()), rest().map(f));
            }
            return ISeq.of();
        });
    }

    public <S, R> ISeq<R> map(Iterable<? extends S> coll, BiFunction<? super T, ? super S, ? extends R> f) {
        return ISeq.lazySeq(() -> {
            var s = ISeq.seq(coll);
            if (!isEmpty() && !s.isEmpty()) {
                return ISeq.cons(f.apply(first(), s.first()), rest().map(s.rest(), f));
            }
            return ISeq.of();
        });
    }

    public <S, R> ISeq<R> map(Iterator<? extends S> coll, BiFunction<? super T, ? super S, ? extends R> f) {
        return map(ISeq.seq(coll), f);
    }

    public <S, R> ISeq<R> map(Stream<? extends S> coll, BiFunction<? super T, ? super S, ? extends R> f) {
        return map(ISeq.seq(coll), f);
    }

    public <S, R> ISeq<R> map(S[] coll, BiFunction<? super T, ? super S, ? extends R> f) {
        return map(ISeq.seq(coll), f);
    }

    public <R> ISeq<R> map(CharSequence coll, BiFunction<? super T, ? super Character, ? extends R> f) {
        return map(ISeq.seq(coll), f);
    }

    public <R> ISeq<R> mapcat(Function<? super T, ? extends Iterable<? extends R>> f) {
        return ISeq.lazySeq(() -> {
            if (!isEmpty()) {
                return Util.concat(f.apply(first()).iterator(), rest().mapcat(f));
            }
            return ISeq.of();
        });
    }

    public <S, R> ISeq<R> mapcat(Iterable<? extends S> coll, BiFunction<? super T, ? super S, Iterable<? extends R>> f) {
        return ISeq.lazySeq(() -> {
            var s = ISeq.seq(coll);
            if (!isEmpty() && !s.isEmpty()) {
                return Util.concat(f.apply(first(), s.first()).iterator(), rest().mapcat(s.rest(), f));
            }
            return ISeq.of();
        });
    }

    public <S, R> ISeq<R> mapcat(Iterator<? extends S> coll, BiFunction<? super T, ? super S, Iterable<? extends R>> f) {
        return mapcat(ISeq.seq(coll), f);
    }

    public <S, R> ISeq<R> mapcat(Stream<? extends S> coll, BiFunction<? super T, ? super S, Iterable<? extends R>> f) {
        return mapcat(ISeq.seq(coll), f);
    }

    public <S, R> ISeq<R> mapcat(S[] coll, BiFunction<? super T, ? super S, Iterable<? extends R>> f) {
        return mapcat(ISeq.seq(coll), f);
    }

    public <R> ISeq<R> mapcat(CharSequence coll, BiFunction<? super T, ? super Character, Iterable<? extends R>> f) {
        return mapcat(ISeq.seq(coll), f);
    }

    public ISeq<T> take(long n) {
        return ISeq.lazySeq(() -> {
            if (n > 0 && !isEmpty()) {
                return n == 1
                        ? ISeq.of(first())
                        : ISeq.cons(first(), rest().take(n - 1));
            }
            return ISeq.of();
        });
    }

    public ISeq<T> drop(long n) {
        return ISeq.lazySeq(() -> {
            if (!isEmpty()) {
                return n > 0
                        ? rest().drop(n - 1)
                        : this;
            }
            return ISeq.of();
        });
    }

    public ISeq<T> takeWhile(Predicate<? super T> pred) {
        return ISeq.lazySeq(() -> {
            if (!isEmpty() && pred.test(first())) {
                return ISeq.cons(first(), rest().takeWhile(pred));
            }
            return ISeq.of();
        });
    }

    public ISeq<T> dropWhile(Predicate<? super T> pred) {
        return ISeq.lazySeq(() -> {
            if (!isEmpty()) {
                return pred.test(first())
                        ? rest().dropWhile(pred)
                        : this;
            }
            return ISeq.of();
        });
    }

    public ISeq<ISeq<T>> partition(int n) {
        return partition(n, n);
    }

    public ISeq<ISeq<T>> partition(int n, int step) {
        return partition(n, step, null);
    }

    public ISeq<ISeq<T>> partition(int n, int step, Iterable<T> pad) {
        return ISeq.lazySeq(() -> {
            if (n < 0 || isEmpty()) {
                return ISeq.of();
            }
            var part = take(n);
            if (part.count() < n) {
                if (pad == null) {
                    return ISeq.of();
                }
                return ISeq.cons(
                        ISeq.concat(part, ISeq.seq(pad).take(n - (long) part.count())),
                        drop(step).partition(n, step, pad));
            }
            return ISeq.cons(part, drop(step).partition(n, step, pad));
        });
    }

    public ISeq<ISeq<T>> partitionAll(int n) {
        return partition(n, n, List.of());
    }

    public ISeq<ISeq<T>> partitionAll(int n, int step) {
        return partition(n, step, List.of());
    }

    public ISeq<T> reductions(BinaryOperator<T> f) {
        return ISeq.lazySeq(() -> {
            if (!isEmpty()) {
                return rest().reductions(first(), f);
            }
            return ISeq.of();
        });
    }

    public <U> ISeq<U> reductions(U init, BiFunction<U, ? super T, U> f) {
        return ISeq.lazySeq(() -> {
            if (!isEmpty()) {
                return ISeq.cons(init, rest().reductions(f.apply(init, first()), f));
            }
            return ISeq.of(init);
        });
    }

    public Optional<T> reduce(BinaryOperator<T> f) {
        if (!isEmpty()) {
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
        reduce(ISeq.of(), (a, b) -> {
            proc.accept(b);
            return ISeq.of();
        });
    }

    public ISeq<T> distinct() {
        return step(this, new HashSet<>());
    }

    private static <T> ISeq<T> step(final ISeq<T> seq, final Set<T> seen) {
        return ISeq.lazySeq(() -> {
            var result = seq.filter(x -> !seen.contains(x));
            if (result.isEmpty()) {
                return ISeq.of();
            }
            var first = result.first();
            return ISeq.cons(first, step(result.rest(), Util.conj(seen, first)));
        });
    }

    @SuppressWarnings("unchecked")
    public ISeq<T> sorted() {
        return sorted((o1, o2) -> ((Comparable<T>) o1).compareTo(o2));
    }

    public ISeq<T> sorted(Comparator<? super T> comp) {
        var result = new ArrayList<>(this);
        result.sort(comp);
        return ISeq.seq(result);
    }

    public ISeq<T> reverse() {
        var iter = iterator();
        var acc = ISeq.<T>of();
        while(iter.hasNext()) {
            acc = ISeq.cons(iter.next(), acc);
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
        return reduce((a, b) -> comp.compare(a, b) > 0 ? a : b);
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
        return nth(index, () -> {
            throw new IndexOutOfBoundsException(index);
        });
    }

    public T nth(int index, T notFound) {
        return nth(index, () -> notFound);
    }

    protected T nth(int index, Supplier<T> notFound) {
        if (index < 0 || isEmpty()) {
            return notFound.get();
        }
        ISeq<T> s = this;
        for (int i = index; i > 0; --i) {
            if (s.rest().isEmpty()) {
                return notFound.get();
            }
            s = s.rest();
        }
        return s.first();
    }

    public String str() {
        return reduce("", (a, b) -> a + b);
    }

    public int count() {
        var i = 0;
        ISeq<T> seq = this;
        while (!seq.isEmpty()) {
            i++;
            seq = seq.drop(1);
        }
        return i;
    }

    public Optional<T> find(int i) {
        return Optional.ofNullable(nth(i, (T)null));
    }

    public Optional<T> findFirst() {
        return Optional.ofNullable(first());
    }

    public Optional<T> findFirst(Predicate<? super T> pred) {
        return filter(pred).findFirst();
    }

    public <K, V> Map<K, V> toMap() {
        if (isEmpty()) return Map.of();
        if (first() instanceof Map.Entry<?, ?>) {
            return toMap(k -> ((Map.Entry<K, V>) k).getKey(), v -> ((Map.Entry<K, V>) v).getValue(), (a, b) -> b);
        }
        throw new UnsupportedOperationException("ISeq is not of type Map.Entry. Provide key- and value-mappers");
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

    public List<T> reify() {
        return List.copyOf(this);
    }

    public ISeq<Map.Entry<T, Integer>> frequencies() {
        var acc = new HashMap<T, Integer>();
        this.run(x -> acc.compute(x, (k, v) -> (v == null) ? 1 : v + 1));
        return ISeq.seq(acc);
    }

    // java.lang.Iterable

    @Override
    public void forEach(Consumer<? super T> action) {
        run(action);
    }


    // java.util.List

    @Override
    public T get(final int index) {
        return nth(index);
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public int size() {
        return count();
    }

    @Override
    public Iterator<T> iterator() {
        return new SeqIterator<>(this);
    }

    @Override
    public Spliterator<T> spliterator() {
        return ((Iterable<T>) () -> iterator()).spliterator();
    }


    // java.util.Collection

    @Override
    public Stream<T> stream() {
        return StreamSupport.stream(((Iterable<T>) () -> iterator()).spliterator(), false);
    }

    @Override
    public Stream<T> parallelStream() {
        return stream();
    }


    // java.lang.Object

    @Override
    public String toString() {
        var result = new StringBuilder("[");
        ISeq<T> seq = this;
        while (!seq.isEmpty()) {
            result.append(seq.first());
            if (seq.rest().isRealized()) {
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
        return isEmpty() && other.isEmpty() || !other.isEmpty() &&
                first().equals(other.first()) &&
                rest().equals(other.rest());
    }

    @Override
    public int hashCode() {
        return isEmpty() ? 0 : first().hashCode() + rest().hashCode() * 31;
    }
}


