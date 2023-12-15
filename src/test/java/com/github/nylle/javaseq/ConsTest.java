package com.github.nylle.javaseq;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConsTest {

    private static <T> ISeq<T> sutFrom(T... items) {
        ISeq<T> cons = Nil.empty();
        for (int i = items.length - 1; i >= 0; i--) {
            cons = new Cons<>(items[i], cons);
        }
        return cons;
    }

    @Test
    void firstReturnsFirstItem() {
        var sut = new Cons<>(0, new Cons<>(1, new Cons<>(2, new Cons<>(3, Nil.empty()))));

        assertThat(sut.first()).isEqualTo(0);
    }

    @Test
    void restReturnsSeqWithItemsExceptFirst() {
        var sut = new Cons<>(0, new Cons<>(1, new Cons<>(2, new Cons<>(3, Nil.empty()))));

        var rest = sut.rest();

        assertThat(rest.first()).isEqualTo(1);
        assertThat(rest.rest().first()).isEqualTo(2);
        assertThat(rest.rest().rest().first()).isEqualTo(3);
        assertThat(rest.rest().rest().rest()).isEmpty();
    }

    @Nested
    class IsRealized {

        @Test
        void returnsTrueForSingleItem() {
            var sut = sutFrom(0);

            assertThat(sut.isRealized()).isTrue();
        }

        @Test
        void returnsTrueForAnyNumberOfItems() {
            var sut = sutFrom(1, 2, 3, 4);

            assertThat(sut.isRealized()).isTrue();
        }
    }

    @Test
    void seqReturnsRealizedSeq() {
        var sut = sutFrom(1, 2, 3);
        assertThat(sut.isRealized()).isTrue();

        var forced = sut.seq();
        assertThat(sut.isRealized()).isTrue();
        assertThat(forced).isSameAs(sut);
        assertThat(forced.isRealized()).isTrue();
    }
}