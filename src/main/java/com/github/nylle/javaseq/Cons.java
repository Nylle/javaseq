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
            throw new IndexOutOfBoundsException(i);
        }
        Seq<T> current = this;
        for (int x = i; x > 0; --x) {
            if (current.rest().isEmpty()) {
                throw new IndexOutOfBoundsException(i);
            }
            current = current.rest();
        }
        return current.first();
    }

    @Override
    public Seq<T> take(long n) {
        if (n <= 0) {
            return Nil.of();
        }

        return Seq.cons(first, () -> rest().take(n - 1));
    }

    @Override
    public int size() {
        return 1 + rest().size();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}
