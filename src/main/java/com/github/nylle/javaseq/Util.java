package com.github.nylle.javaseq;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Util {

    private static final int CHUNK_SIZE = 32;

    private Util() {
    }

    public static <T> ISeq<T> chunkIteratorSeq(Iterator<T> iterator) {
        if(iterator.hasNext()) {
            return new LazySeq2<>(() -> {
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

    public static <T> Set<T> conj(Set<T> seen, T x) {
        var result = new HashSet<>(seen);
        result.add(x);
        return result;
    }
}
