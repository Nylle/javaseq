package com.github.nylle.javaseq;

import java.util.AbstractList;
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
    public T get(final int i) {
        if (i < 0) {
            return null;
        }
        Seq<T> current = this;
        for (int x = i; x > 0; --x) {
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
}
