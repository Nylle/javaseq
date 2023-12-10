package com.github.nylle.javaseq;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

class LazySeq<T> extends ASeq<T> implements ISeq<T> {
    private volatile ISeq<T> seq;
    private final Supplier<ISeq<T>> fn;

    LazySeq(Supplier<ISeq<T>> f) {
        this.fn = f;
    }

    @Override
    public T first() {
        seq();
        return seq.first();
    }

    @Override
    public ISeq<T> rest() {
        seq();
        return seq.rest();
    }

    @Override
    public boolean isEmpty() {
        seq();
        return seq.isEmpty();
    }

    @Override
    public int size() {
        return seq().size();
    }

    @Override
    public T nth(int index, T notFound) {
        if (index < 0 || isEmpty()) {
            return notFound;
        }
        return super.nth(index, notFound);
    }

    @Override
    public boolean isRealized() {
        return seq != null;
    }

    @Override
    public ISeq<T> filter(Predicate<? super T> pred) {
        if (seq().isEmpty()) {
            return Nil.empty();
        }

        if (pred.test(first())) {
            return ISeq.cons(first(), ISeq.lazySeq(() -> rest().filter(pred)));
        } else {
            return ISeq.lazySeq(() -> rest().filter(pred));
        }
    }

    @Override
    public <R> ISeq<R> map(Function<? super T, ? extends R> f) {
        if (seq().isEmpty()) {
            return Nil.empty();
        }

        return ISeq.cons(f.apply(first()), ISeq.lazySeq(() -> rest().map(f)));
    }

    @Override
    public <S, R> ISeq<R> map(ISeq<? extends S> coll, BiFunction<? super T, ? super S, ? extends R> f) {
        return seq().isEmpty() || coll.isEmpty()
                ? ISeq.of()
                : ISeq.cons(f.apply(first(), coll.first()), ISeq.lazySeq(() -> rest().map(coll.rest(), f)));
    }

    @Override
    public <R> ISeq<R> mapcat(Function<? super T, ? extends Iterable<? extends R>> f) {
        return seq().isEmpty()
                ? ISeq.of()
                : concat(ASeq.<R>copy(f.apply(first())).iterator(), ISeq.lazySeq(() -> rest().mapcat(f)));
    }

    @Override
    public <S, R> ISeq<R> mapcat(ISeq<? extends S> coll, BiFunction<? super T, ? super S, Iterable<? extends R>> f) {
        return seq().isEmpty() || coll.isEmpty()
                ? ISeq.of()
                : concat(ASeq.<R>copy(f.apply(first(), coll.first())).iterator(), ISeq.lazySeq(() -> rest().mapcat(coll.rest(), f)));
    }

    @Override
    public ISeq<T> take(long n) {
        if (isEmpty()) {
            return ISeq.of();
        }
        return super.take(n);
    }

    @Override
    public ISeq<T> drop(long n) {
        if (isEmpty()) {
            return ISeq.of();
        }
        return super.drop(n);
    }

    @Override
    public ISeq<T> takeWhile(Predicate<? super T> pred) {
        if (isEmpty()) {
            return ISeq.of();
        }
        return super.takeWhile(pred);
    }

    @Override
    public ISeq<T> dropWhile(Predicate<? super T> pred) {
        if (isEmpty()) {
            return ISeq.of();
        }
        return super.dropWhile(pred);
    }

    @Override
    public ISeq<List<T>> partition(int n, int step, Iterable<T> pad) {
        if (n < 0 || isEmpty()) {
            return ISeq.of();
        }
        var partition = take(n).toList();
        if (partition.size() < n) {
            if (pad == null) return ISeq.of();
            return ISeq.cons(
                    ISeq.concat(partition, ISeq.sequence(pad).take(n - (long) partition.size())).toList(),
                    ISeq.lazySeq(() -> drop(step).partition(n, step, pad)));
        }
        return ISeq.cons(partition, ISeq.lazySeq(() -> drop(step).partition(n, step, pad)));
    }

    @Override
    public ISeq<T> reductions(BinaryOperator<T> f) {
        if (isEmpty()) {
            return ISeq.of();
        }
        return super.reductions(f);
    }

    @Override
    public <U> ISeq<U> reductions(U init, BiFunction<U, ? super T, U> f) {
        if (isEmpty()) {
            return ISeq.of(init);
        }
        return ISeq.cons(init, ISeq.lazySeq(() -> rest().reductions(f.apply(init, first()), f)));
    }

    @Override
    public boolean some(Predicate<? super T> pred) {
        if (isEmpty()) {
            return false;
        }
        return super.some(pred);
    }

    @Override
    public boolean every(Predicate<? super T> pred) {
        if (isEmpty()) {
            return true;
        }
        return super.every(pred);
    }

    @Override
    public boolean notAny(Predicate<? super T> pred) {
        if (isEmpty()) {
            return true;
        }
        return super.notAny(pred);
    }

    @Override
    public Optional<T> max(Comparator<? super T> comp) {
        if (isEmpty()) {
            return Optional.empty();
        }
        return super.max(comp);
    }

    @Override
    public ISeq<T> distinct() {
        return step(this, new HashSet<>());
    }

    private static <T> ISeq<T> step(ISeq<T> seq, Set<T> seen) {
        var result = seq.filter(x -> !seen.contains(x));
        if (result.isEmpty()) {
            return Nil.empty();
        }
        var first = result.first();
        return ISeq.cons(first, ISeq.lazySeq(() -> step(result.rest(), Util.conj(seen, first))));
    }

    @Override
    public <K, V> Map<K, V> toMap() {
        return isEmpty()
                ? Map.of()
                : super.toMap();
    }

    @Override
    public void run(Consumer<? super T> proc) {
        if (!isEmpty()) {
            proc.accept(first());
            rest().run(proc);
        }
    }

    @Override
    public String toString() {
        var result = new StringBuilder("[");
        ISeq<T> seq = this;
        while (!seq.isEmpty()) {
            result.append(seq.first());
            if (seq.rest().isRealized()) {
                if (!seq.rest().isEmpty()) {
                    result.append(", ");
                }
                seq = seq.rest();
            } else {
                result.append(", ").append("?");
                break;
            }
        }
        return result.append("]").toString();
    }

    private ISeq<T> seq() {
        if (!isRealized()) {
            synchronized (this) {
                if (!isRealized()) {
                    seq = fn.get();
                }
            }
        }
        return seq;
    }

    private static <T> ISeq<T> concat(Iterator<T> iterator, ISeq<T> seq) {
        if (iterator.hasNext()) {
            return ISeq.cons(iterator.next(), concat(iterator, seq));
        }
        return seq;
    }
}