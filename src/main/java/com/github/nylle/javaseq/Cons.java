package com.github.nylle.javaseq;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

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
        if (rest == null) {
            synchronized (this) {
                if (rest == null) {
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
    public Optional<T> max(Comparator<? super T> comp) {
        if (rest().isEmpty()) {
            return Optional.of(first());
        }
        var result = first();
        var current = this.rest();
        while (!current.isEmpty()) {
            var next = current.first();
            result = comp.compare(result, next) >= 0 ? result : next;
            current = current.rest();
        }
        return Optional.of(result);
    }

    @Override
    public Optional<T> min(Comparator<? super T> comparator) {
        return max(comparator.reversed());
    }

    @Override
    public <C extends Comparable<? super C>> Optional<T> maxKey(Function<T, C> f) {
        return max(Comparator.comparing(t -> f.apply(t)));
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Seq other)) return false;
        return !other.isEmpty() && first().equals(other.first()) && rest().equals(other.rest());
    }

    @Override
    public int hashCode() {
        return first().hashCode() + rest().hashCode() * 31;
    }

    private static <T> List<T> pad(List<T> partition, Iterable<T> pad, int n) {
        return Seq.concat(partition, () -> Seq.of(pad).take(n - partition.size())).toList();
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
