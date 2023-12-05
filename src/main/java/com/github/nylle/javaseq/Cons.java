package com.github.nylle.javaseq;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

class Cons<T> extends ASeq<T> implements ISeq<T> {

	private final T first;
	private final ISeq<T> rest;

	Cons(T first, ISeq<T> rest) {
		this.first = first;
		this.rest = rest;
	}

	@Override
	public T first() {
		return first;
	}

	@Override
	public ISeq<T> rest() {
		return rest;
	}

	@Override
	public boolean isRealized() {
		return true;
	}

	@Override
	public <R> ISeq<R> map(Function<? super T, ? extends R> f) {
		return ISeq.cons(f.apply(first), rest.map(f));
	}

	@Override
	public ISeq<T> filter(Predicate<? super T> pred) {
		if (pred.test(first)) {
			return ISeq.cons(first, rest.filter(pred));
		} else {
			return rest.filter(pred);
		}
	}

	@Override
	public <R> ISeq<R> mapcat(Function<? super T, ? extends Iterable<? extends R>> f) {
		return ISeq.concat(copy(f.apply(first)), rest.mapcat(f));
	}

	@Override
	public <S, R> ISeq<R> mapcat(Iterable<? extends S> coll, BiFunction<? super T, ? super S, Iterable<? extends R>> f) {
		var other = ISeq.sequence(coll);
		return other.isEmpty()
				? ISeq.of()
				: ISeq.concat(copy(f.apply(first(), other.first())), rest.mapcat(other.rest(), f));
	}

	@Override
	public boolean isEmpty() {
		return false;
	}
}