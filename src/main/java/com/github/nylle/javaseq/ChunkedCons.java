package com.github.nylle.javaseq;

import java.util.ArrayList;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public class ChunkedCons<T> extends ASeq<T> implements ISeq<T> {

    public static final int CHUNK_SIZE = 1000;

    private final IChunk<T> chunk;
    private final ISeq<T> rest;

    ChunkedCons(IChunk<T> chunk, ISeq<T> rest) {
        this.chunk = chunk;
        this.rest = rest;
    }

    static <T> ISeq<T> chunked(ISeq<T> seq) {
        if(seq.isEmpty()) {
            return seq;
        }
        if (seq instanceof ChunkedCons<T> s) {
            return s;
        }
        return new ChunkedCons<>(ArrayChunk.from(seq.partitionAll(CHUNK_SIZE).first()), seq.drop(CHUNK_SIZE));
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
        var acc = new ArrayList<T>();
        for (int i = 0; i < chunk.count(); i++) {
            if (pred.test(chunk.nth(i))) {
                acc.add(chunk.nth(i));
            }
        }

        return ISeq.concat(acc, chunked(rest).filter(pred));
    }

    @Override
    public <R> ISeq<R> map(Function<? super T, ? extends R> f) {
        return ISeq.cons(f.apply(first()), rest().map(f));
    }

    @Override
    public <R> ISeq<R> mapcat(Function<? super T, ? extends Iterable<? extends R>> f) {
        return ISeq.concat(copy(f.apply(first())), rest().mapcat(f));
    }

    @Override
    public <S, R> ISeq<R> mapcat(Iterable<? extends S> coll, BiFunction<? super T, ? super S, Iterable<? extends R>> f) {
        var other = ISeq.sequence(coll);
        return other.isEmpty()
                ? ISeq.of()
                : ISeq.concat(copy(f.apply(first(), other.first())), rest().mapcat(other.rest(), f));
    }

//    @Override
//    public ISeq<T> take(long n) {
//        if(n <= 0) {
//            return ISeq.of();
//        }
//
//        if(chunk.count() < n) {
//            return new ChunkedCons<>(chunk, rest.take(n - chunk.count()));
//        }
//
//        var acc = ISeq.<T>of();
//        for(int i = (int)n-1; i >= 0; i--) {
//            acc = acc.cons(nth(i));
//        }
//        return acc;
//    }
//
//    @Override
//    public int size() {
//        return chunk.count() + rest().size();
//    }

}
