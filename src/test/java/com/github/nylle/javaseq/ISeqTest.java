package com.github.nylle.javaseq;

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
    class From {

        @Test
        void returnsSeqWithNulls() {
            var list = new ArrayList<String>();
            list.add(null);
            list.add(null);
            list.add(null);

            assertThat(ISeq.from(list))
                    .isInstanceOf(ArraySeq.class)
                    .containsExactly(null, null, null);
        }

        @Test
        void returnsSeqOfItemsInIterable() {
            var list = List.of(1, 2, 3);

            assertThat(ISeq.from(list))
                    .isInstanceOf(LazySeq.class)
                    .containsExactly(1, 2, 3);
        }

        @Test
        void returnsSeqOfItemsOfIterator() {
            var infiniteIterator = Stream.iterate(0, x -> x + 1).iterator();

            assertThat(ISeq.from(infiniteIterator).take(4))
                    .isInstanceOf(LazySeq.class)
                    .containsExactly(0, 1, 2, 3);
        }

        @Test
        void returnsSeqOfItemsInStream() {
            var infiniteStream = Stream.iterate(0, x -> x + 1);

            assertThat(ISeq.from(infiniteStream).take(4))
                    .isInstanceOf(LazySeq.class)
                    .containsExactly(0, 1, 2, 3);
        }

        @Test
        void returnsSeqOfItemsInArray() {
            var array = new Integer[]{1, 2, 3};

            assertThat(ISeq.from(array))
                    .isInstanceOf(ArraySeq.class)
                    .containsExactly(1, 2, 3);
        }

        @Test
        void returnsSeqOfKeyValuePairsInMap() {
            var map = Map.of("a", 1, "b", 2, "c", 3);

            assertThat(ISeq.from(map))
                    .isInstanceOf(LazySeq.class)
                    .containsExactlyInAnyOrder(entry("a", 1), entry("b", 2), entry("c", 3));
        }

        @Test
        void returnsSeqOfCharactersInString() {
            assertThat(ISeq.from("foo")).isInstanceOf(StringSeq.class).containsExactly('f', 'o', 'o');
            assertThat(ISeq.from("foo".toCharArray())).isExactlyInstanceOf(StringSeq.class).containsExactly('f', 'o', 'o');
            assertThat(ISeq.from(new Character[] {'f', 'o', 'o'})).isExactlyInstanceOf(StringSeq.class).containsExactly('f', 'o', 'o');
        }

        @Test
        void returnsEmptySeqIfCollIsNull() {
            assertThat(ISeq.from((Iterable<Integer>) null)).isInstanceOf(Nil.class).isEmpty();
            assertThat(ISeq.from((Stream<Integer>) null)).isInstanceOf(Nil.class).isEmpty();
            assertThat(ISeq.from((Iterator<Integer>) null)).isInstanceOf(Nil.class).isEmpty();
            assertThat(ISeq.from((Map<String, Integer>) null)).isInstanceOf(Nil.class).isEmpty();
            assertThat(ISeq.from((String) null)).isInstanceOf(Nil.class).isEmpty();
        }

        @Test
        void returnsEmptySeqIfCollIsEmpty() {
            assertThat(ISeq.from(List.<Integer>of())).isInstanceOf(Nil.class).isEmpty();
            assertThat(ISeq.from(Stream.<Integer>of())).isInstanceOf(Nil.class).isEmpty();
            assertThat(ISeq.from(Collections.<Integer>emptyIterator())).isInstanceOf(Nil.class).isEmpty();
            assertThat(ISeq.from(Map.<String, Integer>of())).isInstanceOf(Nil.class).isEmpty();
            assertThat(ISeq.from("")).isInstanceOf(Nil.class).isEmpty();
        }

        @Test
        void returnsCollIfAlreadyASeq() {
            var coll = ISeq.of(1, 2, 3);
            assertThat(ISeq.from(coll)).isSameAs(coll);
        }

        @Test
        void doesNotForceLazyColl() {
            assertThat(ISeq.from(Stream.iterate(0, x -> x + 1)).take(3))
                    .isInstanceOf(LazySeq.class)
                    .containsExactly(0, 1, 2);
            assertThat(ISeq.from(Stream.iterate(0, x -> x + 1).iterator()).take(3))
                    .isInstanceOf(LazySeq.class)
                    .containsExactly(0, 1, 2);
        }
    }
}