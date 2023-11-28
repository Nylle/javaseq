package com.github.nylle.javaseq;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@SuppressWarnings("java:S3077")
public class Cons<T> extends AbstractList<T> implements Seq<T> {
    private final T first;
    private volatile Seq<T> rest;
    private final Supplier<Seq<T>> f;

    Cons(T first, Supplier<Seq<T>> f) {
        this.first = first;
        this.f = f;
    }

    @Override
    public T first() {
        return first;
    }

    @Override
    public Seq<T> rest() {
        if (!isRealized()) {
            synchronized (this) {
                if (!isRealized()) {
                    rest = f.get();
                }
            }
        }
        return rest;
    }

    @Override
    public T get(int index) {
        if (index < 0) {
            return null;
        }
        Seq<T> current = this;
        for (int i = index; i > 0; --i) {
            if (current.rest().isEmpty()) {
                return null;
            }
            current = current.rest();
        }
        return current.first();
    }

    @Override
    public Seq<T> take(long n) {
        return n <= 0 ? Nil.of() : Seq.cons(first, () -> rest().take(n - 1));
    }

    @Override
    public Seq<T> drop(long n) {
        return n > 0 ? rest().drop(n - 1) : this;
    }

    @Override
    public Seq<T> filter(Predicate<? super T> pred) {
        return pred.test(first) ? Seq.cons(first, () -> rest().filter(pred)) : rest().filter(pred);
    }

    @Override
    public <R> Seq<R> map(Function<? super T, ? extends R> f) {
        return Seq.cons(f.apply(first()), () -> rest().map(f));
    }

    @Override
    public <S, R> Seq<R> map(Seq<? extends S> other, BiFunction<? super T, ? super S, ? extends R> f) {
        return other.isEmpty()
                ? Nil.of()
                : Seq.cons(f.apply(first(), other.first()), () -> rest().map(other.rest(), f));
    }

    @Override
    public <R> Seq<R> mapcat(Function<? super T, ? extends Iterable<? extends R>> f) {
        return Seq.concat(Seq.of(f.apply(first)).map(x -> x), () -> rest().mapcat(f));
    }

    @Override
    public Seq<T> takeWhile(Predicate<? super T> pred) {
        return pred.test(first()) ? Seq.cons(first(), () -> rest().takeWhile(pred)) : Nil.of();
    }

    @Override
    public Seq<T> dropWhile(Predicate<? super T> pred) {
        return pred.test(first()) ? rest().dropWhile(pred) : this;
    }

    @Override
    public Seq<List<T>> partition(int n) {
        return partition(n, n);
    }

    @Override
    public Seq<List<T>> partition(int n, int step) {
        return partition(n, step, null);
    }

    @Override
    public Seq<List<T>> partition(int n, int step, Iterable<T> pad) {
        if (n < 0) {
            return Nil.of();
        }
        var partition = take(n).toList();
        if (partition.size() < n) {
            return pad == null
                    ? Nil.of()
                    : Seq.cons(pad(partition, pad, n), () -> drop(step).partition(n, step, pad));
        }
        return Seq.cons(partition, () -> drop(step).partition(n, step, pad));
    }

    @Override
    public Seq<List<T>> partitionAll(int n) {
        return partition(n, n, List.of());
    }

    @Override
    public Seq<List<T>> partitionAll(int n, int step) {
        return partition(n, step, List.of());
    }

    @Override
    public Seq<T> reductions(BinaryOperator<T> f) {
        return rest().reductions(first(), f);
    }

    @Override
    public Seq<T> reductions(T init, BinaryOperator<T> f) {
        return Seq.cons(init, () -> rest().reductions(f.apply(init, first()), f));
    }

    @Override
    public Optional<T> reduce(BinaryOperator<T> f) {
        return Optional.of(rest().reduce(first(), f));
    }

    @Override
    public <R> R reduce(R val, BiFunction<R, ? super T, R> f) {
        var acc = val;
        Seq<T> seq = this;
        while (!seq.isEmpty()) {
            acc = f.apply(acc, seq.first());
            seq = seq.rest();
        }
        return acc;
    }

    @Override
    public Seq<T> distinct() {
        return distinct(this, new HashSet<>());
    }

    @Override
    @SuppressWarnings("unchecked")
    public Seq<T> sorted() {
        return sorted((o1, o2) -> ((Comparable<T>) o1).compareTo(o2));
    }

    @Override
    public Seq<T> sorted(Comparator<? super T> comp) {
        var list = new ArrayList<>(this);
        list.sort(comp);
        return Seq.of(list);
    }

    @Override
    public boolean some(Predicate<? super T> pred) {
        return pred.test(first()) || rest().some(pred);
    }

    @Override
    public boolean every(Predicate<? super T> pred) {
        return pred.test(first()) && rest().every(pred);
    }

    @Override
    public boolean notAny(Predicate<? super T> pred) {
        return every(pred.negate());
    }

    @Override
    public boolean isRealized() {
        return rest != null;
    }

    @Override
    public Optional<T> max(Comparator<? super T> comp) {
        if (rest().isEmpty()) {
            return Optional.of(first());
        }
        var result = first();
        var current = this.rest();
        while (!current.isEmpty()) {
            var next = current.first();
            result = comp.compare(result, next) > 0 ? result : next;
            current = current.rest();
        }
        return Optional.of(result);
    }

    @Override
    public Optional<T> min(Comparator<? super T> comp) {
        return max(comp.reversed());
    }

    @Override
    public <C extends Comparable<? super C>> Optional<T> maxKey(Function<T, C> f) {
        return max(Comparator.comparing(t -> f.apply(t)));
    }

    @Override
    public <C extends Comparable<? super C>> Optional<T> minKey(Function<T, C> f) {
        return min(Comparator.comparing(t -> f.apply(t)));
    }

    @Override
    public Optional<T> find(int i) {
        return Optional.ofNullable(get(i));
    }

    @Override
    public Optional<T> findFirst() {
        return Optional.ofNullable(first());
    }

    @Override
    public Optional<T> findFirst(Predicate<? super T> pred) {
        return filter(pred).findFirst();
    }

    @Override
    public <K, V> Map<K, V> toMap(Function<T, K> k, Function<T, V> v) {
        return Map.ofEntries(map(x -> Map.entry(k.apply(x), v.apply(x))).toArray(new Map.Entry[0]));
    }

    @Override
    public <K, V> Map<K, V> toMap() {
        if(first instanceof Map.Entry<?,?>) {
            return Map.ofEntries(this.toArray(new Map.Entry[0]));
        }
        throw new UnsupportedOperationException("Seq is not of type Map.Entry. Provide key- and value-mappers.");
    }

    @Override
    public void forEach(Consumer<? super T> f) {
        f.accept(first());
        rest().forEach(f);
    }

    @Override
    public List<T> toList() {
        rest().toList();
        return List.copyOf(this);
    }

    @Override
    public int size() {
        return 1 + rest().size();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public Iterator<T> iterator() {
        var seq = this;
        return new Iterator<>() {
            private Seq<T> source = seq;

            @Override
            public boolean hasNext() {
                return !source.isEmpty();
            }

            @Override
            public T next() {
                var next = source.first();
                source = source.rest();
                return next;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("remove");
            }

            @Override
            public void forEachRemaining(Consumer<? super T> f) {
                source.forEach(f);
            }
        };
    }

    @Override
    public Stream<T> stream() {
        return StreamSupport.stream(((Iterable<T>) () -> iterator()).spliterator(), false);
    }

    @Override
    public Stream<T> parallelStream() {
        return stream();
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        return drop(fromIndex).take((long)toIndex - fromIndex);
    }

    @Override
    @SuppressWarnings("java:S3740")
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Seq other)) return false;
        return !other.isEmpty() && first().equals(other.first()) && rest().equals(other.rest());
    }

    @Override
    public int hashCode() {
        return first().hashCode() + rest().hashCode() * 31;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("[");
        Seq<T> seq = this;
        while (!seq.isEmpty()) {
            s.append(seq.first());
            if (seq.isRealized()) {
                if (!seq.rest().isEmpty()) {
                    s.append(", ");
                }
                seq = seq.rest();
            } else {
                s.append(", ").append("?");
                break;
            }
        }
        return s.append("]").toString();
    }

    private static <T> List<T> pad(List<T> partition, Iterable<T> pad, int n) {
        return Seq.concat(partition, () -> Seq.of(pad).take(n - (long)partition.size())).toList();
    }

    private static <T> Seq<T> distinct(Seq<T> seq, Set<T> exclude) {
        var additionalItems = seq.filter(t -> !exclude.contains(t));
        if (additionalItems.isEmpty()) {
            return Seq.of();
        }
        exclude.add(additionalItems.first());
        return Seq.cons(additionalItems.first(), () -> distinct(additionalItems.rest(), exclude));
    }
}
