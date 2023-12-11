package com.github.nylle.javaseq;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

class Nil<T> extends ASeq<T> implements ISeq<T> {

	private static final Nil<?> NIL = new Nil<>();

	@SuppressWarnings("unchecked")
	public static <T> Nil<T> empty() {
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
	public <S, R> ISeq<R> map(ISeq<? extends S> coll, BiFunction<? super T, ? super S, ? extends R> f) {
		return empty();
	}

	@Override
	public <R> ISeq<R> mapcat(Function<? super T, ? extends Iterable<? extends R>> f) {
		return empty();
	}

	@Override
	public <S, R> ISeq<R> mapcat(ISeq<? extends S> coll, BiFunction<? super T, ? super S, Iterable<? extends R>> f) {
		return empty();
	}

	@Override
	public int size() {
		return 0;
	}

	@Override
	public ISeq<T> realize() {
		return this;
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