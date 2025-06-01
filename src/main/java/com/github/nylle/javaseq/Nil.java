package com.github.nylle.javaseq;

import java.util.function.Supplier;

class Nil<T> extends ASeq<T> implements ISeq<T> {

	private static final Nil<?> NIL = new Nil<>();

	@SuppressWarnings("unchecked")
	static <T> Nil<T> empty() {
		return (Nil<T>) NIL;
	}

	@Override
	public T first() {
		return null;
	}

	@Override
	public T last() {
		return null;
	}

	@Override
	public ISeq<T> rest() {
		return empty();
	}

	@Override
	public boolean isRealized() {
		return true;
	}

	@Override
	protected T nth(int index, Supplier<T> notFound) {
		return notFound.get();
	}

	@Override
	public int count() {
		return 0;
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof Nil;
	}

	@Override
	public int hashCode() {
		return 0;
	}

	@Override
	public boolean isEmpty() {
		return true;
	}
}