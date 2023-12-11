package com.github.nylle.javaseq;

import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

class LazySeq2<T> extends ASeq<T> implements ISeq<T> {
    private volatile ISeq<T> seq;
    private Supplier<ISeq<T>> fn;

    LazySeq2(Supplier<ISeq<T>> f) {
        this.fn = f;
    }

    @Override
    public T first() {
        seq();
        if(seq == null) return null;
        return seq.first();
    }

    @Override
    public ISeq<T> rest() {
        seq();
        if(seq == null) return null;
        return seq.rest();
    }

    @Override
    public boolean isEmpty() {
        seq();
        return seq == null || seq.first() == null;
    }

    @Override
    public int size() {
        seq();
        return seq == null ? 0 : seq.size();
    }

    @Override
    public boolean isRealized() {
        return fn == null;
    }

    @Override
    public ISeq<T> filter(Predicate<? super T> pred) {
        return Fn.filter(pred, this);
    }

    @Override
    public <R> ISeq<R> map(Function<? super T, ? extends R> f) {
        return Fn.map(f, this);
    }

    @Override
    public <S, R> ISeq<R> map(ISeq<? extends S> coll, BiFunction<? super T, ? super S, ? extends R> f) {
        return Fn.map(f, this, coll);
    }

    @Override
    public <R> ISeq<R> mapcat(Function<? super T, ? extends Iterable<? extends R>> f) {
        return Fn.mapcat(f, this);
    }

    @Override
    public <S, R> ISeq<R> mapcat(ISeq<? extends S> coll, BiFunction<? super T, ? super S, Iterable<? extends R>> f) {
        return Fn.mapcat(f, this, coll);
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
                    fn = null;
                }
            }
        }
        return seq;
    }

    ///////// to be moved out /////////


    @Override
    public ISeq<T> realize() {
        seq();
        return seq;
    }

    @Override
    public ISeq<T> distinct() {
        return Fn.distinct(this);
    }

    @Override
    public List<T> toList() {
        if (!isEmpty()) {
            return List.copyOf(seq);
        }
        return List.of();
    }

    @Override
    public Set<T> toSet() {
        if (!isEmpty()) {
            return Set.copyOf(this.realize());
        }
        return Set.of();
    }

}