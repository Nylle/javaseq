package com.github.nylle.javaseq;

import java.util.List;

public class ArrayChunk<T> implements IChunk<T> {

    private final T[] array;
    private final int offset;
    private final int end;

    ArrayChunk(T[] array, int offset, int end) {
        this.array = array;
        this.offset = offset;
        this.end = end;
    }

    static <T> ArrayChunk<T> from(List<T> list) {
        return new ArrayChunk<>((T[])list.toArray(new Object[0]), 0, list.size());
    }

    @Override
    public T nth(int n) {
        return array[offset + n];
    }

    @Override
    public IChunk<T> dropFirst() {
        return new ArrayChunk<>(array, offset + 1, end);
    }

    @Override
    public int count() {
        return end - offset;
    }
}
