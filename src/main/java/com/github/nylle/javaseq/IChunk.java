package com.github.nylle.javaseq;

public interface IChunk<T> {

    T nth(int n);

    IChunk<T> dropFirst();

    IChunk<T> dropLast(int n);

    int count();
}
