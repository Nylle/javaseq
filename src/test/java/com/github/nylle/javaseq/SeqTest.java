package com.github.nylle.javaseq;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

class SeqTest {

    @Nested
    class Static {

        @Nested
        class Of {

            @Test
            void returnsEmptySeq() {
                assertThat(Seq.of()).isEmpty();
            }

            @Test
            void returnsSeqOfSingleItem() {
                assertThat(Seq.of(1)).containsExactly(1);
            }

            @Test
            void returnsSeqOfSuppliedItems() {
                assertThat(Seq.of(1, 2, 3)).containsExactly(1, 2, 3);
            }

            @Test
            void returnsSeqOfItemsInIterable() {
                var list = List.of(1, 2, 3);

                assertThat(Seq.of(list)).containsExactly(1, 2, 3);
            }

            @Test
            void returnsSeqOfItemsInArray() {
                var array = new Integer[] {1, 2, 3};

                assertThat(Seq.of(array)).containsExactly(1, 2, 3);
            }

            @Test
            void returnsSeqOfItemsOfIterator() {
                var infiniteIterator = Stream.iterate(0, x -> x + 1).iterator();

                assertThat(Seq.of(infiniteIterator).take(4)).containsExactly(0, 1, 2, 3);
            }

            @Test
            void returnsSeqOfItemsInStream() {
                var infiniteStream = Stream.iterate(0, x -> x + 1);

                assertThat(Seq.of(infiniteStream).take(4)).containsExactly(0, 1, 2, 3);
            }

            @Test
            void returnsSeqOfKeyValuePairsInMap() {
                var map = Map.of("a", 1, "b", 2, "c", 3);

                assertThat(Seq.of(map)).containsExactlyInAnyOrder(entry("a", 1), entry("b", 2), entry("c", 3));
            }
        }

        @Nested
        class Cons {

            @Test
            void returnsSeqFromFirstElementAndSeqSupplier() {
                var first = "a";
                Supplier<Seq<String>> supplier = () -> Seq.cons("b", () -> Seq.cons("c", () -> Nil.of()));

                var actual = Seq.cons(first, supplier);

                assertThat(actual).containsExactly("a", "b", "c");
            }
        }

        @Nested
        class Concat {

            @Test
            void returnsSeqFromConcatenatingIterableAndSupplier() {
                var iterable = List.of("a", "b", "c");
                Supplier<Seq<String>> supplier = () -> Seq.cons("d", () -> Seq.cons("e", () -> Nil.of()));

                var actual = Seq.concat(iterable, supplier);

                assertThat(actual).containsExactly("a", "b", "c", "d", "e");
            }

            @Test
            void returnsSeqFromConcatenatingIteratorAndSupplier() {
                var iterator = List.of("a", "b", "c").iterator();
                Supplier<Seq<String>> supplier = () -> Seq.cons("d", () -> Seq.cons("e", () -> Nil.of()));

                var actual = Seq.concat(iterator, supplier);

                assertThat(actual).containsExactly("a", "b", "c", "d", "e");
            }
        }
        
        @Nested
        class Iterate {
        
            @Test
            void returnsSeqOfInitialValueUsingFunction() {

                var actual = Seq.iterate(0, x -> x + 1);

                assertThat(actual.take(4)).containsExactly(0, 1, 2, 3);

            }
        }

        @Nested
        class Range {

            @Test
            void returnsInfiniteSeqOfIntegersStartingWithZero() {
                assertThat(Seq.range().take(3)).containsExactly(0, 1, 2);
            }

            @Test
            void returnsSeqOfIntegersStartingWithZeroUntilEnd() {
                assertThat(Seq.range(3)).containsExactly(0, 1, 2);
            }

            @Test
            void returnsSeqOfIntegersFromStartInclusiveUntilEndExclusive() {
                assertThat(Seq.range(1, 5)).containsExactly(1, 2, 3, 4);
                assertThat(Seq.range(-5, 5)).containsExactly(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4);
            }

            @Test
            void returnsSeqOfIntegersFromStartInclusiveUntilEndExclusiveByStep() {
                assertThat(Seq.range(10, 25, 5)).containsExactly(10, 15, 20);
                assertThat(Seq.range(10, -25, -5)).containsExactly(10, 5, 0, -5, -10, -15, -20);
                assertThat(Seq.range(-10, 25, 5)).containsExactly(-10, -5, 0, 5, 10, 15, 20);
            }

            @Test
            void returnsInfiniteSeqOfStartWhenStepIsZero() {
                assertThat(Seq.range(10, 25, 0).take(10)).containsExactly(10, 10, 10, 10, 10, 10, 10, 10, 10, 10);
            }

            @Test
            void returnsEmptySeqWhenStartIsEqualToEnd() {
                assertThat(Seq.range(10, 10)).isEmpty();
                assertThat(Seq.range(-10, -10)).isEmpty();
                assertThat(Seq.range(1, 1, 1)).isEmpty();
                assertThat(Seq.range(-1, -1, 1)).isEmpty();
            }
        }
    }

    @Nested
    class Extensions {

        @Nested
        class FromStream {

            @Test
            void returnsEmptySeqIfStreamIsEmpty() {
                assertThat(Seq.Extensions.toSeq(Stream.of())).isEmpty();
            }

            @Test
            void returnsSeqOfItemsInStream() {
                assertThat(Seq.Extensions.toSeq(Stream.of(1, 2, 3))).containsExactly(1, 2, 3);
            }
        }

        @Nested
        class FromIterable {

            @Test
            void returnsEmptySeqIfIterableIsEmpty() {
                assertThat(Seq.Extensions.toSeq(List.of())).isEmpty();
            }

            @Test
            void returnsSeqOfItemsInIterable() {
                var list = List.of(1, 2, 3);

                assertThat(Seq.Extensions.toSeq(list)).containsExactly(1, 2, 3);
            }
        }

        @Nested
        class FromIterator {

            @Test
            void returnsEmptySeqIfIterableIsEmpty() {
                assertThat(Seq.Extensions.toSeq(Stream.of().iterator())).isEmpty();
            }

            @Test
            void returnsSeqOfItemsInIterable() {
                var iterator = Stream.iterate(0, x -> x + 1).iterator();

                assertThat(Seq.Extensions.toSeq(iterator).take(4)).containsExactly(0, 1, 2, 3);
            }
        }

        @Nested
        class FromMap {

            @Test
            void returnsEmptySeqIfMapIsEmpty() {
                assertThat(Seq.Extensions.toSeq(Map.of())).isEmpty();
            }

            @Test
            void returnsSeqOfKeyValuePairsInMap() {
                var map = Map.of("a", 1, "b", 2, "c", 3);

                assertThat(Seq.Extensions.toSeq(map)).containsExactlyInAnyOrder(entry("a", 1), entry("b", 2), entry("c", 3));
            }
        }
    }
}