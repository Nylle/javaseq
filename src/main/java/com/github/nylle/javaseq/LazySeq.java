package com.github.nylle.javaseq;

import java.util.function.Supplier;

class LazySeq<T> extends ASeq<T> implements ISeq<T> {
    private volatile ISeq<T> seq;
    private final Supplier<ISeq<T>> fn;

    LazySeq(Supplier<ISeq<T>> f) {
        this.fn = f;
    }

    private ISeq<T> seq() {
        if (!isRealized()) {
            synchronized (this) {
                if (!isRealized()) {
                    seq = unwrap(fn.get());
                }
            }
        }
        return seq;
    }

    private ISeq<T> unwrap(ISeq<T> seq) {
        while(seq instanceof LazySeq<T> s) {
            seq = s.seq();
        }
        return seq;
    }

    @Override
    public T first() {
        return seq().first();
    }

    @Override
    public ISeq<T> rest() {
        return seq().rest();
    }

    @Override
    public boolean isEmpty() {
        return seq() instanceof Nil;
    }

    @Override
    public int count() {
        return seq().count();
    }

    @Override
    public boolean isRealized() {
        return seq != null;
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
}