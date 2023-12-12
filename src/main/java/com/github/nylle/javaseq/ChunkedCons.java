package com.github.nylle.javaseq;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class ChunkedCons<T> extends ASeq<T> implements ISeq<T> {

    private final IChunk<T> chunk;
    private final ISeq<T> rest;
    private final int chunkSize;

    ChunkedCons(IChunk<T> chunk, ISeq<T> rest) {
        this.chunk = chunk;
        this.rest = rest;
        this.chunkSize = chunk.count();
    }

    @Override
    public T first() {
        return chunk.nth(0);
    }

    @Override
    public ISeq<T> rest() {
        if (chunkSize > 1) {
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
        var acc = new ArrayList<T>();
        for (int i = 0; i < chunkSize; i++) {
            if (pred.test(chunk.nth(i))) {
                acc.add(chunk.nth(i));
            }
        }
        return ISeq.concat(acc, rest.filter(pred));
    }

    @Override
    public <R> ISeq<R> map(Function<? super T, ? extends R> f) {
        var acc = new ArrayList<R>();
        for (int i = 0; i < chunkSize; i++) {
            acc.add(f.apply(chunk.nth(i)));
        }
        return ISeq.concat(acc, rest.map(f));
    }

    @Override
    public <S, R> ISeq<R> map(ISeq<? extends S> coll, BiFunction<? super T, ? super S, ? extends R> f) {
        if (coll.isEmpty()) {
            return ISeq.of();
        }
        var acc = new ArrayList<R>();
        for (int i = 0; i < chunkSize; i++) {
            acc.add(f.apply(chunk.nth(i), coll.nth(i)));
        }

        return ISeq.concat(acc, rest.map(coll.drop(chunkSize), f));
    }

    @Override
    public ISeq<T> take(long n) {
        if (n < 1) {
            return ISeq.of();
        }
        if (n > chunkSize) {
            return new ChunkedCons<>(chunk, rest.take(n - chunkSize));
        }

        var acc = ISeq.<T>of();
        for (int i = (int) n - 1; i >= 0; i--) {
            acc = acc.cons(nth(i));
        }
        return acc;
    }

    @Override
    public ISeq<T> drop(long n) {
        if (n < 1) {
            return this;
        }

        if (n > chunkSize) {
            return rest.drop(n - chunkSize);
        }

        IChunk<T> acc = chunk;
        for(int i = 0; i < n; i++) {
            acc = acc.dropFirst();
        }

        return new ChunkedCons<>(acc, rest);
    }

    @Override
    public void run(Consumer<? super T> proc) {
        for (int i = 0; i < chunkSize; i++) {
            proc.accept(chunk.nth(i));
        }
        rest.run(proc);
    }

    @Override
    public List<T> toList() {
        var acc = new ArrayList<T>();
        for (int i = 0; i < chunkSize; i++) {
            acc.add(chunk.nth(i));
        }
        acc.addAll(rest.toList());
        return acc;
    }
}
