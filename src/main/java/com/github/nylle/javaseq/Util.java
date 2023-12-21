package com.github.nylle.javaseq;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class Util {

    private Util() {
    }

    private static final int CHUNK_SIZE = 32;

    public static <T> ISeq<T> nil() {
        return Nil.empty();
    }

    public static <T> ISeq<T> cons(T x, Iterable<T> seq) {
        return new Cons<>(x, ISeq.seq(seq));
    }

    public static <T> ISeq<T> lazySeq(Supplier<ISeq<T>> f) {
        return new LazySeq<>(f);
    }

    public static <T> ISeq<T> arraySeq(T[] array) {
        if (array != null && array.length > 0) return new ArraySeq<>(array);
        return nil();
    }

    public static ISeq<Character> stringSeq(CharSequence coll) {
        return new StringSeq(coll, 0, coll.length());
    }

    public static <T> ISeq<T> chunkIteratorSeq(final Iterator<T> iterator) {
        if (iterator.hasNext()) {
            return lazySeq(() -> {
                T[] arr = (T[]) new Object[CHUNK_SIZE];
                int n = 0;
                while (iterator.hasNext() && n < CHUNK_SIZE) {
                    arr[n++] = iterator.next();
                }
                return new ChunkedCons<>(new ArrayChunk<>(arr, 0, n), chunkIteratorSeq(iterator));
            });
        }
        return nil();
    }

    public static <T> ISeq<T> concat(Iterator<? extends T> iterator, ISeq<T> seq) {
        return Util.lazySeq(() -> {
            if (iterator.hasNext()) {
                return Util.cons(iterator.next(), concat(iterator, seq));
            }
            return seq;
        });
    }

    @SafeVarargs
    public static <T> Set<T> conj(Set<T> coll, T... xs) {
        var result = new HashSet<>(coll);
        result.addAll(Arrays.asList(xs));
        return Set.copyOf(result);
    }

    @SafeVarargs
    public static <T> List<T> conj(List<T> coll, T... xs) {
        var result = new ArrayList<>(coll);
        result.addAll(Arrays.asList(xs));
        return List.copyOf(result);
    }
}
