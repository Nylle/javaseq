package com.github.nylle.javaseq;

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
	public boolean isEmpty() {
		return false;
	}
}