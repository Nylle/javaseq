package com.github.nylle.javaseq;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public class StringSeq extends ASeq<Character> implements ISeq<Character> {

    private final CharSequence s;
    private final int i;

    StringSeq(CharSequence s, int i) {
        if(s == null || s.isEmpty()) {
            throw new IllegalArgumentException("string is null or empty");
        }
        this.s = s;
        this.i = i;
    }

    @Override
    public Character first() {
        return s.charAt(i);
    }

    @Override
    public ISeq<Character> rest() {
        return i + 1 < s.length() ? new StringSeq(s, i + 1) : ISeq.of();
    }

    @Override
    public boolean isRealized() {
        return true;
    }

    @Override
    public ISeq<Character> filter(Predicate<? super Character> pred) {
        if (pred.test(first())) {
            return rest().filter(pred).cons(first());
        } else {
            return rest().filter(pred);
        }
    }

    @Override
    public <R> ISeq<R> map(Function<? super Character, ? extends R> f) {
        return ISeq.cons(f.apply(first()), rest().map(f));
    }

    @Override
    public <S, R> ISeq<R> map(ISeq<? extends S> coll, BiFunction<? super Character, ? super S, ? extends R> f) {
        return coll.isEmpty()
                ? ISeq.of()
                : ISeq.cons(f.apply(first(), coll.first()), rest().map(coll.rest(), f));
    }

    @Override
    public <R> ISeq<R> mapcat(Function<? super Character, ? extends Iterable<? extends R>> f) {
        return ISeq.concat(copy(f.apply(first())), rest().mapcat(f));
    }

    @Override
    public <S, R> ISeq<R> mapcat(ISeq<? extends S> coll, BiFunction<? super Character, ? super S, Iterable<? extends R>> f) {
        return coll.isEmpty()
                ? ISeq.of()
                : ISeq.concat(copy(f.apply(first(), coll.first())), rest().mapcat(coll.rest(), f));
    }

    @Override
    public ISeq<Character> cons(Character x) {
        return new StringSeq(x.toString() + s.subSequence(i, s.length()), 0);
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        StringSeq that = (StringSeq) o;

        if (i != that.i) return false;
        return Objects.equals(s, that.s);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (s != null ? s.hashCode() : 0);
        result = 31 * result + i;
        return result;
    }
}
