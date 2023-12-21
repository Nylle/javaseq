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
                    .isInstanceOf(ArraySeq.class)
                    .containsExactly(1);
        }

        @Test
        void returnsSeqOfSuppliedItems() {
            assertThat(ISeq.of(1, 2, 3))
                    .isInstanceOf(ArraySeq.class)
                    .containsExactly(1, 2, 3);
        }

        @Test
        void returnsSeqOfSuppliedLists() {
            assertThat(ISeq.of(List.of(1, 2), List.of("foo", "bar"), List.of(true, false)))
                    .isInstanceOf(ArraySeq.class)
                    .containsExactly(List.of(1, 2), List.of("foo", "bar"), List.of(true, false));
        }
    }

    @Nested
    class Seq {

        @Test
        void returnsSeqWithNulls() {
            var list = new ArrayList<String>();
            list.add(null);
            list.add(null);
            list.add(null);

            assertThat(ISeq.seq(list))
                    .isInstanceOf(ArraySeq.class)
                    .containsExactly(null, null, null);
        }

        @Test
        void returnsSeqOfItemsInIterable() {
            var list = List.of(1, 2, 3);

            assertThat(ISeq.seq(list))
                    .isInstanceOf(LazySeq.class)
                    .containsExactly(1, 2, 3);
        }

        @Test
        void returnsSeqOfItemsInArrayList() {
            var arrayList = new ArrayList<>(List.of("a", "b", "c"));

            assertThat(ISeq.seq(arrayList))
                    .isExactlyInstanceOf(ArraySeq.class)
                    .containsExactly("a", "b", "c");
        }

        @Test
        void returnsSeqOfItemsOfIterator() {
            var infiniteIterator = Stream.iterate(0, x -> x + 1).iterator();

            assertThat(ISeq.seq(infiniteIterator).take(4))
                    .isInstanceOf(LazySeq.class)
                    .containsExactly(0, 1, 2, 3);
        }

        @Test
        void returnsSeqOfItemsInStream() {
            var infiniteStream = Stream.iterate(0, x -> x + 1);

            assertThat(ISeq.seq(infiniteStream).take(4))
                    .isInstanceOf(LazySeq.class)
                    .containsExactly(0, 1, 2, 3);
        }

        @Test
        void returnsSeqOfItemsInArray() {
            var array = new Integer[]{1, 2, 3};

            assertThat(ISeq.seq(array))
                    .isInstanceOf(ArraySeq.class)
                    .containsExactly(1, 2, 3);
        }

        @Test
        void returnsSeqOfKeyValuePairsInMap() {
            var map = Map.of("a", 1, "b", 2, "c", 3);

            assertThat(ISeq.seq(map))
                    .isInstanceOf(LazySeq.class)
                    .containsExactlyInAnyOrder(entry("a", 1), entry("b", 2), entry("c", 3));
        }

        @Test
        void returnsSeqOfCharactersInString() {
            assertThat(ISeq.seq("foo")).isInstanceOf(StringSeq.class).containsExactly('f', 'o', 'o');
            assertThat(ISeq.seq("foo".toCharArray())).isExactlyInstanceOf(StringSeq.class).containsExactly('f', 'o', 'o');
            assertThat(ISeq.seq(new Character[]{'f', 'o', 'o'})).isExactlyInstanceOf(StringSeq.class).containsExactly('f', 'o', 'o');
        }

        @Test
        void returnsEmptySeqIfCollIsNull() {
            assertThat(ISeq.seq((Iterable<Integer>) null)).isInstanceOf(Nil.class).isEmpty();
            assertThat(ISeq.seq((Stream<Integer>) null)).isInstanceOf(Nil.class).isEmpty();
            assertThat(ISeq.seq((Iterator<Integer>) null)).isInstanceOf(Nil.class).isEmpty();
            assertThat(ISeq.seq((Map<String, Integer>) null)).isInstanceOf(Nil.class).isEmpty();
            assertThat(ISeq.seq((String) null)).isInstanceOf(Nil.class).isEmpty();
        }

        @Test
        void returnsEmptySeqIfCollIsEmpty() {
            assertThat(ISeq.seq(List.<Integer>of())).isInstanceOf(Nil.class).isEmpty();
            assertThat(ISeq.seq(Stream.<Integer>of())).isInstanceOf(Nil.class).isEmpty();
            assertThat(ISeq.seq(Collections.<Integer>emptyIterator())).isInstanceOf(Nil.class).isEmpty();
            assertThat(ISeq.seq(Map.<String, Integer>of())).isInstanceOf(Nil.class).isEmpty();
            assertThat(ISeq.seq("")).isInstanceOf(Nil.class).isEmpty();
        }

        @Test
        void returnsCollIfAlreadyASeq() {
            var coll = ISeq.of(1, 2, 3);
            assertThat(ISeq.seq(coll)).isSameAs(coll);
        }

        @Test
        void doesNotForceLazyColl() {
            assertThat(ISeq.seq(Stream.iterate(0, x -> x + 1)).take(3))
                    .isInstanceOf(LazySeq.class)
                    .containsExactly(0, 1, 2);
            assertThat(ISeq.seq(Stream.iterate(0, x -> x + 1).iterator()).take(3))
                    .isInstanceOf(LazySeq.class)
                    .containsExactly(0, 1, 2);
        }
    }

    @Nested
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

    @Nested
    class Repeat {

        @Test
        void returnsSeqWithNulls() {
            var sut = ISeq.repeat(null);

            assertThat(sut.take(4)).containsExactly(null, null, null, null);
        }

        @Test
        void returnsInfiniteSeqOfXs() {
            var actual = ISeq.repeat(99);

            assertThat(actual.take(4))
                    .containsExactly(99, 99, 99, 99);
        }

        @Test
        void returnsSeqOfXsWithLengthN() {
            var actual = ISeq.repeat(4, "bar");

            assertThat(actual).containsExactly("bar", "bar", "bar", "bar");
        }
    }

    @Nested
    @DisplayName("concat")
    class Concat {

        @Test
        void returnsSeqFromConcatenatingMultipleSeqsWithNulls() {
            var sut = ISeq.concat(ISeq.of(null, null), ISeq.of(null, null), ISeq.of(null, null));

            assertThat(sut).containsExactly(null, null, null, null, null, null);
        }

        @Test
        void returnsSeqFromConcatenatingMultipleSeqs() {
            var actual = ISeq.concat(ISeq.of("a", "b"), ISeq.of("c", "d"), ISeq.of("e", "f"));

            assertThat(actual)
                    .isInstanceOf(LazySeq.class)
                    .containsExactly("a", "b", "c", "d", "e", "f");
        }

        @Test
        void returnsSeqFromConcatenatingMultipleStrings() {
            var actual = ISeq.concat("hello", " ", "world");

            assertThat(actual)
                    .isInstanceOf(StringSeq.class)
                    .containsExactly('h', 'e', 'l', 'l', 'o', ' ', 'w', 'o', 'r', 'l', 'd');
        }

        @Test
        void returnsSeqFromConcatenatingMultipleIterables() {
            var actual = ISeq.concat(List.of("a", "b"), List.of("c", "d"), List.of("e", "f"));

            assertThat(actual)
                    .isInstanceOf(LazySeq.class)
                    .containsExactly("a", "b", "c", "d", "e", "f");
        }

        @Test
        void isLazy() {
            assertThat(ISeq.concat(ISeq.of("a", "b"), ISeq.of("c", "d"), ISeq.iterate("e", x -> x + "e")).take(7))
                    .containsExactly("a", "b", "c", "d", "e", "ee", "eee");

            assertThat(ISeq.concat(ISeq.of("a", "b"), ISeq.iterate("c", x -> x + "c"), ISeq.of("e", "f")).take(7))
                    .containsExactly("a", "b", "c", "cc", "ccc", "cccc", "ccccc");

            assertThat(ISeq.concat(ISeq.iterate("a", x -> x + "a"), ISeq.of("c", "d"), ISeq.of("e", "f")).take(7))
                    .containsExactly("a", "aa", "aaa", "aaaa", "aaaaa", "aaaaaa", "aaaaaaa");
        }
    }

}