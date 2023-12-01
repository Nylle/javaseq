package com.github.nylle.javaseq;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

class LazySeq<T> extends ASeq<T> implements ISeq<T> {
	private final T first;
	private volatile ISeq<T> rest;
	private final Supplier<ISeq<T>> fn;

	LazySeq(T first, Supplier<ISeq<T>> f) {
		this.first = first;
		this.fn = f;
	}

	@Override
	public T first() {
		return first;
	}

	@Override
	public ISeq<T> rest() {
		if (!isRealized()) {
			synchronized (this) {
				if (!isRealized()) {
					rest = fn.get();
				}
			}
		}
		return rest;
	}

	@Override
	public boolean isRealized() {
		return rest != null;
	}

	public <R> ISeq<R> map(Function<? super T, ? extends R> f) {
		return ISeq.lazySeq(f.apply(first()), () -> rest().map(f));
	}

	@Override
	public ISeq<T> filter(Predicate<? super T> pred) {
		if (pred.test(first)) {
			return ISeq.lazySeq(first, () -> rest().filter(pred));
		} else {
			return rest().filter(pred);
		}
	}

	@Override
	public <R> ISeq<R> mapcat(Function<? super T, ? extends Iterable<? extends R>> f) {
		var result = new ArrayList<R>();
		f.apply(first).forEach(x -> result.add(x));
		return concat(result.iterator(), () -> rest().mapcat(f));
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	private static <T> ISeq<T> concat(Iterator<T> iterator, Supplier<ISeq<T>> seq) {
		if (iterator.hasNext()) {
			return concatRecursive(iterator, seq);
		} else {
			return seq.get();
		}
	}

	private static <T> ISeq<T> concatRecursive(Iterator<T> iterator, Supplier<ISeq<T>> seq) {
		var next = iterator.next();
		if (iterator.hasNext()) {
			return ISeq.lazySeq(next, () -> concatRecursive(iterator, seq));
		} else {
			return ISeq.lazySeq(next, seq);
		}
	}
}