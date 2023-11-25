package com.github.nylle.javaseq;

import java.util.AbstractList;
import java.util.List;
import java.util.function.BiFunction;
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
}
