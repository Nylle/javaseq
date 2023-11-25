package com.github.nylle.javaseq;

import java.util.AbstractList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;

public class Nil<T> extends AbstractList<T> implements Seq<T> {
    private static final Nil<?> nil = new Nil<>();

    @SuppressWarnings("unchecked")
    public static <T> Nil<T> of() {
        return (Nil<T>) nil;
    }

    @Override
    public T first() {
        return null;
    }

    @Override
    public Seq<T> rest() {
        return of();
    }

    @Override
    public T get(int index) {
        return null;
    }

    @Override
    public Seq<T> take(long n) {
        return of();
    }

    @Override
    public Seq<T> drop(long n) {
        return of();
    }

    @Override
    public Seq<T> filter(Predicate<? super T> pred) {
        return of();
    }

    @Override
    public <R> Seq<R> map(Function<? super T, ? extends R> f) {
        return of();
    }

    @Override
    public <S, R> Seq<R> map(Seq<? extends S> other, BiFunction<? super T, ? super S, ? extends R> f) {
        return of();
    }

    @Override
    public <R> Seq<R> mapcat(Function<? super T, ? extends Iterable<? extends R>> f) {
        return of();
    }

    @Override
    public Seq<T> takeWhile(Predicate<? super T> pred) {
        return of();
    }

    @Override
    public Seq<T> dropWhile(Predicate<? super T> pred) {
        return of();
    }

    @Override
    public Seq<List<T>> partition(int n) {
        return partition(n, n);
    }

    @Override
    public Seq<List<T>> partition(int n, int step) {
        return of();
    }

    @Override
    public Seq<List<T>> partition(int n, int step, Iterable<T> pad) {
        return of();
    }

    @Override
    public Seq<List<T>> partitionAll(int n) {
        return of();
    }

    @Override
    public Seq<List<T>> partitionAll(int n, int step) {
        return of();
    }

    @Override
    public Seq<T> reductions(BinaryOperator<T> f) {
        return of();
    }

    @Override
    public Seq<T> reductions(T init, BinaryOperator<T> f) {
        return Seq.of(init);
    }

    @Override
    public List<T> toList() {
        return List.of();
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Nil;
    }

    @Override
    public int hashCode() {
        return 0;
    }
}
