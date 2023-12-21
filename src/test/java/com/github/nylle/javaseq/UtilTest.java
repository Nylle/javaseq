package com.github.nylle.javaseq;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UtilTest {

    @Nested
    @DisplayName("nil")
    class NilTest {

        @Test
        void returnsEmptySeq() {
            var actual = Util.nil();

            assertThat(actual)
                    .isExactlyInstanceOf(Nil.class)
                    .isEmpty();
        }
    }

    @Nested
    @DisplayName("cons")
    class ConsTest {

        @Test
        void returnsSeqFromFirstElementAndSeq() {
            var actual = Util.cons("a", Util.cons("b", Util.cons("c", Nil.empty())));

            assertThat(actual)
                    .isExactlyInstanceOf(Cons.class)
                    .containsExactly("a", "b", "c");
        }

        @Test
        void returnsSeqWithNulls() {
            var sut = Util.cons(null, Util.cons(null, Util.cons(null, Nil.empty())));

            assertThat(sut).containsExactly(null, null, null);
        }
    }

    @Nested
    @DisplayName("lazySeq")
    class LazySeqTest {

        @Test
        void returnsSeqFromSupplier() {
            var actual = Util.lazySeq(() ->
                    Util.cons("a", Util.lazySeq(() ->
                            Util.cons("b", Util.lazySeq(() ->
                                    Util.cons("c", Util.lazySeq(() ->
                                            Util.nil())))))));

            assertThat(actual)
                    .isInstanceOf(LazySeq.class)
                    .containsExactly("a", "b", "c");
        }

        @Test
        void returnsSeqWithNulls() {
            var sut = Util.lazySeq(() ->
                    Util.cons(null, Util.lazySeq(() ->
                            Util.cons(null, Util.lazySeq(() ->
                                    Util.cons(null, Util.lazySeq(() ->
                                            Util.nil())))))));

            assertThat(sut)
                    .isExactlyInstanceOf(LazySeq.class)
                    .containsExactly(null, null, null);
        }
    }

    @Nested
    @DisplayName("arraySeq")
    class ArraySeqTest {

        @Test
        void returnsSeqWithSuppliedItems() {
            var sut = Util.arraySeq(new Integer[] {0, 1, 2, 3});

            assertThat(sut)
                    .isExactlyInstanceOf(ArraySeq.class)
                    .containsExactly(0, 1, 2, 3);
        }

        @Test
        void returnsSeqWithNulls() {
            var sut = Util.arraySeq(new Integer[] {null, null, null});

            assertThat(sut)
                    .isExactlyInstanceOf(ArraySeq.class)
                    .containsExactly(null, null, null);
        }
    }

    @Nested
    @DisplayName("conj")
    class ConjTest {

        @Test
        void returnsNewListWithXAdded() {
            assertThat(Util.conj(List.of(1), 2)).isInstanceOf(List.class).containsExactly(1, 2);
        }

        @Test
        void returnsNewSetWithXAdded() {
            assertThat(Util.conj(Set.of(1), 2)).isInstanceOf(Set.class).containsExactlyInAnyOrder(1, 2);
        }
    }
}