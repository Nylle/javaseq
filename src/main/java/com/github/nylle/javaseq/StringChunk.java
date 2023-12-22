package com.github.nylle.javaseq;

public class StringChunk implements IChunk<Character> {

    private final CharSequence str;
    private final int offset;
    private final int end;

    StringChunk(CharSequence str, int offset, int end) {
        this.str = str;
        this.offset = offset;
        this.end = end;
    }

    @Override
    public Character nth(int n) {
        if (n < 0 || (offset + n) >= end) {
            throw new IndexOutOfBoundsException("Index " + n + " out of bounds for length " + count());
        }
        return str.charAt(offset + n);
    }

    @Override
    public IChunk<Character> dropFirst() {
        return new StringChunk(str, offset + 1, end);
    }

    @Override
    public IChunk<Character> dropLast(int n) {
        return new StringChunk(str, offset, end - n);
    }

    @Override
    public int count() {
        return end - offset;
    }
}
