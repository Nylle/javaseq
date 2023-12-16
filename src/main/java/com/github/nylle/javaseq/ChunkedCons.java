package com.github.nylle.javaseq;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
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

    private static <T> ChunkedCons<T> chunkedCons(ArrayList<T> xs, ISeq<T> rest) {
        return new ChunkedCons<T>(new ArrayChunk(xs.toArray()), rest);
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
        return Fn.lazySeq(() -> {
            var acc = new ArrayList<T>();
            for (int i = 0; i < chunkSize; i++) {
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
        return Fn.lazySeq(() -> {
            var acc = new ArrayList<R>();
            for (int i = 0; i < chunkSize; i++) {
                acc.add(f.apply(chunk.nth(i)));
            }
            return chunkedCons(acc, rest.map(f));
        });
    }

    @Override
    public <S, R> ISeq<R> map(ISeq<? extends S> coll, BiFunction<? super T, ? super S, ? extends R> f) {
        return Fn.lazySeq(() -> {
            if (coll.isEmpty()) {
                return Fn.nil();
            }
            var acc = new Object[chunkSize];
            for (int i = 0; i < chunkSize; i++) {
                acc[i] = f.apply(chunk.nth(i), coll.nth(i));
            }
            return new ChunkedCons<R>(new ArrayChunk(acc), rest.map(coll.drop(chunkSize), f));
        });
    }

    @Override
    public ISeq<T> take(long n) {
        return Fn.lazySeq(() -> {
            if (n < 1) {
                return Fn.nil();
            }
            if (n >= chunkSize) {
                return new ChunkedCons<>(chunk, rest.take(n - chunkSize));
            }

            var acc = Fn.<T>nil();
            for (int i = (int) n - 1; i >= 0; i--) {
                acc = acc.cons(nth(i));
            }
            return acc;
        });
    }

    @Override
    public ISeq<T> drop(long n) {
        return Fn.lazySeq(() -> {
            if (n < 1) {
                return this;
            }

            if (n >= chunkSize) {
                return rest.drop(n - chunkSize);
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
        return Fn.lazySeq(() -> {
            var end = 0;
            for (int i = 0; i < chunkSize; i++) {
                if (!pred.test(chunk.nth(i))) {
                    break;
                }
                end++;
            }
            if (end == 0) { // no match
                return Fn.nil();
            }
            if (end == chunkSize) { // all match
                return new ChunkedCons<>(chunk, rest.takeWhile(pred));
            }
            return new ChunkedCons<>(chunk.dropLast(chunkSize - end), Fn.nil());
        });
    }

    @Override
    public ISeq<T> dropWhile(Predicate<? super T> pred) {
        return Fn.lazySeq(() -> {
            IChunk<T> acc = chunk;
            for (int i = 0; i < chunkSize; i++) {
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
        return Fn.lazySeq(() -> {
            var acc = new ArrayList<U>();
            acc.add(init);
            var inter = init;
            for (int i = 0; i < chunkSize - 1; i++) {
                inter = f.apply(inter, chunk.nth(i));
                acc.add(inter);
            }
            return chunkedCons(acc, rest.reductions(f.apply(inter, chunk.nth(chunkSize-1)), f));
        });
    }

    @Override
    public <U> U reduce(U val, BiFunction<U, ? super T, U> f) {
        var result = val;
        for (int i = 0; i < chunkSize; i++) {
            result = f.apply(result, chunk.nth(i));
        }
        return rest.reduce(result, f);
    }

    @Override
    public void run(Consumer<? super T> proc) {
        for (int i = 0; i < chunkSize; i++) {
            proc.accept(chunk.nth(i));
        }
        rest.run(proc);
    }

    @Override
    public boolean some(Predicate<? super T> pred) {
        for(int i = 0; i < chunkSize; i++) {
            if(pred.test(chunk.nth(i))) {
                return true;
            }
        }
        return rest().some(pred);
    }

    @Override
    public boolean every(Predicate<? super T> pred) {
        for(int i = 0; i < chunkSize; i++) {
            if(!pred.test(chunk.nth(i))) {
                return false;
            }
        }
        return rest().every(pred);
    }

    @Override
    public Optional<T> max(Comparator<? super T> comp) {
        var max = chunk.nth(0);
        for(int i = 1; i < chunkSize; i++) {
            var next = chunk.nth(i);
            max = comp.compare(max, next) > 0 ? max : next;
        }

        var more = rest.max(comp);
        if(more.isEmpty()) {
            return Optional.of(max);
        }

        return comp.compare(max, more.get()) > 0 ? Optional.of(max) : more;
    }

    @Override
    public T nth(int index) {
        if(index < 0) throw new IndexOutOfBoundsException(index);
        if(index < chunkSize) {
            return chunk.nth(index);
        }
        try {
            return rest.nth(index - chunkSize);
        } catch(IndexOutOfBoundsException ex) {
            throw new IndexOutOfBoundsException(index);
        }
    }

    @Override
    public T nth(int index, T notFound) {
        if(index < 0) return notFound;
        if(index < chunkSize) {
            return chunk.nth(index);
        }
        return rest.nth(index - chunkSize, notFound);
    }

    @Override
    public int count() {
        return chunkSize + rest.count();
    }

    @Override
    public List<T> toList() {
        var acc = new ArrayList<T>();
        for (int i = 0; i < chunkSize; i++) {
            acc.add(chunk.nth(i));
        }
        acc.addAll(rest.toList());
        return List.copyOf(acc);
    }
}
