package com.github.nylle.javaseq;

import java.util.AbstractList;
import java.util.NoSuchElementException;

public class Nil<T> extends AbstractList<T> implements Seq<T> {
    private static final Nil<?> nil = new Nil<>();

    @SuppressWarnings("unchecked")
    public static <T> Nil<T> of() {
        return (Nil<T>) nil;
    }

    @Override
    public T first() {
        throw new NoSuchElementException("first");
    }

    @Override
    public Seq<T> rest() {
        throw new NoSuchElementException("rest");
    }

    @Override
    public T get(int index) {
        throw new IndexOutOfBoundsException(index);
    }

    @Override
    public Seq<T> take(long n) {
        return of();
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }
}
