package com.github.nylle.javaseq;

import java.util.ArrayList;
import java.util.Arrays;
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
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public class Fn {

    private Fn() {
    }

    // util

    public static <T> Set<T> conj(Set<T> coll, T x) {
        var result = new HashSet<>(coll);
        result.add(x);
        return result;
    }

    // creation

    public static <T> ISeq<T> nil() {
        return Nil.empty();
    }

    public static <T> ISeq<T> cons(T x, Iterable<T> coll) {
        return new Cons<>(x, seq(coll));
    }

    public static <T> ISeq<T> lazySeq(Supplier<ISeq<T>> f) {
        return new LazySeq<>(f);
    }

    public static <T> ISeq<T> iterate(T x, UnaryOperator<T> f) {
        return lazySeq(() -> cons(x, iterate(f.apply(x), f)));
    }

    public static <T> ISeq<T> seq(T[] coll) {
        if (coll != null) {
            return seq(Arrays.asList(coll).iterator());
        }
        return nil();
    }

    public static <T> ISeq<T> seq(Iterable<T> coll) {
        if (coll != null) {
            return coll instanceof ISeq<T> seq ? seq : seq(coll.iterator());
        }
        return nil();
    }

    public static <T> ISeq<T> seq(Stream<T> coll) {
        if (coll != null) {
            return seq(coll.iterator());
        }
        return nil();
    }

    public static <K, V> ISeq<Map.Entry<K, V>> seq(Map<K, V> coll) {
        if (coll != null) {
            return seq(coll.entrySet().iterator());
        }
        return nil();
    }

    public static ISeq<Character> seq(CharSequence coll) {
        if (coll != null && !coll.isEmpty()) {
            return new StringSeq(coll, 0);
        }
        return nil();
    }

    public static <T> ISeq<T> seq(Iterator<T> coll) {
        if (coll != null && coll.hasNext()) {
            return lazySeq(() -> cons(coll.next(), seq(coll)));
        }
        return nil();
    }

    @SafeVarargs
    public static <T> ISeq<T> concat(Iterable<T>... colls) {
        return seq(colls).mapcat(x -> x);
    }

    // reducers

    public static <T> ISeq<T> take(long n, ISeq<T> seq) {
        return lazySeq(() -> {
            if (!seq.isEmpty() && n > 0) {
                return n == 1
                        ? ISeq.of(seq.first())
                        : seq.rest().take(n - 1).cons(seq.first());
            }
            return nil();
        });
    }

    public static <T> ISeq<T> drop(long n, ISeq<T> seq) {
        return lazySeq(() -> {
            if (!seq.isEmpty()) {
                return n > 0
                        ? seq.rest().drop(n - 1)
                        : seq;
            }
            return nil();
        });
    }

    public static <T> ISeq<T> takeWhile(Predicate<? super T> pred, ISeq<T> seq) {
        return lazySeq(() -> {
            if (!seq.isEmpty() && pred.test(seq.first())) {
                return seq.rest().takeWhile(pred).cons(seq.first());
            }
            return nil();
        });
    }

    public static <T> ISeq<T> dropWhile(Predicate<? super T> pred, ISeq<T> seq) {
        return lazySeq(() -> {
            if (!seq.isEmpty()) {
                return pred.test(seq.first())
                        ? seq.rest().dropWhile(pred)
                        : seq;
            }
            return nil();
        });
    }

    public static <T> ISeq<T> filter(Predicate<? super T> pred, ISeq<T> seq) {
        return lazySeq(() -> {
            if (!seq.isEmpty()) {
                return pred.test(seq.first())
                        ? lazySeq(() -> seq.rest().filter(pred)).cons(seq.first())
                        : seq.rest().filter(pred);
            }
            return nil();
        });
    }

    public static <R, T> ISeq<R> map(Function<? super T, ? extends R> f, ISeq<T> seq) {
        return lazySeq(() -> {
            if (!seq.isEmpty()) {
                return cons(f.apply(seq.first()), seq.rest().map(f));
            }
            return nil();
        });
    }

    public static <R, S, T> ISeq<R> map(BiFunction<? super T, ? super S, ? extends R> f, ISeq<T> seq1, ISeq<? extends S> seq2) {
        return lazySeq(() -> {
            if (!seq1.isEmpty() && !seq2.isEmpty()) {
                return cons(f.apply(seq1.first(), seq2.first()), seq1.rest().map(seq2.rest(), f));
            }
            return nil();
        });
    }

    public static <R, T> ISeq<R> mapcat(Function<? super T, ? extends Iterable<? extends R>> f, ISeq<T> seq) {
        return lazySeq(() -> {
            if (!seq.isEmpty()) {
                return concat(f.apply(seq.first()).iterator(), mapcat(f, seq.rest()));
            }
            return nil();
        });
    }

    public static <R, S, T> ISeq<R> mapcat(BiFunction<? super T, ? super S, Iterable<? extends R>> f, ISeq<T> seq1, ISeq<? extends S> seq2) {
        return lazySeq(() -> {
            if (!seq1.isEmpty() && !seq2.isEmpty()) {
                return concat(f.apply(seq1.first(), seq2.first()).iterator(), mapcat(f, seq1.rest(), seq2.rest()));
            }
            return nil();
        });
    }

    private static <T> ISeq<T> concat(Iterator<? extends T> iterator, ISeq<T> seq) {
        return lazySeq(() -> {
            if (iterator.hasNext()) {
                return cons(iterator.next(), concat(iterator, seq));
            }
            return seq;
        });
    }

    public static <T> ISeq<List<T>> partition(int n, ISeq<T> seq) {
        return partition(n, n, seq);
    }

    public static <T> ISeq<List<T>> partition(int n, int step, ISeq<T> seq) {
        return partition(n, step, null, seq);
    }

    public static <T> ISeq<List<T>> partition(int n, int step, Iterable<T> pad, ISeq<T> seq) {
        return lazySeq(() -> {
            if (n < 0 || seq.isEmpty()) {
                return nil();
            }
            var part = seq.take(n).toList();
            if (part.size() < n) {
                if (pad == null) {
                    return nil();
                }
                return cons(
                        concat(part, seq(pad).take(n - (long) part.size())).toList(),
                        partition(n, step, pad, seq.drop(step)));
            }
            return cons(part, partition(n, step, pad, seq.drop(step)));
        });
    }

    public static <T> ISeq<List<T>> partitionAll(int n, ISeq<T> seq) {
        return partition(n, n, List.of(), seq);
    }

    public static <T> ISeq<List<T>> partitionAll(int n, int step, ISeq<T> seq) {
        return partition(n, step, List.of(), seq);
    }

    public static <T> ISeq<T> reductions(BinaryOperator<T> f, ISeq<T> seq) {
        return lazySeq(() -> {
            if (!seq.isEmpty()) {
                return seq.rest().reductions(seq.first(), f);
            }
            return nil();
        });
    }

    public static <T, U> ISeq<U> reductions(U init, BiFunction<U, ? super T, U> f, ISeq<T> seq) {
        return lazySeq(() -> {
            if (!seq.isEmpty()) {
                return cons(init, seq.rest().reductions(f.apply(init, seq.first()), f));
            }
            return ISeq.of(init);
        });
    }

    public static <T> Optional<T> reduce(BinaryOperator<T> f, ISeq<T> seq) {
        if (!seq.isEmpty() && !seq.rest().isEmpty()) {
            return Optional.of(seq.rest().reduce(seq.first(), f));
        }
        return Optional.empty();
    }

    public static <T, U> U reduce(U val, BiFunction<U, ? super T, U> f, ISeq<T> seq) {
        var result = val;
        ISeq<T> s = seq;
        while (!s.isEmpty()) {
            result = f.apply(result, s.first());
            s = s.rest();
        }
        return result;
    }

    public static <T> void run(Consumer<? super T> proc, ISeq<T> seq) {
        if (!seq.isEmpty()) {
            proc.accept(seq.first());
            seq.rest().run(proc);
        }
    }

    public static <T> ISeq<T> distinct(ISeq<T> seq) {
        return step(seq, new HashSet<>());
    }

    private static <T> ISeq<T> step(final ISeq<T> seq, final Set<T> seen) {
        return lazySeq(() -> {
            var result = seq.filter(x -> !seen.contains(x));
            if (result.isEmpty()) {
                return nil();
            }
            var first = result.first();
            return step(result.rest(), conj(seen, first)).cons(first);
        });
    }

    @SuppressWarnings("unchecked")
    public static <T> ISeq<T> sort(ISeq<T> seq) {
        return sort((o1, o2) -> ((Comparable<T>) o1).compareTo(o2), seq);
    }

    public static <T> ISeq<T> sort(Comparator<? super T> comp, ISeq<T> seq) {
        var result = new ArrayList<>(seq);
        result.sort(comp);
        return seq(result);
    }

    public static <T> ISeq<T> reverse(ISeq<T> seq) {
        var iter = seq.iterator();
        var acc = ISeq.<T>of();
        while(iter.hasNext()) {
            acc = cons(iter.next(), acc);
        }
        return acc;
    }

    public static <T> boolean some(Predicate<? super T> pred, ISeq<T> seq) {
        return !seq.isEmpty() && (pred.test(seq.first()) || seq.rest().some(pred));
    }

    public static <T> boolean every(Predicate<? super T> pred, ISeq<T> seq) {
        return seq.isEmpty() || pred.test(seq.first()) && seq.rest().every(pred);
    }

    public static <T> boolean notAny(Predicate<? super T> pred, ISeq<T> seq) {
        return seq.isEmpty() || seq.every(pred.negate());
    }

    public static <T> Optional<T> max(Comparator<? super T> comp, ISeq<T> seq) {
        if (seq.isEmpty()) {
            return Optional.empty();
        }
        if (seq.rest().isEmpty()) {
            return Optional.of(seq.first());
        }
        var result = seq.first();
        var s = seq.rest();
        while (!s.isEmpty()) {
            var next = s.first();
            result = comp.compare(result, next) > 0 ? result : next;
            s = s.rest();
        }
        return Optional.of(result);
    }

    public static <T> Optional<T> min(Comparator<? super T> comp, ISeq<T> seq) {
        return seq.max(comp.reversed());
    }

    public static <T, C extends Comparable<? super C>> Optional<T> maxKey(Function<T, C> f, ISeq<T> seq) {
        return max(Comparator.comparing(t -> f.apply(t)), seq);
    }

    public static <T, C extends Comparable<? super C>> Optional<T> minKey(Function<T, C> f, ISeq<T> seq) {
        return min(Comparator.comparing(t -> f.apply(t)), seq);
    }

    public static <T> T nth(ISeq<T> seq, int index) {
        var result = seq.nth(index, null);
        if (result == null) {
            throw new IndexOutOfBoundsException(index);
        }
        return result;
    }

    public static <T> T nth(ISeq<T> seq, int index, T notFound) {
        if (index < 0 || seq.isEmpty()) {
            return notFound;
        }
        ISeq<T> s = seq;
        for (int i = index; i > 0; --i) {
            if (s.rest().isEmpty()) {
                return notFound;
            }
            s = s.rest();
        }
        return s.first();
    }






    private static final int CHUNK_SIZE = 32;
    public static <T> ISeq<T> chunkIteratorSeq(Iterator<T> iterator) {
        if(iterator.hasNext()) {
            return new LazySeq<>(() -> {
                T[] arr = (T[]) new Object[CHUNK_SIZE];
                int n = 0;
                while(iterator.hasNext() && n < CHUNK_SIZE) {
                    arr[n++] = iterator.next();
                }
                return new ChunkedCons<>(new ArrayChunk<>(arr, 0, n), chunkIteratorSeq(iterator));
            });
        }
        return Nil.empty();
    }
}
