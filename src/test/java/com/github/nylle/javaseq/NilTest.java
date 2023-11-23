package com.github.nylle.javaseq;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class NilTest {

    @Test
    void firstThrows() {
        assertThatExceptionOfType(NoSuchElementException.class)
                .isThrownBy(() -> Nil.of().first())
                .withMessage("first");
    }

    @Test
    void restThrows() {
        assertThatExceptionOfType(NoSuchElementException.class)
                .isThrownBy(() -> Nil.of().rest())
                .withMessage("rest");
    }

    @Test
    void getThrows() {
        assertThatExceptionOfType(IndexOutOfBoundsException.class)
                .isThrownBy(() -> Nil.of().get(0))
                .withMessage("Index out of range: 0");
    }

    @Test
    void sizeReturnsZero() {
        assertThat(Nil.of().size()).isZero();
    }

    @Test
    void isEmptyReturnsTrue() {
        assertThat(Nil.of().isEmpty()).isTrue();
    }

    @Nested
    class Take {

        @Test
        void returnsNilWithNegativeItems() {
            var sut = Nil.of();

            assertThat(sut.take(-1)).isEmpty();
            assertThat(sut.take(-1)).isExactlyInstanceOf(Nil.class);
        }

        @Test
        void returnsNilWithZeroItems() {
            var sut = Nil.of();

            assertThat(sut.take(0)).isEmpty();
            assertThat(sut.take(0)).isExactlyInstanceOf(Nil.class);
        }

        @Test
        void returnsNilForMoreThanZeroItems() {
            var sut = Nil.of();

            assertThat(sut.take(3)).isEmpty();
            assertThat(sut.take(3)).isExactlyInstanceOf(Nil.class);
        }
    }
}