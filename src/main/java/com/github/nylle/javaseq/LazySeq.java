package com.github.nylle.javaseq;

import java.util.function.BiFunction;
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
    public boolean isRealized() {
        return seq != null;
    }

    @Override
    public <U> U reduce(U val, BiFunction<U, ? super T, U> f) {
        return seq().reduce(val, f);
    }

    @Override
    public T nth(int index) {
        return seq().nth(index);
    }

    @Override
    public T nth(int index, T notFound) {
        return seq().nth(index, notFound);
    }

    @Override
    public int count() {
        return seq().count();
    }

    @Override
    public Object[] toArray() {
        return seq().toArray();
    }

    @Override
    public int indexOf(Object o) {
        return seq().indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return seq().lastIndexOf(o);
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