package com.github.nylle.javaseq;

import java.util.Iterator;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

class LazySeq<T> extends ASeq<T> implements ISeq<T> {
    private volatile ISeq<T> seq;
    private final Supplier<ISeq<T>> fn;

    LazySeq(Supplier<ISeq<T>> f) {
        this.fn = f;
    }

    @Override
    public T first() {
        seq();
        return seq.first();
    }

    @Override
    public ISeq<T> rest() {
        seq();
        return seq.rest();
    }

    @Override
    public boolean isEmpty() {
        seq();
        return seq.isEmpty();
    }

    @Override
    public int size() {
        return seq().size();
    }

    @Override
    public boolean isRealized() {
        return seq != null;
    }

    @Override
    public ISeq<T> filter(Predicate<? super T> pred) {
        return new LazySeq<>(() -> {
            if (seq().isEmpty()) {
                return ISeq.of();
            }
            return pred.test(first())
                    ? ISeq.cons(first(), new LazySeq<>(() -> rest().filter(pred)))
                    : rest().filter(pred);
        });
    }

    @Override
    public <R> ISeq<R> map(Function<? super T, ? extends R> f) {
        return new LazySeq<>(() -> {
            if (seq().isEmpty()) {
                return ISeq.of();
            }
            return ISeq.cons(f.apply(first()), rest().map(f));
        });
    }

    @Override
    public <S, R> ISeq<R> map(ISeq<? extends S> coll, BiFunction<? super T, ? super S, ? extends R> f) {
        return new LazySeq<>(() -> seq().isEmpty() || coll.isEmpty()
                ? ISeq.of()
                : ISeq.cons(f.apply(first(), coll.first()), rest().map(coll.rest(), f)));
    }

    @Override
    public <R> ISeq<R> mapcat(Function<? super T, ? extends Iterable<? extends R>> f) {
        return new LazySeq<>(() -> seq().isEmpty()
                ? ISeq.of()
                : concat(iterator(f.apply(first())), rest().mapcat(f)));
    }

    @Override
    public <S, R> ISeq<R> mapcat(ISeq<? extends S> coll, BiFunction<? super T, ? super S, Iterable<? extends R>> f) {
        return new LazySeq<>(() -> seq().isEmpty() || coll.isEmpty()
                ? ISeq.of()
                : concat(iterator(f.apply(first(), coll.first())), rest().mapcat(coll.rest(), f)));
    }

    @Override
    public String toString() {
        var result = new StringBuilder("[");
        ISeq<T> seq = this;
        while (!seq.isEmpty()) {
            result.append(seq.first());
            if (seq.rest().isRealized()) {
                if (!seq.rest().isEmpty()) {
                    result.append(", ");
                }
                seq = seq.rest();
            } else {
                result.append(", ").append("?");
                break;
            }
        }
        return result.append("]").toString();
    }

    private ISeq<T> seq() {
        if (!isRealized()) {
            synchronized (this) {
                if (!isRealized()) {
                    seq = fn.get();
                }
            }
        }
        return seq;
    }

    private static <T> ISeq<T> concat(Iterator<T> iterator, ISeq<T> seq) {
        return new LazySeq<>(() -> {
            if (!iterator.hasNext()) {
                return seq;
            }
            return ISeq.cons(iterator.next(), concat(iterator, seq));
        });
    }

    private static <T> Iterator<T> iterator(Iterable<? extends T> iterable) {
        return (Iterator<T>) iterable.iterator();
    }
}