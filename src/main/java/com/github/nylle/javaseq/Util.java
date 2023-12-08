package com.github.nylle.javaseq;

import java.util.List;

public class Util {

    private Util() {
    }

    public static <T> ArrayChunk<T> arrayChunk(List<T> list) {
        return new ArrayChunk<>((T[])list.toArray(new Object[0]), 0, list.size());
    }
}
