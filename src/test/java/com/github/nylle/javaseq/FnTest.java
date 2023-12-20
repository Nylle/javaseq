package com.github.nylle.javaseq;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
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
    @DisplayName("iterate")
    class IterateTest {

        @Test
        void returnsSeqWithNulls() {
            var sut = Fn.iterate(null, x -> null);

            assertThat(sut.take(4)).containsExactly(null, null, null, null);
        }

        @Test
        void returnsSeqOfInitialValueUsingFunction() {
            var actual = Fn.iterate(0, x -> x + 1);

            assertThat(actual.take(4))
                    .containsExactly(0, 1, 2, 3);
        }
    }

    @Nested
    @DisplayName("repeat")
    class RepeatTest {

        @Test
        void returnsSeqWithNulls() {
            var sut = Fn.repeat(null);

            assertThat(sut.take(4)).containsExactly(null, null, null, null);
        }

        @Test
        void returnsInfiniteSeqOfXs() {
            var actual = Fn.repeat(99);

            assertThat(actual.take(4))
                    .containsExactly(99, 99, 99, 99);
        }

        @Test
        void returnsSeqOfXsWithLengthN() {
            var actual = Fn.repeat(4, "bar");

            assertThat(actual).containsExactly("bar", "bar", "bar", "bar");
        }
    }

    @Nested
    @DisplayName("range")
    class RangeTest {

        @Test
        void returnsInfiniteSeqOfIntegersStartingWithZero() {
            assertThat(Fn.range().take(3))
                    .isInstanceOf(LazySeq.class)
                    .containsExactly(0, 1, 2);
        }

        @Test
        void returnsSeqOfIntegersStartingWithZeroUntilEnd() {
            assertThat(Fn.range(3))
                    .isInstanceOf(LazySeq.class)
                    .containsExactly(0, 1, 2);
        }

        @Test
        void returnsSeqOfIntegersFromStartInclusiveUntilEndExclusive() {
            assertThat(Fn.range(1, 5)).isInstanceOf(LazySeq.class).containsExactly(1, 2, 3, 4);
            assertThat(Fn.range(-5, 5)).isInstanceOf(LazySeq.class).containsExactly(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4);
        }

        @Test
        void returnsSeqOfIntegersFromStartInclusiveUntilEndExclusiveByStep() {
            assertThat(Fn.range(10, 25, 5)).isInstanceOf(LazySeq.class).containsExactly(10, 15, 20);
            assertThat(Fn.range(10, -25, -5)).isInstanceOf(LazySeq.class).containsExactly(10, 5, 0, -5, -10, -15, -20);
            assertThat(Fn.range(-10, 25, 5)).isInstanceOf(LazySeq.class).containsExactly(-10, -5, 0, 5, 10, 15, 20);
        }

        @Test
        void returnsInfiniteSeqOfStartWhenStepIsZero() {
            assertThat(Fn.range(10, 25, 0).take(10))
                    .isInstanceOf(LazySeq.class)
                    .containsExactly(10, 10, 10, 10, 10, 10, 10, 10, 10, 10);
        }

        @Test
        void returnsEmptySeqWhenStartIsEqualToEnd() {
            assertThat(Fn.range(10, 10)).isEqualTo(Nil.empty());
            assertThat(Fn.range(-10, -10)).isEqualTo(Nil.empty());
            assertThat(Fn.range(1, 1, 1)).isEqualTo(Nil.empty());
            assertThat(Fn.range(-1, -1, 1)).isEqualTo(Nil.empty());
        }
    }

    @Nested
    @DisplayName("seq")
    class SeqTest {

        @Nested
        @DisplayName("StringSeq")
        class StringSeqTest {

            @Test
            void isCreatedFromString() {
                assertThat(Fn.seq("foo")).isExactlyInstanceOf(StringSeq.class).containsExactly('f', 'o', 'o');
            }

            @Test
            void isCreatedFromCharArray() {
                assertThat(Fn.seq("foo".toCharArray())).isExactlyInstanceOf(StringSeq.class).containsExactly('f', 'o', 'o');
            }

            @Test
            void isCreatedFromCharacterArray() {
                assertThat(Fn.seq(new Character[]{'f', 'o', 'o'})).isExactlyInstanceOf(StringSeq.class).containsExactly('f', 'o', 'o');
            }
        }

        @Nested
        @DisplayName("ArraySeq")
        class ArraySeqTest {

            @Test
            void isCreatedFromArray() {
                var actual = Fn.seq(new Integer[]{0, 1, 2});

                assertThat(actual).isExactlyInstanceOf(ArraySeq.class).containsExactly(0, 1, 2);
            }

            @Test
            void isCreatedFromArrayList() {
                var actual = Fn.seq(new ArrayList<>(List.of("a", "b", "c")));

                assertThat(actual).isExactlyInstanceOf(ArraySeq.class).containsExactly("a", "b", "c");
            }
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