package com.github.nylle.javaseq;

public class ArrayChunk<T> implements IChunk<T> {

    private final T[] array;
    private final int offset;
    private final int end;

    ArrayChunk(T[] array) {
        this.array = array;
        this.offset = 0;
        this.end = array.length;
    }

    ArrayChunk(T[] array, int offset, int end) {
        this.array = array;
        this.offset = offset;
        this.end = end;
    }

    @Override
    public T nth(int n) {
        if (n < 0 || (offset + n) >= end) {
            throw new IndexOutOfBoundsException("Index " + n + " out of bounds for length " + count());
        }
        return array[offset + n];
    }

    @Override
    public IChunk<T> dropFirst() {
        return new ArrayChunk<>(array, offset + 1, end);
    }

    @Override
    public IChunk<T> dropLast(int n) {
        return new ArrayChunk<>(array, offset, end - n);
    }

    @Override
    public int count() {
        return end - offset;
    }
}
