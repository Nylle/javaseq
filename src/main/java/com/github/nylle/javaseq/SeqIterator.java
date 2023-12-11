package com.github.nylle.javaseq;

import java.util.Iterator;
import java.util.function.Consumer;

public class SeqIterator<T> implements Iterator<T> {

	private ISeq<T> seq;

	public SeqIterator(ISeq<T> seq) {
		this.seq = seq;
	}

	@Override
	public boolean hasNext() {
		return !seq.isEmpty();
	}

	@Override
	public T next() {
		var next = seq.first();
		seq = seq.rest();
		return next;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("remove");
	}

	@Override
	public void forEachRemaining(Consumer<? super T> action) {
		seq.run(action);
	}
}
