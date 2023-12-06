package com.github.nylle.javaseq;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ChunkedConsTest {

    @Test
    void firstReturnsFirstItem() {
        var sut = new ChunkedCons<>(ArrayChunk.fromList(List.of(1, 2, 3)), ISeq.of(4, 5, 6));

        assertThat(sut.first()).isEqualTo(1);
    }

    @Test
    void restReturnsSeqWithItemsExceptFirst() {
        var sut = new ChunkedCons<>(ArrayChunk.fromList(List.of(1, 2, 3)), ISeq.of(4, 5, 6));

        var actual = sut.rest();

        assertThat(actual.first()).isEqualTo(2);
        assertThat(actual.rest().first()).isEqualTo(3);
        assertThat(actual.rest().rest().first()).isEqualTo(4);
        assertThat(actual.rest().rest().rest().first()).isEqualTo(5);
        assertThat(actual.rest().rest().rest().rest().first()).isEqualTo(6);
        assertThat(actual.rest().rest().rest().rest().rest()).isEmpty();
    }

    @Test
    void isRealizedReturnsTrue() {
        assertThat(new ChunkedCons<>(ArrayChunk.fromList(List.of(1, 2, 3)), ISeq.of(4, 5, 6)).isRealized()).isTrue();
    }

    @Nested
    class Filter {

        @Test
        void returnsNilWhenNoItemsMatch() {
            var sut = new ChunkedCons<>(ArrayChunk.fromList(List.of(1, 2, 3)), ISeq.of(4, 5, 6));

            assertThat(sut.filter(x -> x < 1)).isEmpty();
        }

        @Test
        void returnsMatchingItems() {
            var sut = new ChunkedCons<>(ArrayChunk.fromList(List.of(1, 2, 3)), ISeq.of(4, 5, 6));

            assertThat(sut.filter(x -> x > 1 && x < 6)).containsExactly(2, 3, 4, 5);
        }
    }

    @Nested
    class Map {

        @Test
        void returnsSingleMapResult() {
            var sut = new ChunkedCons<>(ArrayChunk.fromList(List.of("xxx")), ISeq.of());

            assertThat(sut.map(x -> x.length())).isEqualTo(ISeq.of(3));
        }

        @Test
        void returnsAllMapResults() {
            var sut = new ChunkedCons<>(ArrayChunk.fromList(List.of("xxx", "ab")), ISeq.of("baz", "foobar"));

            assertThat(sut.map(x -> x.length())).isEqualTo(ISeq.of(3, 2, 3, 6));
        }

        @Nested
        class WithOtherColl {

            @Test
            void returnsEmptySeqWhenProvidingEmptyOther() {
                var sut = new ChunkedCons<>(ArrayChunk.fromList(List.of("xxx", "ab")), ISeq.of("baz", "foobar"));

                assertThat(sut.map(ISeq.<Integer>of(), (a, b) -> a + b)).isEmpty();
                assertThat(sut.map(ISeq.<Integer>of().iterator(), (a, b) -> a + b)).isEmpty();
                assertThat(sut.map(List.<Integer>of(), (a, b) -> a + b)).isEmpty();
                assertThat(sut.map(Stream.<Integer>of(), (a, b) -> a + b)).isEmpty();
                assertThat(sut.map(new Integer[0], (a, b) -> a + b)).isEmpty();
                assertThat(sut.map("", (a, b) -> "" + a + b)).isEmpty();
            }

            @Test
            void returnsANewSeqWithTheItemsOfBothInitialSeqsAreCombinedUsingF() {
                var sut = new ChunkedCons<>(ArrayChunk.fromList(List.of(1, 2)), ISeq.of(3));

                assertThat(sut.map(ISeq.of("a", "b", "c"), (a, b) -> a + b)).containsExactly("1a", "2b", "3c");
                assertThat(sut.map(ISeq.of("a", "b", "c").iterator(), (a, b) -> a + b)).containsExactly("1a", "2b", "3c");
                assertThat(sut.map(List.of("a", "b", "c"), (a, b) -> a + b)).containsExactly("1a", "2b", "3c");
                assertThat(sut.map(Stream.of("a", "b", "c"), (a, b) -> a + b)).containsExactly("1a", "2b", "3c");
                assertThat(sut.map(new String[]{"a", "b", "c"}, (a, b) -> a + b)).containsExactly("1a", "2b", "3c");
                assertThat(sut.map("abc", (a, b) -> "" + a + b)).containsExactly("1a", "2b", "3c");
            }

            @Test
            void ignoresRemainingItemsIfOneOfTheSeqsIsExhausted() {
                var sut = new ChunkedCons<>(ArrayChunk.fromList(List.of(1, 2)), ISeq.of(3));

                assertThat(sut.map(ISeq.of("a", "b"), (a, b) -> a + b)).containsExactly("1a", "2b");
                assertThat(sut.map(ISeq.of("a", "b", "c", "d"), (a, b) -> a + b)).containsExactly("1a", "2b", "3c");

                assertThat(sut.map(ISeq.of("a", "b").iterator(), (a, b) -> a + b)).containsExactly("1a", "2b");
                assertThat(sut.map(ISeq.of("a", "b", "c", "d").iterator(), (a, b) -> a + b)).containsExactly("1a", "2b", "3c");

                assertThat(sut.map(List.of("a", "b"), (a, b) -> a + b)).containsExactly("1a", "2b");
                assertThat(sut.map(List.of("a", "b", "c", "d"), (a, b) -> a + b)).containsExactly("1a", "2b", "3c");

                assertThat(sut.map(Stream.of("a", "b"), (a, b) -> a + b)).containsExactly("1a", "2b");
                assertThat(sut.map(Stream.of("a", "b", "c", "d"), (a, b) -> a + b)).containsExactly("1a", "2b", "3c");

                assertThat(sut.map(new String[]{"a", "b"}, (a, b) -> a + b)).containsExactly("1a", "2b");
                assertThat(sut.map(new String[]{"a", "b", "c", "d"}, (a, b) -> a + b)).containsExactly("1a", "2b", "3c");

                assertThat(sut.map("ab", (a, b) -> "" + a + b)).containsExactly("1a", "2b");
                assertThat(sut.map("abcd", (a, b) -> "" + a + b)).containsExactly("1a", "2b", "3c");
            }
        }
    }

    @Nested
    class Mapcat {

        @Test
        void returnsFlattenedMapResult() {
            var sut = new ChunkedCons<>(ArrayChunk.fromList(List.of(1, 2)), ISeq.of(3, 4));

            assertThat(sut.mapcat(x -> List.of(x, x))).containsExactly(1, 1, 2, 2, 3, 3, 4, 4);
        }

        @Test
        void ignoresEmptyResults() {
            var sut = new ChunkedCons<>(ArrayChunk.fromList(List.of(0, 1)), ISeq.of(2, 3));

            assertThat(sut.mapcat(x -> x == 0 ? List.of() : List.of(x, x))).containsExactly(1, 1, 2, 2, 3, 3);
        }

        @Nested
        class WithOtherColl {

            @Test
            void returnsEmptySeqWhenProvidingEmptyOther() {
                var sut = new ChunkedCons<>(ArrayChunk.fromList(List.of(0, 1)), ISeq.of(2, 3));

                assertThat(sut.mapcat(List.<Integer>of(), (a, b) -> List.of(a + b, a + b))).isEmpty();
            }

            @Test
            void returnsANewSeqWithTheItemsOfBothInitialSeqsAreCombinedUsingF() {
                var sut = new ChunkedCons<>(ArrayChunk.fromList(List.of(1, 2)), ISeq.of(3));

                assertThat(sut.mapcat(List.of("a", "b", "c"), (a, b) -> List.of(a + b, a + b)))
                        .containsExactly("1a", "1a", "2b", "2b", "3c", "3c");
            }

            @Test
            void ignoresRemainingItemsIfOneOfTheSeqsIsExhausted() {
                var sut = new ChunkedCons<>(ArrayChunk.fromList(List.of(1, 2)), ISeq.of(3));

                assertThat(sut.mapcat(List.of("a", "b"), (a, b) -> List.of(a + b, a + b)))
                        .containsExactly("1a", "1a", "2b", "2b");
                assertThat(sut.mapcat(List.of("a", "b", "c", "d"), (a, b) -> List.of(a + b, a + b)))
                        .containsExactly("1a", "1a", "2b", "2b", "3c", "3c");
            }
        }
    }

    @Nested
    class Take {

        @Test
        void returnsNilWithNegativeItems() {
            var sut = new ChunkedCons<>(ArrayChunk.fromList(List.of(1, 2, 3)), ISeq.of(4, 5, 6));

            assertThat(sut.take(-1))
                    .isExactlyInstanceOf(Nil.class)
                    .isEmpty();
        }

        @Test
        void returnsNilWithZeroItems() {
            var sut = new ChunkedCons<>(ArrayChunk.fromList(List.of(1, 2, 3)), ISeq.of(4, 5, 6));

            assertThat(sut.take(0))
                    .isExactlyInstanceOf(Nil.class)
                    .isEmpty();
        }

        @Test
        void returnsConsWithFewerItemsThanChunk() {
            var sut = new ChunkedCons<>(ArrayChunk.fromList(List.of(1, 2, 3)), ISeq.of(4, 5, 6));

            assertThat(sut.take(2))
                    .isExactlyInstanceOf(Cons.class)
                    .containsExactly(1, 2);
        }

        @Test
        void returnsConsWithSameItemsAsChunk() {
            var sut = new ChunkedCons<>(ArrayChunk.fromList(List.of(1, 2, 3)), ISeq.of(4, 5, 6));

            assertThat(sut.take(3))
                    .isExactlyInstanceOf(Cons.class)
                    .containsExactly(1, 2, 3);
        }

        @Test
        void returnsConsWithMoreItemsThanChunk() {
            var sut = new ChunkedCons<>(ArrayChunk.fromList(List.of(1, 2, 3)), ISeq.of(4, 5, 6));

            assertThat(sut.take(4))
                    .isExactlyInstanceOf(Cons.class)
                    .containsExactly(1, 2, 3, 4);
        }

        @Test
        void canHandleLargeCollection() {
//            var sut = new ChunkedCons<>(ISeq.range(10000));
//
//            var actual = sut.take(10000);
//
//            assertThat(actual).hasSize(10000);
        }
    }
}