package com.github.nylle.javaseq;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

class ISeqTest {

    @Nested
    @DisplayName("of")
    class Of {

        @Test
        void returnsSeqWithNulls() {
            var sut = ISeq.of(null, null, null);

            assertThat(sut).containsExactly(null, null, null);
        }

        @Test
        void returnsEmptySeq() {
            assertThat(ISeq.of())
                    .isInstanceOf(Nil.class)
                    .isEmpty();
        }

        @Test
        void returnsSeqOfSingleItem() {
            assertThat(ISeq.of(1))
                    .isInstanceOf(Cons.class)
                    .containsExactly(1);
        }

        @Test
        void returnsSeqOfSuppliedItems() {
            assertThat(ISeq.of(1, 2, 3))
                    .isInstanceOf(Cons.class)
                    .containsExactly(1, 2, 3);
        }

        @Test
        void returnsSeqOfSuppliedLists() {
            assertThat(ISeq.of(List.of(1, 2), List.of("foo", "bar"), List.of(true, false)))
                    .isInstanceOf(Cons.class)
                    .containsExactly(List.of(1, 2), List.of("foo", "bar"), List.of(true, false));
        }
    }

    @Nested
    @DisplayName("sequence")
    class Sequence {

        @Test
        void returnsSeqWithNulls() {
            var list = new ArrayList<String>();
            list.add(null);
            list.add(null);
            list.add(null);

            assertThat(ISeq.sequence(list))
                    .isInstanceOf(LazySeq.class)
                    .containsExactly(null, null, null);
        }

        @Test
        void returnsSeqOfItemsInIterable() {
            var list = List.of(1, 2, 3);

            assertThat(ISeq.sequence(list))
                    .isInstanceOf(LazySeq.class)
                    .containsExactly(1, 2, 3);
        }

        @Test
        void returnsSeqOfItemsOfIterator() {
            var infiniteIterator = Stream.iterate(0, x -> x + 1).iterator();

            assertThat(ISeq.sequence(infiniteIterator).take(4))
                    .isInstanceOf(LazySeq.class)
                    .containsExactly(0, 1, 2, 3);
        }

        @Test
        void returnsSeqOfItemsInStream() {
            var infiniteStream = Stream.iterate(0, x -> x + 1);

            assertThat(ISeq.sequence(infiniteStream).take(4))
                    .isInstanceOf(LazySeq.class)
                    .containsExactly(0, 1, 2, 3);
        }

        @Test
        void returnsSeqOfItemsInArray() {
            var array = new Integer[]{1, 2, 3};

            assertThat(ISeq.sequence(array))
                    .isInstanceOf(LazySeq.class)
                    .containsExactly(1, 2, 3);
        }

        @Test
        void returnsSeqOfKeyValuePairsInMap() {
            var map = Map.of("a", 1, "b", 2, "c", 3);

            assertThat(ISeq.sequence(map))
                    .isInstanceOf(LazySeq.class)
                    .containsExactlyInAnyOrder(entry("a", 1), entry("b", 2), entry("c", 3));
        }

        @Test
        void returnsSeqOfCharactersInString() {
            var actual = ISeq.sequence("foo");

            assertThat(actual)
                    .isInstanceOf(StringSeq.class)
                    .containsExactly('f', 'o', 'o');
        }

        @Test
        void returnsEmptySeqIfCollIsNull() {
            assertThat(ISeq.sequence((Iterable<Integer>) null)).isInstanceOf(Nil.class).isEmpty();
            assertThat(ISeq.sequence((Stream<Integer>) null)).isInstanceOf(Nil.class).isEmpty();
            assertThat(ISeq.sequence((Iterator<Integer>) null)).isInstanceOf(Nil.class).isEmpty();
            assertThat(ISeq.sequence((Map<String, Integer>) null)).isInstanceOf(Nil.class).isEmpty();
            assertThat(ISeq.sequence((String) null)).isInstanceOf(Nil.class).isEmpty();
        }

        @Test
        void returnsEmptySeqIfCollIsEmpty() {
            assertThat(ISeq.sequence(List.<Integer>of())).isInstanceOf(Nil.class).isEmpty();
            assertThat(ISeq.sequence(Stream.<Integer>of())).isInstanceOf(Nil.class).isEmpty();
            assertThat(ISeq.sequence(Collections.<Integer>emptyIterator())).isInstanceOf(Nil.class).isEmpty();
            assertThat(ISeq.sequence(Map.<String, Integer>of())).isInstanceOf(Nil.class).isEmpty();
            assertThat(ISeq.sequence("")).isInstanceOf(Nil.class).isEmpty();
        }

        @Test
        void returnsCollIfAlreadyASeq() {
            var coll = ISeq.of(1, 2, 3);
            assertThat(ISeq.sequence(coll)).isSameAs(coll);
        }

        @Test
        void doesNotForceLazyColl() {
            assertThat(ISeq.sequence(Stream.iterate(0, x -> x + 1)).take(3))
                    .isInstanceOf(LazySeq.class)
                    .containsExactly(0, 1, 2);
            assertThat(ISeq.sequence(Stream.iterate(0, x -> x + 1).iterator()).take(3))
                    .isInstanceOf(LazySeq.class)
                    .containsExactly(0, 1, 2);
        }
    }

    @Nested
    @DisplayName("iterate")
    class Iterate {

        @Test
        void returnsSeqWithNulls() {
            var sut = ISeq.iterate(null, x -> null);

            assertThat(sut.take(4)).containsExactly(null, null, null, null);
        }

        @Test
        void returnsSeqOfInitialValueUsingFunction() {
            var actual = ISeq.iterate(0, x -> x + 1);

            assertThat(actual.take(4))
                    .containsExactly(0, 1, 2, 3);
        }
    }

    @Nested
    @DisplayName("range")
    class Range {

        @Test
        void returnsInfiniteSeqOfIntegersStartingWithZero() {
            assertThat(ISeq.range().take(3))
                    .isInstanceOf(LazySeq.class)
                    .containsExactly(0, 1, 2);
        }

        @Test
        void returnsSeqOfIntegersStartingWithZeroUntilEnd() {
            assertThat(ISeq.range(3))
                    .isInstanceOf(LazySeq.class)
                    .containsExactly(0, 1, 2);
        }

        @Test
        void returnsSeqOfIntegersFromStartInclusiveUntilEndExclusive() {
            assertThat(ISeq.range(1, 5)).isInstanceOf(LazySeq.class).containsExactly(1, 2, 3, 4);
            assertThat(ISeq.range(-5, 5)).isInstanceOf(LazySeq.class).containsExactly(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4);
        }

        @Test
        void returnsSeqOfIntegersFromStartInclusiveUntilEndExclusiveByStep() {
            assertThat(ISeq.range(10, 25, 5)).isInstanceOf(LazySeq.class).containsExactly(10, 15, 20);
            assertThat(ISeq.range(10, -25, -5)).isInstanceOf(LazySeq.class).containsExactly(10, 5, 0, -5, -10, -15, -20);
            assertThat(ISeq.range(-10, 25, 5)).isInstanceOf(LazySeq.class).containsExactly(-10, -5, 0, 5, 10, 15, 20);
        }

        @Test
        void returnsInfiniteSeqOfStartWhenStepIsZero() {
            assertThat(ISeq.range(10, 25, 0).take(10))
                    .isInstanceOf(LazySeq.class)
                    .containsExactly(10, 10, 10, 10, 10, 10, 10, 10, 10, 10);
        }

        @Test
        void returnsEmptySeqWhenStartIsEqualToEnd() {
            assertThat(ISeq.range(10, 10)).isEqualTo(Nil.empty());
            assertThat(ISeq.range(-10, -10)).isEqualTo(Nil.empty());
            assertThat(ISeq.range(1, 1, 1)).isEqualTo(Nil.empty());
            assertThat(ISeq.range(-1, -1, 1)).isEqualTo(Nil.empty());
        }
    }
}