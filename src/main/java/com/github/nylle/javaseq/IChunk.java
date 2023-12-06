package com.github.nylle.javaseq;

public interface IChunk<T> {

    T nth(int n);

    IChunk<T> dropFirst();

    int count();
}
