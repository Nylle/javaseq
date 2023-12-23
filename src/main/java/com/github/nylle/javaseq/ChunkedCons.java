package com.github.nylle.javaseq;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public class ChunkedCons<T> extends ASeq<T> implements ISeq<T> {

    private final IChunk<T> chunk;
    private final ISeq<T> rest;

    ChunkedCons(IChunk<T> chunk, ISeq<T> rest) {
        this.chunk = chunk;
        this.rest = rest;
    }

    private static <T> ChunkedCons<T> chunkedCons(ArrayList<T> xs, ISeq<T> rest) {
        return new ChunkedCons<T>(new ArrayChunk(xs.toArray()), rest);
    }

    @Override
    public T first() {
        return chunk.nth(0);
    }

    @Override
    public ISeq<T> rest() {
        if (chunk.count() > 1) {
            return new ChunkedCons<>(chunk.dropFirst(), rest);
        }
        return rest;
    }

    @Override
    public boolean isRealized() {
        return true;
    }

    @Override
    public ISeq<T> filter(Predicate<? super T> pred) {
        return ISeq.lazySeq(() -> {
            var acc = new ArrayList<T>();
            for (int i = 0; i < chunk.count(); i++) {
                if (pred.test(chunk.nth(i))) {
                    acc.add(chunk.nth(i));
                }
            }
            if (acc.isEmpty()) {
                return rest.filter(pred);
            }
            return chunkedCons(acc, rest.filter(pred));
        });
    }

    @Override
    public <R> ISeq<R> map(Function<? super T, ? extends R> f) {
        return ISeq.lazySeq(() -> {
            var acc = new ArrayList<R>();
            for (int i = 0; i < chunk.count(); i++) {
                acc.add(f.apply(chunk.nth(i)));
            }
            return chunkedCons(acc, rest.map(f));
        });
    }

    @Override
    public <S, R> ISeq<R> map(Iterable<? extends S> coll, BiFunction<? super T, ? super S, ? extends R> f) {
        return ISeq.lazySeq(() -> {
            var s = ISeq.seq(coll);
            if (s.isEmpty()) {
                return ISeq.of();
            }
            var acc = new Object[chunk.count()];
            for (int i = 0; i < chunk.count(); i++) {
                acc[i] = f.apply(chunk.nth(i), s.nth(i));
            }
            return new ChunkedCons<R>(new ArrayChunk(acc), rest.map(s.drop(chunk.count()), f));
        });
    }

    @Override
    public ISeq<T> take(long n) {
        return ISeq.lazySeq(() -> {
            if (n < 1) {
                return ISeq.of();
            }
            if (n >= chunk.count()) {
                return new ChunkedCons<>(chunk, rest.take(n - chunk.count()));
            }

            var acc = ISeq.<T>of();
            for (int i = (int) n - 1; i >= 0; i--) {
                acc = ISeq.cons(nth(i), acc);
            }
            return acc;
        });
    }

    @Override
    public ISeq<T> drop(long n) {
        return ISeq.lazySeq(() -> {
            if (n < 1) {
                return this;
            }

            if (n >= chunk.count()) {
                return rest.drop(n - chunk.count());
            }

            IChunk<T> acc = chunk;
            for (int i = 0; i < n; i++) {
                acc = acc.dropFirst();
            }

            return new ChunkedCons<>(acc, rest);
        });
    }

    @Override
    public ISeq<T> takeWhile(Predicate<? super T> pred) {
        return ISeq.lazySeq(() -> {
            var end = 0;
            for (int i = 0; i < chunk.count(); i++) {
                if (!pred.test(chunk.nth(i))) {
                    break;
                }
                end++;
            }
            if (end == 0) { // no match
                return ISeq.of();
            }
            if (end == chunk.count()) { // all match
                return new ChunkedCons<>(chunk, rest.takeWhile(pred));
            }
            return new ChunkedCons<>(chunk.dropLast(chunk.count() - end), ISeq.of());
        });
    }

    @Override
    public ISeq<T> dropWhile(Predicate<? super T> pred) {
        return ISeq.lazySeq(() -> {
            IChunk<T> acc = chunk;
            for (int i = 0; i < chunk.count(); i++) {
                if (pred.test(chunk.nth(i))) {
                    acc = acc.dropFirst();
                } else {
                    break;
                }
            }
            if(acc.count() == 0) { // all items match
                return rest.dropWhile(pred);
            }
            return new ChunkedCons<>(acc, rest);
        });
    }

    @Override
    public <U> ISeq<U> reductions(U init, BiFunction<U, ? super T, U> f) {
        return ISeq.lazySeq(() -> {
            var acc = new ArrayList<U>();
            acc.add(init);
            var inter = init;
            for (int i = 0; i < chunk.count() - 1; i++) {
                inter = f.apply(inter, chunk.nth(i));
                acc.add(inter);
            }
            return chunkedCons(acc, rest.reductions(f.apply(inter, chunk.nth(chunk.count() -1)), f));
        });
    }

    @Override
    public <U> U reduce(U val, BiFunction<U, ? super T, U> f) {
        var result = val;
        for (int i = 0; i < chunk.count(); i++) {
            result = f.apply(result, chunk.nth(i));
        }
        return rest.reduce(result, f);
    }

    @Override
    public boolean some(Predicate<? super T> pred) {
        for(int i = 0; i < chunk.count(); i++) {
            if(pred.test(chunk.nth(i))) {
                return true;
            }
        }
        return rest().some(pred);
    }

    @Override
    public boolean every(Predicate<? super T> pred) {
        for(int i = 0; i < chunk.count(); i++) {
            if(!pred.test(chunk.nth(i))) {
                return false;
            }
        }
        return rest().every(pred);
    }

    @Override
    public T nth(int index) {
        if(index < 0) throw new IndexOutOfBoundsException(index);
        if(index < chunk.count()) {
            return chunk.nth(index);
        }
        try {
            return rest.nth(index - chunk.count());
        } catch(IndexOutOfBoundsException ex) {
            throw new IndexOutOfBoundsException(index);
        }
    }

    @Override
    public T nth(int index, T notFound) {
        if(index < 0) return notFound;
        if(index < chunk.count()) {
            return chunk.nth(index);
        }
        return rest.nth(index - chunk.count(), notFound);
    }

    @Override
    public int count() {
        return chunk.count() + rest.count();
    }

    @Override
    public List<T> toList() {
        var acc = new ArrayList<T>();
        for (int i = 0; i < chunk.count(); i++) {
            acc.add(chunk.nth(i));
        }
        acc.addAll(rest.toList());
        return List.copyOf(acc);
    }
}
