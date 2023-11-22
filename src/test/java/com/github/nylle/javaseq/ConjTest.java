package com.github.nylle.javaseq;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.github.nylle.javaseq.Seq.conj;
import static com.github.nylle.javaseq.Seq.of;
import static org.assertj.core.api.Assertions.assertThat;

class ConjTest {

    @Test
    void firstReturnsHead() {
        var sut = conj(0, of(1));

        assertThat(sut.first()).isEqualTo(0);
    }

    @Test
    void restReturnsTail() {
        var sut = conj(0, conj(1, conj(2, of(3))));

        var rest = sut.rest();

        assertThat(rest.first()).isEqualTo(1);
        assertThat(rest.rest().first()).isEqualTo(2);
        assertThat(rest.rest().rest().first()).isEqualTo(3);
    }

    @Test
    void getReturnsValueAtIndex() {
        var sut = conj(0, conj(1, conj(2, of(3))));

        assertThat(sut.get(0)).isEqualTo(0);
        assertThat(sut.get(1)).isEqualTo(1);
        assertThat(sut.get(2)).isEqualTo(2);
        assertThat(sut.get(3)).isEqualTo(3);
    }

    @Test
    void sizeReturnsSizeOfConj() {
        var sut = conj(0, conj(1, conj(2, of(3))));

        assertThat(sut.size()).isEqualTo(4);
    }

    @Test
    void isEmptyReturnsFalse() {
        var sut = conj(0, of(2));

        assertThat(sut.isEmpty()).isFalse();
    }

    @Nested
    class Take {

        @Test
        void returnsNilWithNegativeItems() {
            var sut = conj(0, conj(1, of(2)));

            assertThat(sut.take(-1)).isEmpty();
            assertThat(sut.take(-1)).isExactlyInstanceOf(Nil.class);
        }

        @Test
        void returnsNilWithZeroItems() {
            var sut = conj(0, conj(1, of(2)));

            assertThat(sut.take(0)).isEmpty();
            assertThat(sut.take(0)).isExactlyInstanceOf(Nil.class);
        }

        @Test
        void returnsConjWithOneItem() {
            var sut = conj(0, conj(1, of(2)));

            assertThat(sut.take(1)).containsExactly(0);
            assertThat(sut.take(1)).isExactlyInstanceOf(Conj.class);
        }

        @Test
        void returnsConjWithMoreThanOneItem() {
            var sut = conj(0, conj(1, of(2)));

            assertThat(sut.take(3)).containsExactly(0, 1, 2);
            assertThat(sut.take(3)).isExactlyInstanceOf(Conj.class);
        }

        @Test
        void returnsEntireConjIfShorter() {
            var sut = conj(0, conj(1, of(2)));

            assertThat(sut.take(10)).containsExactly(0, 1, 2);
            assertThat(sut.take(10)).isExactlyInstanceOf(Conj.class);
        }
    }
}