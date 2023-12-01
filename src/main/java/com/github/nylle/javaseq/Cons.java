package com.github.nylle.javaseq;

import java.util.ArrayList;
import java.util.function.Function;
import java.util.function.Predicate;

class Cons<T> extends ASeq<T> implements ISeq<T> {

	private final T first;
	private final ISeq<T> rest;

	public Cons(T first, ISeq<T> rest) {
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
		var result = new ArrayList<R>();
		f.apply(first).forEach(x -> result.add(x));
		return ISeq.concat(result, rest.mapcat(f));
	}

	@Override
	public boolean isEmpty() {
		return false;
	}
}