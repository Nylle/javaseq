package com.github.nylle.javaseq;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
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
        return ISeq.concat(acc, rest.filter(pred));
    }

    @Override
    public <R> ISeq<R> map(Function<? super T, ? extends R> f) {
        var acc = new ArrayList<R>();
        for (int i = 0; i < chunk.count(); i++) {
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
        for (int i = 0; i < chunk.count(); i++) {
            acc.add(f.apply(chunk.nth(i), coll.nth(i)));
        }

        return ISeq.concat(acc, rest.map(coll.drop(chunk.count()), f));
    }

    @Override
    public <R> ISeq<R> mapcat(Function<? super T, ? extends Iterable<? extends R>> f) {
        var acc = new ArrayList<R>();
        for (int i = 0; i < chunk.count(); i++) {
            acc.addAll(copy(f.apply(chunk.nth(i))));
        }
        return ISeq.concat(acc, rest.mapcat(f));
    }

    @Override
    public <S, R> ISeq<R> mapcat(Iterable<? extends S> coll, BiFunction<? super T, ? super S, Iterable<? extends R>> f) {
        var other = ISeq.sequence(coll);
        return other.isEmpty()
                ? ISeq.of()
                : ISeq.concat(copy(f.apply(first(), other.first())), rest().mapcat(other.rest(), f));
    }

    @Override
    public ISeq<T> take(long n) {
        if (n < 1) {
            return ISeq.of();
        }

        if (n > chunk.count()) {
            return new ChunkedCons<>(chunk, rest.take(n - chunk.count()));
        }

        var acc = ISeq.<T>of();
        for (int i = (int) n - 1; i >= 0; i--) {
            acc = acc.cons(nth(i));
        }
        return acc;
    }

    @Override
    public void run(Consumer<? super T> proc) {
        for (int i = 0; i < chunk.count(); i++) {
            proc.accept(chunk.nth(i));
        }
        rest.run(proc);
    }

    @Override
    public List<T> toList() {
        var acc = new ArrayList<T>();
        for (int i = 0; i < chunk.count(); i++) {
            acc.add(chunk.nth(i));
        }
        acc.addAll(rest.toList());
        return acc;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ChunkedCons<?> that = (ChunkedCons<?>) o;

        if (!Objects.equals(chunk, that.chunk)) return false;
        return Objects.equals(rest, that.rest);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (chunk != null ? chunk.hashCode() : 0);
        result = 31 * result + (rest != null ? rest.hashCode() : 0);
        return result;
    }
}
