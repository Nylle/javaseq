package com.github.nylle.javaseq;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class ChunkedConsTest {

    private static <T> ArrayChunk<T> arrayChunk(T... items) {
        return new ArrayChunk<T>(items, 0, items.length);
    }

    @Test
    void canContainNull() {
        var sut = new ChunkedCons<>(arrayChunk(1, 2, 3, null), ISeq.of(4, 5, 6));

        assertThat(sut).containsExactly(1, 2, 3, null, 4, 5, 6);
    }

    @Test
    void firstReturnsFirstItem() {
        var sut = new ChunkedCons<>(arrayChunk(1, 2, 3), ISeq.of(4, 5, 6));

        assertThat(sut.first()).isEqualTo(1);
    }

    @Nested
    class Second {

        @Test
        void returnsSecondItem() {
            var sut = new ChunkedCons<>(arrayChunk(1, 2, 3), ISeq.of(4, 5, 6));

            assertThat(sut.second()).isEqualTo(2);
        }

        @Test
        void returnsNullIfSeqHasOnlyOneElement() {
            var sut = new ChunkedCons<>(arrayChunk(1), Nil.empty());

            assertThat(sut.second()).isNull();
        }
    }

    @Test
    void restReturnsSeqWithItemsExceptFirst() {
        var sut = new ChunkedCons<>(arrayChunk(1, 2, 3), ISeq.of(4, 5, 6));

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
        assertThat(new ChunkedCons<>(arrayChunk(1, 2, 3), ISeq.of(4, 5, 6)).isRealized()).isTrue();
    }

    @Nested
    class Filter {

        @Test
        void returnsNilWhenNoItemsMatch() {
            var sut = new ChunkedCons<>(arrayChunk(1, 2, 3), ISeq.of(4, 5, 6));

            assertThat(sut.filter(x -> x < 1)).isEmpty();
        }

        @Test
        void returnsMatchingItems() {
            var sut = new ChunkedCons<>(arrayChunk(1, 2, 3), ISeq.of(4, 5, 6));

            assertThat(sut.filter(x -> x > 1 && x < 6)).containsExactly(2, 3, 4, 5);
        }
    }

    @Nested
    class Map {

        @Test
        void returnsSingleMapResult() {
            var sut = new ChunkedCons<>(arrayChunk("xxx"), Nil.empty());

            assertThat(sut.map(x -> x.length())).containsExactly(3);
        }

        @Test
        void returnsAllMapResults() {
            var sut = new ChunkedCons<>(arrayChunk("xxx", "ab"), ISeq.of("baz", "foobar"));

            assertThat(sut.map(x -> x.length())).containsExactly(3, 2, 3, 6);
        }

        @Test
        void returnsInfiniteSeqWithInfiniteSeqsIfMappingResultIsInfinite() {
            var sut = new ChunkedCons<>(arrayChunk(0, 1, 2), ISeq.of(3, 4, 5));

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
                var sut = new ChunkedCons<>(arrayChunk("xxx", "ab"), ISeq.of("baz", "foobar"));

                assertThat(sut.map(Nil.empty(), (a, b) -> a + b)).isEmpty();
                assertThat(sut.map(List.<Integer>of().iterator(), (a, b) -> a + b)).isEmpty();
                assertThat(sut.map(List.<Integer>of(), (a, b) -> a + b)).isEmpty();
                assertThat(sut.map(Stream.<Integer>of(), (a, b) -> a + b)).isEmpty();
                assertThat(sut.map(new Integer[0], (a, b) -> a + b)).isEmpty();
                assertThat(sut.map("", (a, b) -> "" + a + b)).isEmpty();
            }

            @Test
            void returnsANewSeqWithTheItemsOfBothInitialSeqsAreCombinedUsingF() {
                var sut = new ChunkedCons<>(arrayChunk(1, 2), ISeq.of(3));

                assertThat(sut.map(ISeq.of("a", "b", "c"), (a, b) -> a + b)).containsExactly("1a", "2b", "3c");
                assertThat(sut.map(List.of("a", "b", "c").iterator(), (a, b) -> a + b)).containsExactly("1a", "2b", "3c");
                assertThat(sut.map(List.of("a", "b", "c"), (a, b) -> a + b)).containsExactly("1a", "2b", "3c");
                assertThat(sut.map(Stream.of("a", "b", "c"), (a, b) -> a + b)).containsExactly("1a", "2b", "3c");
                assertThat(sut.map(new String[]{"a", "b", "c"}, (a, b) -> a + b)).containsExactly("1a", "2b", "3c");
                assertThat(sut.map("abc", (a, b) -> "" + a + b)).containsExactly("1a", "2b", "3c");
            }

            @Test
            void ignoresRemainingItemsIfOneOfTheSeqsIsExhausted() {
                var sut = new ChunkedCons<>(arrayChunk(1, 2), ISeq.of(3));

                assertThat(sut.map(ISeq.of("a", "b"), (a, b) -> a + b)).containsExactly("1a", "2b");
                assertThat(sut.map(ISeq.of("a", "b", "c", "d"), (a, b) -> a + b)).containsExactly("1a", "2b", "3c");

                assertThat(sut.map(List.of("a", "b").iterator(), (a, b) -> a + b)).containsExactly("1a", "2b");
                assertThat(sut.map(List.of("a", "b", "c", "d").iterator(), (a, b) -> a + b)).containsExactly("1a", "2b", "3c");

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
                var sut = new ChunkedCons<>(arrayChunk(0, 1, 2), ISeq.of(3, 4, 5));

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
            var sut = new ChunkedCons<>(arrayChunk(1, 2), ISeq.of(3, 4));

            assertThat(sut.mapcat(x -> List.of(x, x))).containsExactly(1, 1, 2, 2, 3, 3, 4, 4);
        }

        @Test
        void ignoresEmptyResults() {
            var sut = new ChunkedCons<>(arrayChunk(0, 1), ISeq.of(2, 3));

            assertThat(sut.mapcat(x -> x == 0 ? List.of() : List.of(x, x))).containsExactly(1, 1, 2, 2, 3, 3);
        }

        @Test
        void returnsInfiniteLazySeqIfMappingResultIsInfinite() {
            var sut = new ChunkedCons<>(arrayChunk(0, 1), ISeq.of(2, 3));

            assertThat(sut.mapcat(x -> ISeq.iterate("Y", y -> y + "Y")).take(4)).containsExactly("Y", "YY", "YYY", "YYYY");
        }

        @Nested
        class WithOtherColl {

            @Test
            void returnsEmptySeqWhenProvidingEmptyOther() {
                var sut = new ChunkedCons<>(arrayChunk(0, 1), ISeq.of(2, 3));

                assertThat(sut.mapcat(List.<Integer>of(), (a, b) -> List.of(a + b, a + b))).isEmpty();
            }

            @Test
            void returnsANewSeqWithTheItemsOfBothInitialSeqsAreCombinedUsingF() {
                var sut = new ChunkedCons<>(arrayChunk(1, 2), ISeq.of(3));

                assertThat(sut.mapcat(List.of("a", "b", "c"), (a, b) -> List.of(a + b, a + b)))
                        .containsExactly("1a", "1a", "2b", "2b", "3c", "3c");
            }

            @Test
            void ignoresRemainingItemsIfOneOfTheSeqsIsExhausted() {
                var sut = new ChunkedCons<>(arrayChunk(1, 2), ISeq.of(3));

                assertThat(sut.mapcat(List.of("a", "b"), (a, b) -> List.of(a + b, a + b)))
                        .containsExactly("1a", "1a", "2b", "2b");
                assertThat(sut.mapcat(List.of("a", "b", "c", "d"), (a, b) -> List.of(a + b, a + b)))
                        .containsExactly("1a", "1a", "2b", "2b", "3c", "3c");
                assertThat(sut.mapcat(ISeq.iterate("Y", y -> y + "Y"), (a, b) -> List.of(a + b, a + b)))
                        .containsExactly("1Y", "1Y", "2YY", "2YY", "3YYY", "3YYY");
            }

            @Test
            void returnsInfiniteLazySeqIfMappingResultIsInfinite() {
                var sut = new ChunkedCons<>(arrayChunk(1, 2), ISeq.of(3));
                var other = List.of("a", "b", "c");

                assertThat(sut.mapcat(other, (a, b) -> ISeq.iterate("Y", y -> y + a + b)).take(4)).containsExactly("Y", "Y1a", "Y1a1a", "Y1a1a1a");
            }
        }
    }

    @Nested
    class Take {

        @Test
        void returnsNilWithNegativeItems() {
            var sut = new ChunkedCons<>(arrayChunk(1, 2, 3), ISeq.of(4, 5, 6));

            assertThat(sut.take(-1)).isEqualTo(Nil.empty());
        }

        @Test
        void returnsNilWithZeroItems() {
            var sut = new ChunkedCons<>(arrayChunk(1, 2, 3), ISeq.of(4, 5, 6));

            assertThat(sut.take(0)).isEqualTo(Nil.empty());
        }

        @Test
        void returnsSeqWithFewerItemsThanChunk() {
            var sut = new ChunkedCons<>(arrayChunk(1, 2, 3), ISeq.of(4, 5, 6));

            assertThat(sut.take(2))
                    .isExactlyInstanceOf(LazySeq.class)
                    .containsExactly(1, 2);
        }

        @Test
        void returnsSeqWithSameItemsAsChunk() {
            var sut = new ChunkedCons<>(arrayChunk(1, 2, 3), ISeq.of(4, 5, 6));

            assertThat(sut.take(3))
                    .isExactlyInstanceOf(LazySeq.class)
                    .containsExactly(1, 2, 3);
        }

        @Test
        void returnsSeqWithMoreItemsThanChunk() {
            var sut = new ChunkedCons<>(arrayChunk(1, 2, 3), ISeq.of(4, 5, 6));

            assertThat(sut.take(4))
                    .isExactlyInstanceOf(LazySeq.class)
                    .containsExactly(1, 2, 3, 4);
        }

        @Test
        void returnsUnchangedConsTakingMoreItemsThanPresent() {
            var sut = new ChunkedCons<>(arrayChunk(1, 2, 3), ISeq.of(4, 5, 6));

            assertThat(sut.take(6)).containsExactly(1, 2, 3, 4, 5, 6);
            assertThat(sut.take(7)).containsExactly(1, 2, 3, 4, 5, 6);
        }
    }

    @Nested
    class Drop {

        @Test
        void returnsUnchangedSeqWithNegativeItemsToDrop() {
            var sut = new ChunkedCons<>(arrayChunk(1, 2, 3), ISeq.of(4, 5, 6));

            assertThat(sut.drop(-1)).containsExactly(1, 2, 3, 4, 5, 6);
        }

        @Test
        void returnsUnchangedSeqWithZeroItemsToDrop() {
            var sut = new ChunkedCons<>(arrayChunk(1, 2, 3), ISeq.of(4, 5, 6));

            assertThat(sut.drop(0)).containsExactly(1, 2, 3, 4, 5, 6);
        }

        @Test
        void returnsSeqDroppingFewerItemsThanChunk() {
            var sut = new ChunkedCons<>(arrayChunk(1, 2, 3), ISeq.of(4, 5, 6));

            assertThat(sut.drop(2)).containsExactly(3, 4, 5, 6);
        }

        @Test
        void returnsSeqDroppingSameItemsAsChunk() {
            var sut = new ChunkedCons<>(arrayChunk(1, 2, 3), ISeq.of(4, 5, 6));

            assertThat(sut.drop(3).toList()).containsExactly(4, 5, 6);
        }

        @Test
        void returnsSeqDroppingMoreItemsThanChunk() {
            var sut = new ChunkedCons<>(arrayChunk(1, 2, 3), ISeq.of(4, 5, 6));

            assertThat(sut.drop(4)).containsExactly(5, 6);
        }

        @Test
        void returnsEmptySeqIfMoreItemsAreDroppedThanPresent() {
            var sut = new ChunkedCons<>(arrayChunk(1, 2, 3), ISeq.of(4, 5, 6));

            assertThat(sut.drop(6)).isEmpty();
            assertThat(sut.drop(7)).isEmpty();
        }
    }

    @Nested
    class TakeWhile {

        @Test
        void returnsEmptySeqWhenFirstItemDoesNotMatch() {
            var sut = new ChunkedCons<>(arrayChunk(1, 2, 3), ISeq.of(4, 5, 6));

            assertThat(sut.takeWhile(x -> x < 1)).isEmpty();
        }

        @Test
        void returnsSeqWithFewerItemsThanChunk() {
            var sut = new ChunkedCons<>(arrayChunk(1, 2, 3), ISeq.of(4, 5, 6));

            assertThat(sut.takeWhile(x -> x < 3))
                    .isExactlyInstanceOf(LazySeq.class)
                    .containsExactly(1, 2);
        }

        @Test
        void returnsSeqWithSameItemsAsChunk() {
            var sut = new ChunkedCons<>(arrayChunk(1, 2, 3), ISeq.of(4, 5, 6));

            assertThat(sut.takeWhile(x -> x < 4))
                    .isExactlyInstanceOf(LazySeq.class)
                    .containsExactly(1, 2, 3);
        }

        @Test
        void returnsSeqWithMoreItemsThanChunk() {
            var sut = new ChunkedCons<>(arrayChunk(1, 2, 3), ISeq.of(4, 5, 6));

            assertThat(sut.takeWhile(x -> x < 5))
                    .isExactlyInstanceOf(LazySeq.class)
                    .containsExactly(1, 2, 3, 4);
        }

        @Test
        void returnsSeqWithAllItems() {
            var sut = new ChunkedCons<>(arrayChunk(1, 2, 3), ISeq.of(4, 5, 6));

            assertThat(sut.takeWhile(x -> true)).containsExactly(1, 2, 3, 4, 5, 6);
        }
    }

    @Nested
    class DropWhile {

        @Test
        void returnsEmptySeqWhenAllItemsMatch() {
            var sut = new ChunkedCons<>(arrayChunk(1, 2, 3), ISeq.of(4, 5, 6));

            assertThat(sut.dropWhile(x -> x > 0)).isEmpty();
        }

        @Test
        void returnsSeqDroppingFewerItemsThanChunk() {
            var sut = new ChunkedCons<>(arrayChunk(1, 2, 3), ISeq.of(4, 5, 6));

            assertThat(sut.dropWhile(x -> x < 3))
                    .isExactlyInstanceOf(LazySeq.class)
                    .containsExactly(3, 4, 5, 6);
        }

        @Test
        void returnsSeqDroppingSameItemsAsChunk() {
            var sut = new ChunkedCons<>(arrayChunk(1, 2, 3), ISeq.of(4, 5, 6));

            assertThat(sut.dropWhile(x -> x < 4))
                    .isExactlyInstanceOf(LazySeq.class)
                    .containsExactly(4, 5, 6);
        }

        @Test
        void returnsSeqDroppingMoreItemsThanChunk() {
            var sut = new ChunkedCons<>(arrayChunk(1, 2, 3), ISeq.of(4, 5, 6));

            assertThat(sut.dropWhile(x -> x < 5))
                    .isExactlyInstanceOf(LazySeq.class)
                    .containsExactly(5, 6);
        }

        @Test
        void returnsEntireSeqWhenFirstItemDoesNotMatch() {
            var sut = new ChunkedCons<>(arrayChunk(1, 2, 3), ISeq.of(4, 5, 6));

            assertThat(sut.dropWhile(x -> x > 2)).containsExactly(1, 2, 3, 4, 5, 6);
        }

        @Test
        void returnsEntireSeqWhenNoItemMatches() {
            var sut = new ChunkedCons<>(arrayChunk(1, 2, 3), ISeq.of(4, 5, 6));

            assertThat(sut.dropWhile(x -> false)).containsExactly(1, 2, 3, 4, 5, 6);
        }
    }

    @Nested
    class Reductions {

        @Test
        void returnsASeqWithTheIntermediateValuesOfTheReduction() {
            var sut = new ChunkedCons<>(arrayChunk("1", "2", "3"), ISeq.of("4", "5", "6"));

            assertThat(sut.reductions((a, b) -> a + b)).containsExactly("1", "12", "123", "1234", "12345", "123456");
        }

        @Test
        void returnsASeqWithTheIntermediateValuesOfTheReductionStartingWithInit() {
            var sut = new ChunkedCons<>(arrayChunk("1", "2", "3"), ISeq.of("4", "5", "6"));

            assertThat(sut.reductions("0", (a, b) -> a + b.toString())).containsExactly("0", "01", "012", "0123", "01234", "012345", "0123456");
        }
    }

    @Nested
    class Reduce {

        @Test
        void returnsOptionalResultWhenValIsNotSupplied() {
            var sut = new ChunkedCons<>(arrayChunk(1, 2, 3), ISeq.of(4, 5, 6));

            assertThat(sut.reduce((a, b) -> a + b)).hasValue(21);
        }

        @Test
        void returnsResultWhenValIsSupplied() {
            var sut = new ChunkedCons<>(arrayChunk(2, 3, 4), ISeq.of(5, 6, 7));

            assertThat(sut.reduce(1, (a, b) -> a + b)).isEqualTo(28);
        }

        @Test
        void returnsResultOfDifferentTypeThanSeq() {
            var sut = new ChunkedCons<>(arrayChunk("a", "bb", "ccc"), ISeq.of("dddd", "eeeee", "ffffff"));

            assertThat(sut.reduce(0, (acc, x) -> acc + x.length())).isEqualTo(21);
        }
    }

    @Nested
    class Some {

        @Test
        void returnsFalseIfNoneOfTheItemsMatchPred() {
            var sut = new ChunkedCons<>(arrayChunk(1, 2, 3), ISeq.of(4, 5, 6));

            assertThat(sut.take(10).some(x -> x < 0)).isFalse();
        }

        @Test
        void returnsTrueIfAllItemsMatchPred() {
            var sut = new ChunkedCons<>(arrayChunk(1, 2, 3), ISeq.of(4, 5, 6));

            assertThat(sut.some(x -> x >= 0)).isTrue();
        }

        @Test
        void returnsTrueIfFirstItemMatchesPred() {
            var sut = new ChunkedCons<>(arrayChunk(1, 2, 3), ISeq.of(4, 5, 6));

            assertThat(sut.some(x -> x == 1)).isTrue();
        }

        @Test
        void returnsTrueIfSomeItemMatchesPred() {
            var sut = new ChunkedCons<>(arrayChunk(1, 2, 3), ISeq.of(4, 5, 6));

            assertThat(sut.some(x -> x == 2)).isTrue();
            assertThat(sut.some(x -> x == 5)).isTrue();
        }

        @Test
        void returnsTrueIfLastItemMatchesPred() {
            var sut = new ChunkedCons<>(arrayChunk(1, 2, 3), ISeq.of(4, 5, 6));

            assertThat(sut.some(x -> x == 3)).isTrue();
            assertThat(sut.some(x -> x == 6)).isTrue();
        }
    }

    @Nested
    class Every {

        @Test
        void returnsTrueIfAllItemsInSeqMatchPred() {
            var sut = new ChunkedCons<>(arrayChunk(1, 2, 3), ISeq.of(4, 5, 6));

            assertThat(sut.every(x -> x > 0)).isTrue();
        }

        @Test
        void returnsFalseIfFirstItemDoesNotMatchPred() {
            var sut = new ChunkedCons<>(arrayChunk(1, 2, 3), ISeq.of(4, 5, 6));

            assertThat(sut.every(x -> x == 0)).isFalse();
        }

        @Test
        void returnsFalseIfAnyItemDoesNotMatchPred() {
            var sut = new ChunkedCons<>(arrayChunk(1, 2, 3), ISeq.of(4, 5, 6));

            assertThat(sut.every(x -> x < 2)).isFalse();
            assertThat(sut.every(x -> x < 5)).isFalse();
        }

        @Test
        void returnsFalseIfLastItemDoesNotMatchPred() {
            var sut = new ChunkedCons<>(arrayChunk(1, 2, 3), ISeq.of(4, 5, 6));

            assertThat(sut.every(x -> x < 3)).isFalse();
            assertThat(sut.every(x -> x < 6)).isFalse();
        }
    }

    @Nested
    class Max {

        @Test
        void returnsSingleItem() {
            var sut = new ChunkedCons<>(arrayChunk(1), Nil.empty());

            assertThat(sut.max(Comparator.naturalOrder())).hasValue(1);
        }

        @Test
        void returnsHighestNumber() {
            var sut = new ChunkedCons<>(arrayChunk(1, 2, 3), ISeq.of(4, 5, 6));

            assertThat(sut.max(Comparator.naturalOrder())).hasValue(6);
        }

        @Test
        void returnsLongestString() {
            var sut = new ChunkedCons<>(arrayChunk("x", "xx", "xxx"), ISeq.of("xxxx", "xxxxx", "xxxxxx"));

            assertThat(sut.max(Comparator.comparingInt(x -> x.length()))).hasValue("xxxxxx");
        }

        @Test
        void returnsTheLastOccurrenceOfLongestStringIfMoreThanOneItemFound() {
            var sut = new ChunkedCons<>(arrayChunk("x", "xx", "aaa"), ISeq.of("", "x", "bbb"));

            assertThat(sut.max(Comparator.comparingInt(x -> x.length()))).hasValue("bbb");
        }
    }

    @Nested
    class Min {

        @Test
        void returnsSingleItem() {
            var sut = new ChunkedCons<>(arrayChunk(1), Nil.empty());

            assertThat(sut.min(Comparator.naturalOrder())).hasValue(1);
        }

        @Test
        void returnsLowestNumber() {
            var sut = new ChunkedCons<>(arrayChunk(-5, -4, -3, -2), ISeq.of(-1, 0, 1, 2));

            assertThat(sut.min(Comparator.naturalOrder())).hasValue(-5);
        }

        @Test
        void returnsShortestString() {
            var sut = new ChunkedCons<>(arrayChunk("xxxxxx", "xxxxx", "xxxx"), ISeq.of("x", "xx", "xxx"));

            assertThat(sut.min(Comparator.comparingInt(x -> x.length()))).hasValue("x");
        }

        @Test
        void returnsTheLastOccurrenceOfShortestStringIfMoreThanOneItemFound() {
            var sut = new ChunkedCons<>(arrayChunk("a", "xx", "aaa"), ISeq.of("x", "bbb", "b"));

            assertThat(sut.min(Comparator.comparingInt(x -> x.length()))).hasValue("b");
        }
    }

    @Nested
    class Nth {

        @Test
        void returnsValueAtIndex() {
            var sut = new ChunkedCons<>(arrayChunk("", null, "1"), ISeq.of("2", null, "4"));

            assertThat(sut.nth(0)).isEqualTo("");
            assertThat(sut.nth(1)).isEqualTo(null);
            assertThat(sut.nth(2)).isEqualTo("1");
            assertThat(sut.nth(3)).isEqualTo("2");
            assertThat(sut.nth(4)).isEqualTo(null);
            assertThat(sut.nth(5)).isEqualTo("4");
        }

        @Test
        void returnsDefaultValue() {
            var sut = new ChunkedCons<>(arrayChunk("0", null, "2"), ISeq.of("3", null, "4"));

            assertThat(sut.nth(-1, "X")).isEqualTo("X");
            assertThat(sut.nth(0, "X")).isEqualTo("0");
            assertThat(sut.nth(1, "X")).isEqualTo(null);
            assertThat(sut.nth(2, "X")).isEqualTo("2");
            assertThat(sut.nth(3, "X")).isEqualTo("3");
            assertThat(sut.nth(4, "X")).isEqualTo(null);
            assertThat(sut.nth(5, "X")).isEqualTo("4");
            assertThat(sut.nth(6, "X")).isEqualTo("X");
            assertThat(sut.nth(7, "X")).isEqualTo("X");
        }

        @Test
        void throwsForNegativeIndex() {
            var sut = new ChunkedCons<>(arrayChunk("", "0", "01"), ISeq.of("012", "0123", "01234"));

            assertThatExceptionOfType(IndexOutOfBoundsException.class)
                    .isThrownBy(() -> sut.nth(-1))
                    .withMessage("Index out of range: -1");
        }

        @Test
        void throwsIfIndexNotPresent() {
            var sut = new ChunkedCons<>(arrayChunk("", "0", "01"), ISeq.of("012", "0123", "01234"));

            assertThatExceptionOfType(IndexOutOfBoundsException.class)
                    .isThrownBy(() -> sut.nth(6))
                    .withMessage("Index out of range: 6");
        }
    }

    @Test
    void strReturnsConcatenatedStringRepresentationsOfAllItems() {
        assertThat(new ChunkedCons<>(arrayChunk(1, 2), ISeq.of(3, 4)).str())
                .isEqualTo("1234");
        assertThat(new ChunkedCons<>(arrayChunk(new Object(), new Object()), ISeq.of(new Object(), new Object())).str())
                .matches("java\\.lang\\.Object@.+java\\.lang\\.Object@.+java\\.lang\\.Object@.+java\\.lang\\.Object@.+");
    }

    @Test
    void reverseReturnsReversedSeq() {
        var sut = new ChunkedCons<>(arrayChunk(1, 2, 3), ISeq.of(4, 5, 6));

        assertThat(sut.reverse()).containsExactly(6, 5, 4, 3, 2, 1);
    }

    @Test
    void countReturnsSizeOfSeq() {
        var sut = new ChunkedCons<>(arrayChunk(1, 2, 3), ISeq.of(4, 5, 6));

        assertThat(sut.count()).isEqualTo(6);
        assertThat(sut.rest().count()).isEqualTo(5);
    }

    @Test
    void runCallsProcForEveryItemPresent() {
        var proc = Mockito.<Consumer<Integer>>mock();

        var sut = new ChunkedCons<>(arrayChunk(1, 2, 3), ISeq.of(4, 5, 6));

        sut.run(proc);

        verify(proc).accept(1);
        verify(proc).accept(2);
        verify(proc).accept(3);
        verify(proc).accept(4);
        verify(proc).accept(5);
        verify(proc).accept(6);
        verifyNoMoreInteractions(proc);
    }

    @Nested
    class ToList {

        @Test
        void throwsWhenChunkContainsNull() {
            var sut = new ChunkedCons<>(arrayChunk(1, 2, 3, null), ISeq.of(4, 5, 6));

            assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> sut.toList());
        }

        @Test
        void throwsWhenRestContainsNull() {
            var sut = new ChunkedCons<>(arrayChunk(1, 2, 3), ISeq.of(4, 5, 6, null));

            assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> sut.toList());
        }

        @Test
        void returnsAllItems() {
            var sut = new ChunkedCons<>(arrayChunk(1, 2, 3), ISeq.of(4, 5, 6));

            assertThat(sut.toList())
                    .isInstanceOf(List.class)
                    .containsExactly(1, 2, 3, 4, 5, 6);
        }
    }

    @Test
    void toSetReturnsUniqueItems() {
        var sut = new ChunkedCons<>(arrayChunk(1, 2, 2), ISeq.of(6, 3, 6));

        assertThat(sut.toSet())
                .isInstanceOf(Set.class)
                .containsExactlyInAnyOrder(1, 2, 3, 6);
    }
}