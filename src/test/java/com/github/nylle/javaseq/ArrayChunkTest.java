package com.github.nylle.javaseq;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class ArrayChunkTest {

    private static <T> ArrayChunk<T> from(T... items) {
        return new ArrayChunk<>(items);
    }

    @Test
    void nth() {
        var sut = from(-1, 0, 1, 2, 3, 4).dropFirst().dropLast(1);

        assertThat(sut.nth(0)).isEqualTo(0);
        assertThat(sut.nth(1)).isEqualTo(1);
        assertThat(sut.nth(2)).isEqualTo(2);
        assertThat(sut.nth(3)).isEqualTo(3);

        assertThatExceptionOfType(IndexOutOfBoundsException.class)
                .isThrownBy(() -> sut.nth(4))
                .withMessage("Index 4 out of bounds for length 4");

        assertThatExceptionOfType(IndexOutOfBoundsException.class)
                .isThrownBy(() -> sut.nth(-1))
                .withMessage("Index -1 out of bounds for length 4");
    }

    @Test
    void dropFirst() {
        var actual = from(0, 1, 2, 3, 4).dropFirst();

        assertThat(actual.nth(0)).isEqualTo(1);
        assertThat(actual.nth(3)).isEqualTo(4);
    }

    @Test
    void dropLast() {
        var actual = from(0, 1, 2, 3, 4).dropLast(2);

        assertThat(actual.nth(0)).isEqualTo(0);
        assertThat(actual.nth(2)).isEqualTo(2);
        assertThatExceptionOfType(IndexOutOfBoundsException.class)
                .isThrownBy(() -> actual.nth(3))
                .withMessage("Index 3 out of bounds for length 3");
    }

    @Test
    void count() {
        assertThat(from(-1, 0, 1, 2, 3, 4).count()).isEqualTo(6);
        assertThat(from(-1, 0, 1, 2, 3, 4).dropFirst().count()).isEqualTo(5);
        assertThat(from(-1, 0, 1, 2, 3, 4).dropLast(1).count()).isEqualTo(5);
        assertThat(from(-1, 0, 1, 2, 3, 4).dropFirst().dropLast(2).count()).isEqualTo(3);
    }
}