package com.github.nylle.javaseq;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

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
    @DisplayName("cons")
    class ConsTest {

        @Test
        void returnsSeqFromFirstElementAndSeq() {
            var actual = ISeq.cons("a", ISeq.cons("b", ISeq.cons("c", Nil.empty())));

            assertThat(actual)
                    .isInstanceOf(Cons.class)
                    .containsExactly("a", "b", "c");
        }
    }

    @Nested
    @DisplayName("lazySeq")
    class LazySeqTest {

        @Test
        void returnsSeqFromFirstElementAndSeqSupplier() {
            var actual = ISeq.lazySeq("a", () -> ISeq.lazySeq("b", () -> ISeq.lazySeq("c", () -> Nil.empty())));

            assertThat(actual)
                    .isInstanceOf(LazySeq.class)
                    .containsExactly("a", "b", "c");
        }
    }

    @Nested
    @DisplayName("sequence")
    class Sequence {

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
                    .isInstanceOf(Cons.class)
                    .containsExactly(0, 1, 2, 3);
        }

        @Test
        void returnsSeqOfItemsInStream() {
            var infiniteStream = Stream.iterate(0, x -> x + 1);

            assertThat(ISeq.sequence(infiniteStream).take(4))
                    .isInstanceOf(Cons.class)
                    .containsExactly(0, 1, 2, 3);
        }

        @Test
        void returnsSeqOfItemsInArray() {
            var array = new Integer[] {1, 2, 3};

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
        void returnsEmptySeqIfCollIsNull() {
            assertThat(ISeq.sequence((Iterable<Integer>) null)).isInstanceOf(Nil.class).isEmpty();
            assertThat(ISeq.sequence((Stream<Integer>) null)).isInstanceOf(Nil.class).isEmpty();
            assertThat(ISeq.sequence((Iterator<Integer>) null)).isInstanceOf(Nil.class).isEmpty();
            assertThat(ISeq.sequence((Map<String, Integer>) null)).isInstanceOf(Nil.class).isEmpty();
        }

        @Test
        void returnsCollIfAlreadyASeq() {
            var coll = ISeq.of(1, 2, 3);
            assertThat(ISeq.sequence(coll)).isSameAs(coll);
        }

        @Test
        void doesNotForceLazyColl() {
            assertThat(ISeq.sequence(Stream.iterate(0, x -> x + 1)).take(3))
                    .isInstanceOf(Cons.class)
                    .containsExactly(0, 1, 2);
            assertThat(ISeq.sequence(Stream.iterate(0, x -> x + 1).iterator()).take(3))
                    .isInstanceOf(Cons.class)
                    .containsExactly(0, 1, 2);
        }
    }

    @Nested
    @DisplayName("concat")
    class Concat {

        @Test
        void returnsSeqFromConcatenatingMultipleSeqs() {
            var actual = ISeq.concat(ISeq.of("a", "b"), ISeq.of("c", "d"), ISeq.of("e", "f"));

            assertThat(actual)
                    .isInstanceOf(LazySeq.class)
                    .containsExactly("a", "b", "c", "d", "e", "f");
        }

        @Test
        void returnsSeqFromConcatenatingMultipleIterables() {
            var actual = ISeq.concat(List.of("a", "b"), List.of("c", "d"), List.of("e", "f"));

            assertThat(actual)
                    .isInstanceOf(LazySeq.class)
                    .containsExactly("a", "b", "c", "d", "e", "f");
        }
    }

    @Nested
    @DisplayName("iterate")
    class Iterate {

        @Test
        void returnsSeqOfInitialValueUsingFunction() {

            var actual = ISeq.iterate(0, x -> x + 1);

            assertThat(actual.take(4))
                    .isInstanceOf(Cons.class)
                    .containsExactly(0, 1, 2, 3);
        }
    }

    @Nested
    @DisplayName("range")
    class Range {

        @Test
        void returnsInfiniteSeqOfIntegersStartingWithZero() {
            assertThat(ISeq.range().take(3))
                    .isInstanceOf(Cons.class)
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
                    .isInstanceOf(Cons.class)
                    .containsExactly(10, 10, 10, 10, 10, 10, 10, 10, 10, 10);
        }

        @Test
        void returnsEmptySeqWhenStartIsEqualToEnd() {
            assertThat(ISeq.range(10, 10)).isInstanceOf(Nil.class).isEmpty();
            assertThat(ISeq.range(-10, -10)).isInstanceOf(Nil.class).isEmpty();
            assertThat(ISeq.range(1, 1, 1)).isInstanceOf(Nil.class).isEmpty();
            assertThat(ISeq.range(-1, -1, 1)).isInstanceOf(Nil.class).isEmpty();
        }
    }
}