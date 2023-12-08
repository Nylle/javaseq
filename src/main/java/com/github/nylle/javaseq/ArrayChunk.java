package com.github.nylle.javaseq;

public class ArrayChunk<T> implements IChunk<T> {

    private final T[] array;
    private final int offset;
    private final int end;

    ArrayChunk(T[] array, int offset, int end) {
        this.array = array;
        this.offset = offset;
        this.end = end;
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
