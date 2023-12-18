package com.github.nylle.javaseq;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FnTest {

    @Nested
    @DisplayName("nil")
    class NilTest {

        @Test
        void returnsEmptySeq() {
            var actual = Fn.nil();

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
            var actual = Fn.cons("a", Fn.cons("b", Fn.cons("c", Nil.empty())));

            assertThat(actual)
                    .isExactlyInstanceOf(Cons.class)
                    .containsExactly("a", "b", "c");
        }

        @Test
        void returnsSeqWithNulls() {
            var sut = Fn.cons(null, Fn.cons(null, Fn.cons(null, Nil.empty())));

            assertThat(sut).containsExactly(null, null, null);
        }
    }

    @Nested
    @DisplayName("lazySeq")
    class LazySeqTest {

        @Test
        void returnsSeqFromSupplier() {
            var actual = Fn.lazySeq(() ->
                    Fn.cons("a", Fn.lazySeq(() ->
                            Fn.cons("b", Fn.lazySeq(() ->
                                    Fn.cons("c", Fn.lazySeq(() ->
                                            Fn.nil())))))));

            assertThat(actual)
                    .isInstanceOf(LazySeq.class)
                    .containsExactly("a", "b", "c");
        }

        @Test
        void returnsSeqWithNulls() {
            var sut = Fn.lazySeq(() ->
                    Fn.cons(null, Fn.lazySeq(() ->
                            Fn.cons(null, Fn.lazySeq(() ->
                                    Fn.cons(null, Fn.lazySeq(() ->
                                            Fn.nil())))))));

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
            var sut = Fn.arraySeq(0, 1, 2, 3, 4, 5);

            assertThat(sut)
                    .isExactlyInstanceOf(ArraySeq.class)
                    .containsExactly(0, 1, 2, 3, 4, 5);
        }

        @Test
        void returnsSeqWithNulls() {
            var sut = Fn.arraySeq(null, null, null);

            assertThat(sut)
                    .isExactlyInstanceOf(ArraySeq.class)
                    .containsExactly(null, null, null);
        }
    }

    @Nested
    @DisplayName("concat")
    class Concat {

        @Test
        void returnsSeqFromConcatenatingMultipleSeqsWithNulls() {
            var sut = Fn.concat(ISeq.of(null, null), ISeq.of(null, null), ISeq.of(null, null));

            assertThat(sut).containsExactly(null, null, null, null, null, null);
        }

        @Test
        void returnsSeqFromConcatenatingMultipleSeqs() {
            var actual = Fn.concat(ISeq.of("a", "b"), ISeq.of("c", "d"), ISeq.of("e", "f"));

            assertThat(actual)
                    .isInstanceOf(LazySeq.class)
                    .containsExactly("a", "b", "c", "d", "e", "f");
        }

        @Test
        void returnsSeqFromConcatenatingMultipleIterables() {
            var actual = Fn.concat(List.of("a", "b"), List.of("c", "d"), List.of("e", "f"));

            assertThat(actual)
                    .isInstanceOf(LazySeq.class)
                    .containsExactly("a", "b", "c", "d", "e", "f");
        }

        @Test
        void isLazy() {
            assertThat(Fn.concat(ISeq.of("a", "b"), ISeq.of("c", "d"), ISeq.iterate("e", x -> x + "e")).take(7))
                    .containsExactly("a", "b", "c", "d", "e", "ee", "eee");

            assertThat(Fn.concat(ISeq.of("a", "b"), ISeq.iterate("c", x -> x + "c"), ISeq.of("e", "f")).take(7))
                    .containsExactly("a", "b", "c", "cc", "ccc", "cccc", "ccccc");

            assertThat(Fn.concat(ISeq.iterate("a", x -> x + "a"), ISeq.of("c", "d"), ISeq.of("e", "f")).take(7))
                    .containsExactly("a", "aa", "aaa", "aaaa", "aaaaa", "aaaaaa", "aaaaaaa");
        }
    }
}