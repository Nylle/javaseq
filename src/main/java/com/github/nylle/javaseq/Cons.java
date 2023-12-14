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
	public ISeq<T> filter(Predicate<? super T> pred) {
		if (pred.test(first)) {
			return new Cons<>(first, rest.filter(pred));
		} else {
			return rest.filter(pred);
		}
	}

	@Override
	public <R> ISeq<R> map(Function<? super T, ? extends R> f) {
		return new Cons<>(f.apply(first), rest.map(f));
	}

	@Override
	public <S, R> ISeq<R> map(ISeq<? extends S> coll, BiFunction<? super T, ? super S, ? extends R> f) {
		return coll.isEmpty()
				? Fn.nil()
				: new Cons<>(f.apply(first(), coll.first()), rest().map(coll.rest(), f));
	}

	@Override
	public boolean isEmpty() {
		return false;
	}
}