package com.github.nylle.javaseq;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

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
    void sizeReturnsZero() {
        assertThat(Nil.of().size()).isZero();
    }

    @Test
    void isEmptyReturnsTrue() {
        assertThat(Nil.of().isEmpty()).isTrue();
    }

    @Test
    void getReturnsNull() {
        assertThat(Nil.of().get(0)).isNull();
    }

    @Nested
    class Take {

        @Test
        void returnsNilWithNegativeItems() {
            var sut = Nil.of();

            assertThat(sut.take(-1)).isEqualTo(Nil.of());
        }

        @Test
        void returnsNilWithZeroItems() {
            var sut = Nil.of();

            assertThat(sut.take(0)).isEqualTo(Nil.of());
        }

        @Test
        void returnsNilForMoreThanZeroItems() {
            var sut = Nil.of();

            assertThat(sut.take(3)).isEqualTo(Nil.of());
        }
    }

    @Nested
    class Drop {

        @Test
        void returnsNilWithNegativeItems() {
            assertThat(Nil.of().drop(-1)).isEqualTo(Nil.of());
        }

        @Test
        void returnsNilWithZeroItems() {
            assertThat(Nil.of().drop(0)).isEqualTo(Nil.of());
        }

        @Test
        void returnsNilForMoreThanZeroItems() {
            assertThat(Nil.of().drop(12)).isEqualTo(Nil.of());
        }
    }

    @Test
    void filterReturnsNil() {
        assertThat(Nil.<Integer>of().filter(x -> x != null)).isEqualTo(Nil.of());
    }

    @Test
    void mapReturnsNil() {
        assertThat(Nil.<Integer>of().map(x -> x * 100)).isEqualTo(Nil.of());
    }

    @Test
    void mapcatReturnsNil() {
        assertThat(Nil.<Integer>of().mapcat(x -> List.of(x, x))).isEqualTo(Nil.of());
    }

    @Test
    void takeWhileReturnsNil() {
        assertThat(Nil.of().takeWhile(x -> true)).isEqualTo(Nil.of());
    }

    @Test
    void dropWhileReturnsNil() {
        assertThat(Nil.of().dropWhile(x -> true)).isEqualTo(Nil.of());
    }
}