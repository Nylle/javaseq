package com.github.nylle.javaseq;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class ChunkedConsTest {

    private static <T> ArrayChunk<T> arrayChunk(List<T> list) {
        return new ArrayChunk<>((T[]) list.toArray(new Object[0]), 0, list.size());
    }

    @Test
    void firstReturnsFirstItem() {
        var sut = new ChunkedCons<>(arrayChunk(List.of(1, 2, 3)), ISeq.of(4, 5, 6));

        assertThat(sut.first()).isEqualTo(1);
    }

    @Nested
    class Second {

        @Test
        void returnsSecondItem() {
            var sut = new ChunkedCons<>(arrayChunk(List.of(1, 2, 3)), ISeq.of(4, 5, 6));

            assertThat(sut.second()).isEqualTo(2);
        }

        @Test
        void returnsNullIfSeqHasOnlyOneElement() {
            var sut = new ChunkedCons<>(arrayChunk(List.of(1)), ISeq.of());

            assertThat(sut.second()).isNull();
        }
    }

    @Test
    void restReturnsSeqWithItemsExceptFirst() {
        var sut = new ChunkedCons<>(arrayChunk(List.of(1, 2, 3)), ISeq.of(4, 5, 6));

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
        assertThat(new ChunkedCons<>(arrayChunk(List.of(1, 2, 3)), ISeq.of(4, 5, 6)).isRealized()).isTrue();
    }

    @Nested
    class Filter {

        @Test
        void returnsNilWhenNoItemsMatch() {
            var sut = new ChunkedCons<>(arrayChunk(List.of(1, 2, 3)), ISeq.of(4, 5, 6));

            assertThat(sut.filter(x -> x < 1)).isEmpty();
        }

        @Test
        void returnsMatchingItems() {
            var sut = new ChunkedCons<>(arrayChunk(List.of(1, 2, 3)), ISeq.of(4, 5, 6));

            assertThat(sut.filter(x -> x > 1 && x < 6)).containsExactly(2, 3, 4, 5);
        }
    }

    @Nested
    class Map {

        @Test
        void returnsSingleMapResult() {
            var sut = new ChunkedCons<>(arrayChunk(List.of("xxx")), ISeq.of());

            assertThat(sut.map(x -> x.length())).containsExactly(3);
        }

        @Test
        void returnsAllMapResults() {
            var sut = new ChunkedCons<>(arrayChunk(List.of("xxx", "ab")), ISeq.of("baz", "foobar"));

            assertThat(sut.map(x -> x.length())).containsExactly(3, 2, 3, 6);
        }

        @Test
        void returnsInfiniteSeqWithInfiniteSeqsIfMappingResultIsInfinite() {
            var sut = new ChunkedCons<>(arrayChunk(List.of(0, 1, 2)), ISeq.of(3, 4, 5));

            var actual = sut.map(x -> ISeq.iterate(x, i -> i + x)).take(4);

            assertThat(actual).hasSize(4);
            assertThat(actual.nth(0).take(3)).containsExactly(0, 0, 0);
            assertThat(actual.nth(1).take(3)).containsExactly(1, 2, 3);
            assertThat(actual.nth(2).take(3)).containsExactly(2, 4, 6);
            assertThat(actual.nth(3).take(3)).containsExactly(3, 6, 9);
        }

        @Nested
        class WithOtherColl {

            @Test
            void returnsEmptySeqWhenProvidingEmptyOther() {
                var sut = new ChunkedCons<>(arrayChunk(List.of("xxx", "ab")), ISeq.of("baz", "foobar"));

                assertThat(sut.map(ISeq.<Integer>of(), (a, b) -> a + b)).isEmpty();
                assertThat(sut.map(ISeq.<Integer>of().iterator(), (a, b) -> a + b)).isEmpty();
                assertThat(sut.map(List.<Integer>of(), (a, b) -> a + b)).isEmpty();
                assertThat(sut.map(Stream.<Integer>of(), (a, b) -> a + b)).isEmpty();
                assertThat(sut.map(new Integer[0], (a, b) -> a + b)).isEmpty();
                assertThat(sut.map("", (a, b) -> "" + a + b)).isEmpty();
            }

            @Test
            void returnsANewSeqWithTheItemsOfBothInitialSeqsAreCombinedUsingF() {
                var sut = new ChunkedCons<>(arrayChunk(List.of(1, 2)), ISeq.of(3));

                assertThat(sut.map(ISeq.of("a", "b", "c"), (a, b) -> a + b)).containsExactly("1a", "2b", "3c");
                assertThat(sut.map(ISeq.of("a", "b", "c").iterator(), (a, b) -> a + b)).containsExactly("1a", "2b", "3c");
                assertThat(sut.map(List.of("a", "b", "c"), (a, b) -> a + b)).containsExactly("1a", "2b", "3c");
                assertThat(sut.map(Stream.of("a", "b", "c"), (a, b) -> a + b)).containsExactly("1a", "2b", "3c");
                assertThat(sut.map(new String[]{"a", "b", "c"}, (a, b) -> a + b)).containsExactly("1a", "2b", "3c");
                assertThat(sut.map("abc", (a, b) -> "" + a + b)).containsExactly("1a", "2b", "3c");
            }

            @Test
            void ignoresRemainingItemsIfOneOfTheSeqsIsExhausted() {
                var sut = new ChunkedCons<>(arrayChunk(List.of(1, 2)), ISeq.of(3));

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

                assertThat(sut.map(ISeq.iterate(0, x -> x + 1), (a, b) -> a + b)).containsExactly(1, 3, 5);
            }

            @Test
            void returnsSeqWithInfiniteSeqsIfMappingResultIsInfinite() {
                var sut = new ChunkedCons<>(arrayChunk(List.of(0, 1, 2)), ISeq.of(3, 4, 5));

                var actual = sut.map(ISeq.iterate(0, x -> x + 1), (a, b) -> ISeq.iterate(a, i -> i + a + b));

                assertThat(actual).hasSize(6);
                assertThat(actual.nth(0).take(3)).containsExactly(0, 0, 0);
                assertThat(actual.nth(1).take(3)).containsExactly(1, 3, 5);
                assertThat(actual.nth(2).take(3)).containsExactly(2, 6, 10);
                assertThat(actual.nth(3).take(3)).containsExactly(3, 9, 15);
            }
        }
    }

    @Nested
    class Mapcat {

        @Test
        void returnsFlattenedMapResult() {
            var sut = new ChunkedCons<>(arrayChunk(List.of(1, 2)), ISeq.of(3, 4));

            assertThat(sut.mapcat(x -> List.of(x, x))).containsExactly(1, 1, 2, 2, 3, 3, 4, 4);
        }

        @Test
        void ignoresEmptyResults() {
            var sut = new ChunkedCons<>(arrayChunk(List.of(0, 1)), ISeq.of(2, 3));

            assertThat(sut.mapcat(x -> x == 0 ? List.of() : List.of(x, x))).containsExactly(1, 1, 2, 2, 3, 3);
        }

        @Test
        void returnsInfiniteLazySeqIfMappingResultIsInfinite() {
            var sut = new ChunkedCons<>(arrayChunk(List.of(0, 1)), ISeq.of(2, 3));

            assertThat(sut.mapcat(x -> ISeq.iterate("Y", y -> y + "Y")).take(4)).containsExactly("Y", "YY", "YYY", "YYYY");
        }

        @Nested
        class WithOtherColl {

            @Test
            void returnsEmptySeqWhenProvidingEmptyOther() {
                var sut = new ChunkedCons<>(arrayChunk(List.of(0, 1)), ISeq.of(2, 3));

                assertThat(sut.mapcat(List.<Integer>of(), (a, b) -> List.of(a + b, a + b))).isEmpty();
            }

            @Test
            void returnsANewSeqWithTheItemsOfBothInitialSeqsAreCombinedUsingF() {
                var sut = new ChunkedCons<>(arrayChunk(List.of(1, 2)), ISeq.of(3));

                assertThat(sut.mapcat(List.of("a", "b", "c"), (a, b) -> List.of(a + b, a + b)))
                        .containsExactly("1a", "1a", "2b", "2b", "3c", "3c");
            }

            @Test
            void ignoresRemainingItemsIfOneOfTheSeqsIsExhausted() {
                var sut = new ChunkedCons<>(arrayChunk(List.of(1, 2)), ISeq.of(3));

                assertThat(sut.mapcat(List.of("a", "b"), (a, b) -> List.of(a + b, a + b)))
                        .containsExactly("1a", "1a", "2b", "2b");
                assertThat(sut.mapcat(List.of("a", "b", "c", "d"), (a, b) -> List.of(a + b, a + b)))
                        .containsExactly("1a", "1a", "2b", "2b", "3c", "3c");
                assertThat(sut.mapcat(ISeq.iterate("Y", y -> y + "Y"), (a, b) -> List.of(a + b, a + b)))
                        .containsExactly("1Y", "1Y", "2YY", "2YY", "3YYY", "3YYY");
            }

            @Test
            void returnsInfiniteLazySeqIfMappingResultIsInfinite() {
                var sut = new ChunkedCons<>(arrayChunk(List.of(1, 2)), ISeq.of(3));
                var other = List.of("a", "b", "c");

                assertThat(sut.mapcat(other, (a, b) -> ISeq.iterate("Y", y -> y + a + b)).take(4)).containsExactly("Y", "Y1a", "Y1a1a", "Y1a1a1a");
            }
        }
    }

    @Nested
    class Take {

        @Test
        void returnsNilWithNegativeItems() {
            var sut = new ChunkedCons<>(arrayChunk(List.of(1, 2, 3)), ISeq.of(4, 5, 6));

            assertThat(sut.take(-1))
                    .isExactlyInstanceOf(Nil.class)
                    .isEmpty();
        }

        @Test
        void returnsNilWithZeroItems() {
            var sut = new ChunkedCons<>(arrayChunk(List.of(1, 2, 3)), ISeq.of(4, 5, 6));

            assertThat(sut.take(0))
                    .isExactlyInstanceOf(Nil.class)
                    .isEmpty();
        }

        @Test
        void returnsConsWithFewerItemsThanChunk() {
            var sut = new ChunkedCons<>(arrayChunk(List.of(1, 2, 3)), ISeq.of(4, 5, 6));

            assertThat(sut.take(2))
                    .isExactlyInstanceOf(Cons.class)
                    .containsExactly(1, 2);
        }

        @Test
        void returnsConsWithSameItemsAsChunk() {
            var sut = new ChunkedCons<>(arrayChunk(List.of(1, 2, 3)), ISeq.of(4, 5, 6));

            assertThat(sut.take(3))
                    .isExactlyInstanceOf(Cons.class)
                    .containsExactly(1, 2, 3);
        }

        @Test
        void returnsConsWithMoreItemsThanChunk() {
            var sut = new ChunkedCons<>(arrayChunk(List.of(1, 2, 3)), ISeq.of(4, 5, 6));

            assertThat(sut.take(4))
                    .isExactlyInstanceOf(ChunkedCons.class)
                    .containsExactly(1, 2, 3, 4);
        }


        @Test
        void returnsUnchangedConsTakingMoreItemsThanPresent() {
            var sut = new ChunkedCons<>(arrayChunk(List.of(1, 2, 3)), ISeq.of(4, 5, 6));

            assertThat(sut.take(6)).containsExactly(1, 2, 3, 4, 5, 6);
            assertThat(sut.take(7)).containsExactly(1, 2, 3, 4, 5, 6);
        }
    }

    @Nested
    class Drop {

        @Test
        void returnsUnchangedSeqWithNegativeItemsToDrop() {
            var sut = new ChunkedCons<>(arrayChunk(List.of(1, 2, 3)), ISeq.of(4, 5, 6));

            assertThat(sut.drop(-1)).containsExactly(1, 2, 3, 4, 5, 6);
        }

        @Test
        void returnsUnchangedSeqWithZeroItemsToDrop() {
            var sut = new ChunkedCons<>(arrayChunk(List.of(1, 2, 3)), ISeq.of(4, 5, 6));

            assertThat(sut.drop(0)).containsExactly(1, 2, 3, 4, 5, 6);
        }

        @Test
        void returnsConsDroppingFewerItemsThanChunk() {
            var sut = new ChunkedCons<>(arrayChunk(List.of(1, 2, 3)), ISeq.of(4, 5, 6));

            assertThat(sut.drop(2)).containsExactly(3, 4, 5, 6);
        }

        @Test
        void returnsConsDroppingSameItemsAsChunk() {
            var sut = new ChunkedCons<>(arrayChunk(List.of(1, 2, 3)), ISeq.of(4, 5, 6));

            assertThat(sut.drop(3)).containsExactly(4, 5, 6);
        }

        @Test
        void returnsConsDroppingMoreItemsThanChunk() {
            var sut = new ChunkedCons<>(arrayChunk(List.of(1, 2, 3)), ISeq.of(4, 5, 6));

            assertThat(sut.drop(4)).containsExactly(5, 6);
        }

        @Test
        void returnsEmptySeqIfMoreItemsAreDroppedThanPresent() {
            var sut = new ChunkedCons<>(arrayChunk(List.of(1, 2, 3)), ISeq.of(4, 5, 6));

            assertThat(sut.drop(6)).isEmpty();
            assertThat(sut.drop(7)).isEmpty();
        }
    }

    @Test
    void strReturnsConcatenatedStringRepresentationsOfAllItems() {
        assertThat(new ChunkedCons<>(arrayChunk(List.of(1, 2)), ISeq.of(3, 4)).str())
                .isEqualTo("1234");
        assertThat(new ChunkedCons<>(arrayChunk(List.of(new Object(), new Object())), ISeq.of(new Object(), new Object())).str())
                .matches("java\\.lang\\.Object@.+java\\.lang\\.Object@.+java\\.lang\\.Object@.+java\\.lang\\.Object@.+");
    }

    @Test
    void reverseReturnsReversedSeq() {
        var sut = new ChunkedCons<>(arrayChunk(List.of(1, 2, 3)), ISeq.of(4, 5, 6));

        assertThat(sut.reverse()).containsExactly(6, 5, 4, 3, 2, 1);
    }

    @Test
    void sizeReturnsSizeOfSeq() {
        var sut = new ChunkedCons<>(arrayChunk(List.of(1, 2, 3)), ISeq.of(4, 5, 6));

        assertThat(sut.size()).isEqualTo(6);
        assertThat(sut.rest().size()).isEqualTo(5);
    }

    @Test
    void runCallsProcForEveryItemPresent() {
        var proc = Mockito.<Consumer<Integer>>mock();

        var sut = new ChunkedCons<>(arrayChunk(List.of(1, 2, 3)), ISeq.of(4, 5, 6));

        sut.run(proc);

        verify(proc).accept(1);
        verify(proc).accept(2);
        verify(proc).accept(3);
        verify(proc).accept(4);
        verify(proc).accept(5);
        verify(proc).accept(6);
        verifyNoMoreInteractions(proc);
    }

    @Test
    void toListReturnsAllItems() {
        var sut = new ChunkedCons<>(arrayChunk(List.of(1, 2, 3)), ISeq.of(4, 5, 6));

        assertThat(sut.toList())
                .isInstanceOf(List.class)
                .containsExactly(1, 2, 3, 4, 5, 6);
    }

    @Test
    void toSetReturnsUniqueItems() {
        var sut = new ChunkedCons<>(arrayChunk(List.of(1, 2, 2)), ISeq.of(6, 3, 6));

        assertThat(sut.toSet())
                .isInstanceOf(Set.class)
                .containsExactlyInAnyOrder(1, 2, 3, 6);
    }
}