package com.github.nylle.javaseq;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
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
		return false;
	}

	@Override
	public ISeq<T> take(long n) {
		return empty();
	}

	@Override
	public ISeq<T> drop(long n) {
		return empty();
	}

	@Override
	public T get(int index) {
		throw new IndexOutOfBoundsException(index);
	}

	@Override
	public <R> ISeq<R> map(Function<? super T, ? extends R> f) {
		return empty();
	}

	@Override
	public ISeq<T> filter(Predicate<? super T> pred) {
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
	public Optional<T> min(Comparator<? super T> comp) {
		return Optional.empty();
	}

	@Override
	public Optional<T> max(Comparator<? super T> comp) {
		return Optional.empty();
	}

	@Override
	public int size() {
		return 0;
	}

	@Override
	public boolean some(Predicate<? super T> pred) {
		return false;
	}

	@Override
	public boolean every(Predicate<? super T> pred) {
		return true;
	}

	@Override
	public T nth(int index, T notFound) {
		return notFound;
	}

	@Override
	public <S, R> ISeq<R> map(Iterable<? extends S> other, BiFunction<? super T, ? super S, ? extends R> f) {
		return empty();
	}

	@Override
	public ISeq<T> takeWhile(Predicate<? super T> pred) {
		return empty();
	}

	@Override
	public ISeq<T> dropWhile(Predicate<? super T> pred) {
		return empty();
	}

	@Override
	public ISeq<List<T>> partition(int n, int step, Iterable<T> pad) {
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
	public void run(Consumer<? super T> proc) {
	}

	@Override
	public ISeq<T> distinct() {
		return empty();
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