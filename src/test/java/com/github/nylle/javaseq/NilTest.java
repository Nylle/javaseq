package com.github.nylle.javaseq;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NilTest {

    @Test
    void firstReturnsNull() {
        assertThat(Nil.of().first()).isNull();
    }

    @Test
    void restReturnsNil() {
        assertThat(Nil.of().rest()).isEqualTo(Nil.of());
    }

    @Test
    void getReturnsNull() {
        assertThat(Nil.of().get(0)).isNull();
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

            assertThat(sut.take(-1))
                    .isExactlyInstanceOf(Nil.class)
                    .isEmpty();
        }

        @Test
        void returnsNilWithZeroItems() {
            var sut = Nil.of();

            assertThat(sut.take(0))
                    .isExactlyInstanceOf(Nil.class)
                    .isEmpty();
        }

        @Test
        void returnsNilForMoreThanZeroItems() {
            var sut = Nil.of();

            assertThat(sut.take(3))
                    .isExactlyInstanceOf(Nil.class)
                    .isEmpty();
        }
    }
}