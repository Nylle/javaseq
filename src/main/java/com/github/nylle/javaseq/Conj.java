package com.github.nylle.javaseq;

import java.util.AbstractList;

public class Conj<T> extends AbstractList<T> implements Seq<T> {
    private final T first;
    private final Seq<T> rest;

    Conj(T first, Seq<T> rest) {
        this.first = first;
        this.rest = rest;
    }

    @Override
    public T first() {
        return first;
    }

    @Override
    public Seq<T> rest() {
        return rest;
    }

    @Override
    public T get(final int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException(Integer.toString(index));
        }
        Seq<T> current = this;
        for (int i = index; i > 0; --i) {
            if (current.rest().isEmpty()) {
                throw new IndexOutOfBoundsException(Integer.toString(index));
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

        if (n == 1) {
            return Seq.conj(first, Seq.of());
        }

        return Seq.conj(first, rest.take(n - 1));
    }

    @Override
    public int size() {
        return 1 + rest.size();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}
