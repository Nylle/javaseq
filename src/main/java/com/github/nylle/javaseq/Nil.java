package com.github.nylle.javaseq;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

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
	public ISeq<T> rest() {
		return empty();
	}

	@Override
	public boolean isRealized() {
		return true;
	}

	@Override
	public T get(int index) {
		throw new IndexOutOfBoundsException(index);
	}

	@Override
	public ISeq<T> filter(Predicate<? super T> pred) {
		return empty();
	}

	@Override
	public <R> ISeq<R> map(Function<? super T, ? extends R> f) {
		return empty();
	}

	@Override
	public <S, R> ISeq<R> map(Iterable<? extends S> coll, BiFunction<? super T, ? super S, ? extends R> f) {
		return empty();
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