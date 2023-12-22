package com.github.nylle.javaseq;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

class SeqExtensionsTest {

    @Nested
    class FromStream {

        @Test
        void returnsEmptySeqIfStreamIsEmpty() {
            assertThat(SeqExtensions.toSeq(Stream.of())).isEmpty();
        }

        @Test
        void returnsSeqOfItemsInStream() {
            assertThat(SeqExtensions.toSeq(Stream.of(1, 2, 3)))
                    .isInstanceOf(LazySeq.class)
                    .containsExactly(1, 2, 3);
        }
    }

    @Nested
    class FromArray {

        @Test
        void returnsEmptySeqIfArrayIsEmpty() {
            assertThat(SeqExtensions.toSeq(new String[0])).isEmpty();
        }

        @Test
        void returnsSeqOfItemsInArray() {
            assertThat(SeqExtensions.toSeq("f o o".split(" ")))
                    .isInstanceOf(ArraySeq.class)
                    .containsExactly("f", "o", "o");
        }
    }

    @Nested
    class FromIterable {

        @Test
        void returnsEmptySeqIfIterableIsEmpty() {
            assertThat(SeqExtensions.toSeq(List.of())).isEmpty();
        }

        @Test
        void returnsSeqOfItemsInIterable() {
            var list = List.of(1, 2, 3);

            assertThat(SeqExtensions.toSeq(list))
                    .isInstanceOf(ArraySeq.class)
                    .containsExactly(1, 2, 3);
        }
    }

    @Nested
    class FromIterator {

        @Test
        void returnsEmptySeqIfIterableIsEmpty() {
            assertThat(SeqExtensions.toSeq(Stream.of().iterator())).isEmpty();
        }

        @Test
        void returnsSeqOfItemsInIterable() {
            var iterator = Stream.iterate(0, x -> x + 1).iterator();

            assertThat(SeqExtensions.toSeq(iterator).take(4)).containsExactly(0, 1, 2, 3);
            assertThat(SeqExtensions.toSeq(iterator)).isInstanceOf(LazySeq.class);
        }
    }

    @Nested
    class FromMap {

        @Test
        void returnsEmptySeqIfMapIsEmpty() {
            assertThat(SeqExtensions.toSeq(Map.of())).isEmpty();
        }

        @Test
        void returnsSeqOfKeyValuePairsInMap() {
            var map = Map.of("a", 1, "b", 2, "c", 3);

            assertThat(SeqExtensions.toSeq(map))
                    .isInstanceOf(LazySeq.class)
                    .containsExactlyInAnyOrder(entry("a", 1), entry("b", 2), entry("c", 3));
        }
    }

    @Nested
    class FromString {

        @Test
        void returnsEmptySeqIfIterableIsEmpty() {
            assertThat(SeqExtensions.toSeq("")).isEmpty();
            assertThat(SeqExtensions.toSeq("".toCharArray())).isEmpty();
            assertThat(SeqExtensions.toSeq(new Character[0])).isEmpty();
        }

        @Test
        void returnsSeqOfItemsInIterable() {
            assertThat(SeqExtensions.toSeq("foo")).isInstanceOf(StringSeq.class).containsExactly('f', 'o', 'o');
            assertThat(SeqExtensions.toSeq("foo".toCharArray())).isInstanceOf(StringSeq.class).containsExactly('f', 'o', 'o');
            assertThat(SeqExtensions.toSeq(new Character[]{'f', 'o', 'o'})).isInstanceOf(StringSeq.class).containsExactly('f', 'o', 'o');
        }
    }
}