package com.github.nylle.javaseq;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
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
	public T second() {
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
	public T nth(int index) {
		throw new IndexOutOfBoundsException(index);
	}

	@Override
	public T nth(int index, T notFound) {
		return notFound;
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
	public <R> ISeq<R> mapcat(Function<? super T, ? extends Iterable<? extends R>> f) {
		return empty();
	}

	@Override
	public <S, R> ISeq<R> mapcat(Iterable<? extends S> coll, BiFunction<? super T, ? super S, Iterable<? extends R>> f) {
		return empty();
	}

	@Override
	public ISeq<T> reductions(BinaryOperator<T> f) {
		return empty();
	}

	@Override
	public <U> ISeq<U> reductions(U init, BiFunction<U, ? super T, U> f) {
		return ISeq.of(init);
	}

	@Override
	public Optional<T> reduce(BinaryOperator<T> f) {
		return Optional.empty();
	}

	@Override
	public <U> U reduce(U val, BiFunction<U, ? super T, U> f) {
		return val;
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