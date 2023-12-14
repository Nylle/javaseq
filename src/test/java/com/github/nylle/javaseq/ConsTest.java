package com.github.nylle.javaseq;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class ConsTest {

    private static <T> ISeq<T> sutFrom(T... items) {
        ISeq<T> cons = Nil.empty();
        for (int i = items.length - 1; i >= 0; i--) {
            cons = new Cons<>(items[i], cons);
        }
        return cons;
    }

    @Test
    void firstReturnsFirstItem() {
        var sut = new Cons<>(0, new Cons<>(1, new Cons<>(2, new Cons<>(3, Nil.empty()))));

        assertThat(sut.first()).isEqualTo(0);
    }

    @Nested
    class Second {

        @Test
        void returnsSecondItem() {
            var sut = new Cons<>(1, new Cons<>(2, new Cons<>(3, Nil.empty())));

            assertThat(sut.second()).isEqualTo(2);
        }

        @Test
        void returnsNullIfSeqHasOnlyOneElement() {
            var sut = new Cons<>(1, Nil.empty());

            assertThat(sut.second()).isNull();
        }
    }

    @Test
    void restReturnsSeqWithItemsExceptFirst() {
        var sut = new Cons<>(0, new Cons<>(1, new Cons<>(2, new Cons<>(3, Nil.empty()))));

        var rest = sut.rest();

        assertThat(rest.first()).isEqualTo(1);
        assertThat(rest.rest().first()).isEqualTo(2);
        assertThat(rest.rest().rest().first()).isEqualTo(3);
        assertThat(rest.rest().rest().rest()).isEmpty();
    }

    @Nested
    class IsRealized {

        @Test
        void returnsTrueForSingleItem() {
            var sut = sutFrom(0);

            assertThat(sut.isRealized()).isTrue();
        }

        @Test
        void returnsTrueForAnyNumberOfItems() {
            var sut = sutFrom(1, 2, 3, 4);

            assertThat(sut.isRealized()).isTrue();
        }
    }

    @Nested
    class Take {

        @Test
        void returnsNilWithNegativeItems() {
            var sut = sutFrom(0, 1, 2, 3);

            assertThat(sut.take(-1)).isEqualTo(Nil.empty());
        }

        @Test
        void returnsNilWithZeroItems() {
            var sut = sutFrom(0, 1, 2, 3);

            assertThat(sut.take(0)).isEqualTo(Nil.empty());
        }

        @Test
        void returnsConsWithMoreThanZeroItems() {
            var sut = sutFrom(0, 1, 2, 3);

            assertThat(sut.take(3))
                    .isExactlyInstanceOf(LazySeq.class)
                    .containsExactly(0, 1, 2);
        }
    }

    @Nested
    class Drop {

        @Test
        void returnsUnchangedSeqWithNegativeItemsToDrop() {
            assertThat(sutFrom(1, 2, 3, 4).drop(-1)).containsExactly(1, 2, 3, 4);
        }

        @Test
        void returnsUnchangedSeqWithZeroItemsToDrop() {
            assertThat(sutFrom(1, 2, 3, 4).drop(0)).containsExactly(1, 2, 3, 4);
        }

        @Test
        void returnsSeqOfAllButTheFirstNItems() {
            assertThat(sutFrom(1, 2, 3, 4).drop(2)).containsExactly(3, 4);
        }

        @Test
        void returnsEmptySeqIfMoreItemsAreDroppedThanPresent() {
            assertThat(sutFrom(1, 2, 3, 4).drop(5)).isEmpty();
        }
    }

    @Nested
    class Filter {

        @Test
        void returnsNilWhenNoItemsMatch() {
            var sut = sutFrom(0, 1, 2, 3);

            assertThat(sut.filter(x -> x < 0)).isEmpty();
        }

        @Test
        void returnsMatchingItems() {
            var sut = sutFrom(0, 1, 2, 3);

            assertThat(sut.filter(x -> x > 1)).containsExactly(2, 3);
        }
    }

    @Nested
    class Map {

        @Test
        void returnsSingleMapResult() {
            var sut = sutFrom("xxx");

            assertThat(sut.map(x -> x.length())).containsExactly(3);
        }

        @Test
        void returnsAllMapResults() {
            var sut = sutFrom("xxx", "ab", "baz", "foobar");

            assertThat(sut.map(x -> x.length())).containsExactly(3, 2, 3, 6);
        }

        @Test
        void returnsSeqWithInfiniteSeqs() {
            var sut = sutFrom(0, 1, 2, 3);

            var actual = sut.map(x -> ISeq.iterate(x, i -> i + x));

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
                assertThat(sutFrom(1, 2, 3).map(Nil.<Integer>empty(), (a, b) -> a + b)).isEmpty();
                assertThat(sutFrom(1, 2, 3).map(Collections.<Integer>emptyIterator(), (a, b) -> a + b)).isEmpty();
                assertThat(sutFrom(1, 2, 3).map(List.<Integer>of(), (a, b) -> a + b)).isEmpty();
                assertThat(sutFrom(1, 2, 3).map(Stream.<Integer>of(), (a, b) -> a + b)).isEmpty();
                assertThat(sutFrom(1, 2, 3).map(new Integer[0], (a, b) -> a + b)).isEmpty();
                assertThat(sutFrom(1, 2, 3).map("", (a, b) -> "" + a + b)).isEmpty();
            }

            @Test
            void returnsANewSeqWithTheItemsOfBothInitialSeqsAreCombinedUsingF() {
                var sut = sutFrom(1, 2, 3);

                assertThat(sut.map(ISeq.of("a", "b", "c"), (a, b) -> a + b)).containsExactly("1a", "2b", "3c");
                assertThat(sut.map(List.of("a", "b", "c").iterator(), (a, b) -> a + b)).containsExactly("1a", "2b", "3c");
                assertThat(sut.map(List.of("a", "b", "c"), (a, b) -> a + b)).containsExactly("1a", "2b", "3c");
                assertThat(sut.map(Stream.of("a", "b", "c"), (a, b) -> a + b)).containsExactly("1a", "2b", "3c");
                assertThat(sut.map(new String[]{"a", "b", "c"}, (a, b) -> a + b)).containsExactly("1a", "2b", "3c");
                assertThat(sut.map("abc", (a, b) -> "" + a + b)).containsExactly("1a", "2b", "3c");
            }

            @Test
            void ignoresRemainingItemsIfOneOfTheSeqsIsExhausted() {
                var sut = sutFrom(1, 2, 3);

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
                var sut = sutFrom(0, 1, 2, 3);

                var actual = sut.map(ISeq.iterate(0, x -> x + 1), (a, b) -> ISeq.iterate(a, i -> i + a + b));

                assertThat(actual).hasSize(4);
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
            var sut = sutFrom(0, 1, 2);

            assertThat(sut.mapcat(x -> List.of(x, x))).containsExactly(0, 0, 1, 1, 2, 2);
        }

        @Test
        void returnsFlattenedSeq() {
            var sut = sutFrom(sutFrom(0, 1, 2), sutFrom(3, 4, 5));

            assertThat(sut.mapcat(x -> x)).containsExactly(0, 1, 2, 3, 4, 5);
        }

        @Test
        void ignoresEmptyResults() {
            var sut = sutFrom(0, 1, 2, 3);

            assertThat(sut.mapcat(x -> x == 0 ? List.of() : List.of(x, x))).containsExactly(1, 1, 2, 2, 3, 3);
        }

        @Test
        void returnsInfiniteLazySeqIfMappingResultIsInfinite() {
            var sut = sutFrom(0, 1, 2, 3);

            assertThat(sut.mapcat(x -> ISeq.iterate("Y", y -> y + "Y")).take(4)).containsExactly("Y", "YY", "YYY", "YYYY");
        }

        @Nested
        class WithOtherColl {

            @Test
            void returnsEmptySeqWhenProvidingEmptyOther() {
                assertThat(sutFrom(1, 2, 3).mapcat(List.<Integer>of(), (a, b) -> List.of(a + b, a + b))).isEmpty();
            }

            @Test
            void returnsANewSeqWithTheItemsOfBothInitialSeqsAreCombinedUsingF() {
                var sut = sutFrom(1, 2, 3);

                assertThat(sut.mapcat(List.of("a", "b", "c"), (a, b) -> List.of(a + b, a + b)))
                        .containsExactly("1a", "1a", "2b", "2b", "3c", "3c");
            }

            @Test
            void ignoresRemainingItemsIfOneOfTheSeqsIsExhausted() {
                var sut = sutFrom(1, 2, 3);

                assertThat(sut.mapcat(List.of("a", "b"), (a, b) -> List.of(a + b, a + b)))
                        .containsExactly("1a", "1a", "2b", "2b");
                assertThat(sut.mapcat(List.of("a", "b", "c", "d"), (a, b) -> List.of(a + b, a + b)))
                        .containsExactly("1a", "1a", "2b", "2b", "3c", "3c");
                assertThat(sut.mapcat(ISeq.iterate("Y", y -> y + "Y"), (a, b) -> List.of(a + b, a + b)))
                        .containsExactly("1Y", "1Y", "2YY", "2YY", "3YYY", "3YYY");
            }

            @Test
            void returnsInfiniteLazySeqIfMappingResultIsInfinite() {
                var sut = sutFrom(1, 2, 3);
                var other = List.of("a", "b", "c");

                assertThat(sut.mapcat(other, (a, b) -> ISeq.iterate("Y", y -> y + a + b)).take(4)).containsExactly("Y", "Y1a", "Y1a1a", "Y1a1a1a");
            }
        }
    }

    @Nested
    class TakeWhile {

        @Test
        void returnsEmptySeqWhenFirstItemDoesNotMatch() {
            assertThat(sutFrom(1).takeWhile(x -> x > 1)).isEmpty();
        }

        @Test
        void returnsSeqWithSingleMatchingItem() {
            assertThat(sutFrom(1).takeWhile(x -> x > 0)).containsExactly(1);
        }

        @Test
        void returnsSeqWithMatchingItems() {
            assertThat(sutFrom(0, 1, 2, 3).takeWhile(x -> x < 3)).containsExactly(0, 1, 2);
        }

        @Test
        void returnsSeqWithAllMatchingItems() {
            assertThat(sutFrom(1, 2, 3).takeWhile(x -> true)).containsExactly(1, 2, 3);
        }
    }

    @Nested
    class DropWhile {

        @Test
        void returnsEmptySeqWhenAllItemsMatch() {
            assertThat(sutFrom(1, 2, 3, 4).dropWhile(x -> x > 0)).isEmpty();
        }

        @Test
        void returnsSeqWithItemsThatDoNotMatch() {
            assertThat(sutFrom(1, 2, 3, 4).dropWhile(x -> x < 3)).containsExactly(3, 4);
        }

        @Test
        void returnsEntireSeqWhenFirstItemDoesNotMatch() {
            assertThat(sutFrom(1, 2, 3, 4).dropWhile(x -> x > 2)).containsExactly(1, 2, 3, 4);
        }
    }

    @Nested
    class Partition {

        @Test
        void returnsEmptySeqForNegativeSizeN() {
            var sut = sutFrom(1, 2, 3);

            assertThat(sut.partition(-1)).isEmpty();
            assertThat(sut.partition(-1, 10)).isEmpty();
            assertThat(sut.partition(-1, 3)).isEmpty();
            assertThat(sut.partition(-1, 1)).isEmpty();
            assertThat(sut.partition(-1, 0)).isEmpty();
            assertThat(sut.partition(-1, -1)).isEmpty();
        }

        @Test
        void returnsInfiniteSeqOfEmptyListsForZeroSizeN() {
            var sut = sutFrom(1, 2, 3);

            assertThat(sut.partition(0).take(2)).containsExactly(
                    List.of(),
                    List.of());

            assertThat(sut.partition(0, 1).take(2)).containsExactly(
                    List.of(),
                    List.of());

            assertThat(sut.partition(0, 0).take(2)).containsExactly(
                    List.of(),
                    List.of());

            assertThat(sut.partition(0, -1).take(2)).containsExactly(
                    List.of(),
                    List.of());
        }

        @Test
        void returnsSeqOfListsOf1ItemEachAtOffsetsStepApart() {
            var sut = sutFrom(0, 1, 2, 3, 4);

            assertThat(sut.partition(1).take(3)).containsExactly(
                    List.of(0),
                    List.of(1),
                    List.of(2));

            assertThat(sut.partition(1, 1).take(3)).containsExactly(
                    List.of(0),
                    List.of(1),
                    List.of(2));

            assertThat(sut.partition(1, 2).take(3)).containsExactly(
                    List.of(0),
                    List.of(2),
                    List.of(4));
        }

        @Test
        void returnsSeqOfListsOfNItemsEachAtOffsetsStepApart() {
            var sut = sutFrom(0, 1, 2, 3, 4, 5, 6, 7, 8);

            assertThat(sut.partition(3).take(3)).containsExactly(
                    List.of(0, 1, 2),
                    List.of(3, 4, 5),
                    List.of(6, 7, 8));

            assertThat(sut.partition(3, 3).take(3)).containsExactly(
                    List.of(0, 1, 2),
                    List.of(3, 4, 5),
                    List.of(6, 7, 8));

            assertThat(sut.partition(2, 3).take(3)).containsExactly(
                    List.of(0, 1),
                    List.of(3, 4),
                    List.of(6, 7));
        }

        @Test
        void dropsItemsThatDoNotMakeACompleteLastPartition() {
            var sut = sutFrom(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

            assertThat(sut.partition(4)).containsExactly(
                    List.of(0, 1, 2, 3),
                    List.of(4, 5, 6, 7));

            assertThat(sut.partition(4, 4)).containsExactly(
                    List.of(0, 1, 2, 3),
                    List.of(4, 5, 6, 7));

            assertThat(sut.partition(2, 4)).containsExactly(
                    List.of(0, 1),
                    List.of(4, 5),
                    List.of(8, 9));
        }

        @Test
        void returnsSeqOfOneEmptyListForStepGreaterThanOrEqualToSizeN() {
            var sut = sutFrom(1, 2, 3);

            assertThat(sut.partition(0, 3)).containsExactly(
                    List.of());

            assertThat(sut.partition(0, 4)).containsExactly(
                    List.of());
        }

        @Test
        void returnsASlidingWindowIfStepIsLowerThanSizeN() {
            var sut = sutFrom(0, 1, 2, 3, 4, 5, 6);

            assertThat(sut.partition(3, 2)).containsExactly(
                    List.of(0, 1, 2),
                    List.of(2, 3, 4),
                    List.of(4, 5, 6));
        }

        @Nested
        class WhenPadIsProvidedAndNotNull {

            @Test
            void fillsIncompleteLastPartitionWithItemsFromPad() {
                var sut = sutFrom(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13);

                assertThat(sut.partition(4, 4, List.of(-1, -2, -3, -4))).containsExactly(
                        List.of(0, 1, 2, 3),
                        List.of(4, 5, 6, 7),
                        List.of(8, 9, 10, 11),
                        List.of(12, 13, -1, -2));

                assertThat(sut.partition(3, 4, List.of(-1, -2, -3, -4))).containsExactly(
                        List.of(0, 1, 2),
                        List.of(4, 5, 6),
                        List.of(8, 9, 10),
                        List.of(12, 13, -1));
            }

            @Test
            void returnsAnIncompleteLastPartitionIfItemsInPadAreFewerThanRequired() {
                var sut = sutFrom(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13);

                assertThat(sut.partition(4, 4, List.of())).containsExactly(
                        List.of(0, 1, 2, 3),
                        List.of(4, 5, 6, 7),
                        List.of(8, 9, 10, 11),
                        List.of(12, 13));

                assertThat(sut.partition(3, 4, List.of())).containsExactly(
                        List.of(0, 1, 2),
                        List.of(4, 5, 6),
                        List.of(8, 9, 10),
                        List.of(12, 13));
            }
        }
    }

    @Nested
    class PartitionAll {

        @Test
        void returnsEmptySeqForNegativeSizeN() {
            var sut = sutFrom(1, 2, 3);

            assertThat(sut.partitionAll(-1)).isEmpty();
            assertThat(sut.partitionAll(-1, 10)).isEmpty();
            assertThat(sut.partitionAll(-1, 3)).isEmpty();
            assertThat(sut.partitionAll(-1, 1)).isEmpty();
            assertThat(sut.partitionAll(-1, 0)).isEmpty();
            assertThat(sut.partitionAll(-1, -1)).isEmpty();
        }

        @Test
        void returnsInfiniteSeqOfEmptyListsForZeroSizeN() {
            var sut = sutFrom(1, 2, 3);

            assertThat(sut.partitionAll(0).take(2)).containsExactly(
                    List.of(),
                    List.of());

            assertThat(sut.partitionAll(0, 1).take(2)).containsExactly(
                    List.of(),
                    List.of());

            assertThat(sut.partitionAll(0, 0).take(2)).containsExactly(
                    List.of(),
                    List.of());

            assertThat(sut.partitionAll(0, -1).take(2)).containsExactly(
                    List.of(),
                    List.of());
        }

        @Test
        void returnsSeqOfListsOf1ItemEachAtOffsetsStepApart() {
            var sut = sutFrom(0, 1, 2, 3, 4);

            assertThat(sut.partitionAll(1)).containsExactly(
                    List.of(0),
                    List.of(1),
                    List.of(2),
                    List.of(3),
                    List.of(4));

            assertThat(sut.partitionAll(1, 1)).containsExactly(
                    List.of(0),
                    List.of(1),
                    List.of(2),
                    List.of(3),
                    List.of(4));

            assertThat(sut.partitionAll(1, 2)).containsExactly(
                    List.of(0),
                    List.of(2),
                    List.of(4));
        }

        @Test
        void returnsSeqOfListsOfNItemsEachAtOffsetsStepApart() {
            var sut = sutFrom(0, 1, 2, 3, 4, 5, 6, 7, 8);

            assertThat(sut.partitionAll(3)).containsExactly(
                    List.of(0, 1, 2),
                    List.of(3, 4, 5),
                    List.of(6, 7, 8));

            assertThat(sut.partitionAll(3, 3)).containsExactly(
                    List.of(0, 1, 2),
                    List.of(3, 4, 5),
                    List.of(6, 7, 8));

            assertThat(sut.partitionAll(2, 3)).containsExactly(
                    List.of(0, 1),
                    List.of(3, 4),
                    List.of(6, 7));
        }

        @Test
        void returnsSeqOfOneEmptyListForStepGreaterThanOrEqualToSizeN() {
            var sut = sutFrom(1, 2, 3);

            assertThat(sut.partitionAll(0, 3)).containsExactly(
                    List.of());

            assertThat(sut.partitionAll(0, 4)).containsExactly(
                    List.of());
        }

        @Test
        void returnsASlidingWindowIfStepIsLowerThanSizeN() {
            var sut = sutFrom(0, 1, 2, 3, 4, 5, 6);

            assertThat(sut.partitionAll(3, 2).take(3)).containsExactly(
                    List.of(0, 1, 2),
                    List.of(2, 3, 4),
                    List.of(4, 5, 6));
        }

        @Test
        void returnsAnIncompleteLastPartition() {
            var sut = sutFrom(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13);

            assertThat(sut.partitionAll(4, 4)).containsExactly(
                    List.of(0, 1, 2, 3),
                    List.of(4, 5, 6, 7),
                    List.of(8, 9, 10, 11),
                    List.of(12, 13));

            assertThat(sut.partitionAll(3, 4)).containsExactly(
                    List.of(0, 1, 2),
                    List.of(4, 5, 6),
                    List.of(8, 9, 10),
                    List.of(12, 13));
        }
    }

    @Nested
    class Reductions {

        @Test
        void returnsASeqWithTheIntermediateValuesOfTheReduction() {
            var sut = sutFrom("1", "2", "3");

            assertThat(sut.reductions((a, b) -> a + b)).containsExactly("1", "12", "123");
        }

        @Test
        void returnsASeqWithTheIntermediateValuesOfTheReductionStartingWithInit() {
            var sut = sutFrom(1, 2, 3);

            assertThat(sut.reductions("0", (a, b) -> a + b.toString())).containsExactly("0", "01", "012", "0123");
        }
    }

    @Nested
    @DisplayName("cons")
    class ConsMethod {

        @Test
        void returnsNewSeqWithItemPrepended() {
            var sut = sutFrom(0, 1, 2);

            var actual = sut.cons(-1);

            assertThat(actual.first()).isEqualTo(-1);
            assertThat(actual.rest().take(3)).containsExactly(0, 1, 2);
            assertThat(actual.take(4)).containsExactly(-1, 0, 1, 2);
        }

        @Test
        void acceptsNullAsItem() {
            var sut = sutFrom(0, 1, 2);

            var actual = sut.cons(null);

            assertThat(actual.first()).isNull();
            assertThat(actual.rest().take(3)).containsExactly(0, 1, 2);
            assertThat(actual.take(4)).containsExactly(null, 0, 1, 2);
        }
    }

    @Nested
    class Reduce {

        @Test
        void returnsOptionalResultWhenValIsNotSupplied() {
            var sut = sutFrom(0, 1, 2, 3);

            assertThat(sut.reduce((a, b) -> a + b)).hasValue(6);
        }

        @Test
        void returnsResultWhenValIsSupplied() {
            var sut = sutFrom(1, 2, 3);

            assertThat(sut.reduce(0, (a, b) -> a + b)).isEqualTo(6);
        }

        @Test
        void returnsResultOfDifferentTypeThanSeq() {
            var sut = sutFrom("a", "bb", "ccc", "dddd");

            assertThat(sut.reduce(0, (acc, x) -> acc + x.length())).isEqualTo(10);
        }
    }

    @Test
    void runCallsProcForEveryItemPresent() {
        var proc = Mockito.<Consumer<Integer>>mock();

        var sut = sutFrom(0, 1, 2, 3, 4);

        sut.run(proc);

        verify(proc).accept(0);
        verify(proc).accept(1);
        verify(proc).accept(2);
        verify(proc).accept(3);
        verify(proc).accept(4);
        verifyNoMoreInteractions(proc);
    }

    @Nested
    class Distinct {

        @Test
        void returnsSeqWithSingleItem() {
            assertThat(sutFrom(1).distinct()).containsExactly(1);
        }

        @Test
        void returnsSeqThatAlreadyIsDistinct() {
            var sut = sutFrom(0, 1, 2, 3);

            assertThat(sut.distinct()).containsExactly(0, 1, 2, 3);
        }

        @Test
        void returnsSeqWithSingleItemForSeqWithIdenticalItems() {
            var sut = sutFrom("a", "a", "a", "a");

            assertThat(sut.distinct()).containsExactly("a");
        }

        @Test
        void returnsDistinctItemsInSameOrderAsEncounteredFirst() {
            var sut = sutFrom("a", "c", "a", "b", "b", "d", "f", "e", "g", "e");

            assertThat(sut.distinct()).containsExactly("a", "c", "b", "d", "f", "e", "g");
        }
    }

    @Nested
    class Sorted {

        @Test
        void returnsSeqWithSingleItem() {
            assertThat(sutFrom(1).sorted()).containsExactly(1);
        }

        @Test
        void returnsSeqWithAllItemsSortedUsingDefaultComparator() {
            var sut = sutFrom(10, 9, 7, 8);

            assertThat(sut.sorted()).containsExactly(7, 8, 9, 10);
        }

        @Test
        void returnsSeqWithAllItemsSortedUsingSuppliedComparator() {
            var sut = sutFrom(0, 1, 2, 3);

            assertThat(sut.sorted(Comparator.reverseOrder())).containsExactly(3, 2, 1, 0);
        }
    }

    @Test
    void reverseReturnsReversedSeq() {
        var sut = sutFrom(10, 9, 7, 8);

        assertThat(sut.reverse()).containsExactly(8, 7, 9, 10);
    }

    @Nested
    class Some {

        @Test
        void returnsFalseIfNoneOfTheItemsMatchPred() {
            var sut = sutFrom(0, 1, 2, 3);

            assertThat(sut.take(10).some(x -> x < 0)).isFalse();
        }

        @Test
        void returnsTrueIfAllItemsMatchPred() {
            var sut = sutFrom(0, 1, 2, 3);

            assertThat(sut.some(x -> x >= 0)).isTrue();
        }

        @Test
        void returnsTrueIfFirstItemMatchesPred() {
            var sut = sutFrom(0, 1, 2, 3);

            assertThat(sut.some(x -> x == 0)).isTrue();
        }

        @Test
        void returnsTrueIfSomeItemMatchesPred() {
            var sut = sutFrom(0, 1, 2, 3);

            assertThat(sut.some(x -> x == 2)).isTrue();
        }

        @Test
        void returnsTrueIfLastItemMatchesPred() {
            var sut = sutFrom(0, 1, 2, 3);

            assertThat(sut.some(x -> x == 3)).isTrue();
        }
    }

    @Nested
    class Every {

        @Test
        void returnsTrueIfAllItemsInSeqMatchPred() {
            var sut = sutFrom(1, 2, 3, 4);

            assertThat(sut.every(x -> x > 0)).isTrue();
        }

        @Test
        void returnsFalseIfFirstItemDoesNotMatchPred() {
            var sut = sutFrom(0, 1, 2, 3);

            assertThat(sut.every(x -> x > 0)).isFalse();
        }

        @Test
        void returnsFalseIfAnyItemDoesNotMatchPred() {
            var sut = sutFrom(1, 2, 3, 4);

            assertThat(sut.every(x -> x < 3)).isFalse();
        }

        @Test
        void returnsFalseIfLastItemDoesNotMatchPred() {
            var sut = sutFrom(1, 2, 3, 4);

            assertThat(sut.every(x -> x < 4)).isFalse();
        }
    }

    @Nested
    class NotAny {

        @Test
        void returnsFalseIfFirstItemMatchesPred() {
            var sut = sutFrom(0, 1, 2);

            assertThat(sut.notAny(x -> x == 0)).isFalse();
        }

        @Test
        void returnsFalseIfAnyItemMatchesPred() {
            var sut = sutFrom(0, 1, 2);

            assertThat(sut.notAny(x -> x == 1)).isFalse();
        }

        @Test
        void returnsFalseIfAllItemsMatchPred() {
            var sut = sutFrom(1, 2, 3, 4);

            assertThat(sut.notAny(x -> x > 0)).isFalse();
        }

        @Test
        void returnsTrueIfNoItemMatchesPred() {
            var sut = sutFrom(0, 1, 2);

            assertThat(sut.take(100).notAny(x -> x < 0)).isTrue();
        }
    }

    @Nested
    class Max {

        @Test
        void returnsSingleItem() {
            assertThat(sutFrom(1).max(Comparator.naturalOrder())).hasValue(1);
        }

        @Test
        void returnsHighestNumber() {
            var sut = sutFrom(1, 2, 3, 4, 5, 6, 7, 8);

            assertThat(sut.max(Comparator.naturalOrder())).hasValue(8);
        }

        @Test
        void returnsLongestString() {
            var sut = sutFrom("x", "xx", "xxx", "xxxxx");

            assertThat(sut.max(Comparator.comparingInt(x -> x.length()))).hasValue("xxxxx");
        }

        @Test
        void returnsTheLastOccurrenceOfLongestStringIfMoreThanOneItemFound() {
            var sut = sutFrom("x", "xx", "aaa", "x", "bbb");

            assertThat(sut.max(Comparator.comparingInt(x -> x.length()))).hasValue("bbb");
        }
    }

    @Nested
    class Min {

        @Test
        void returnsSingleItem() {
            assertThat(sutFrom(1).min(Comparator.naturalOrder())).hasValue(1);
        }

        @Test
        void returnsLowestNumber() {
            var sut = sutFrom(-5, -4, -3, -2, -1, 0, 1, 2);

            assertThat(sut.min(Comparator.naturalOrder())).hasValue(-5);
        }

        @Test
        void returnsShortestString() {
            var sut = sutFrom("xxxxxx", "xxxxx", "xxxx", "x", "xx", "xxx");

            assertThat(sut.min(Comparator.comparingInt(x -> x.length()))).hasValue("x");
        }

        @Test
        void returnsTheLastOccurrenceOfShortestStringIfMoreThanOneItemFound() {
            var sut = sutFrom("a", "xx", "aaa", "x", "bbb", "b");

            assertThat(sut.min(Comparator.comparingInt(x -> x.length()))).hasValue("b");
        }
    }

    @Nested
    class MaxKey {

        @Test
        void returnsSingleItem() {
            assertThat(sutFrom(1).maxKey(x -> Math.abs(x))).hasValue(1);
        }

        @Test
        void returnsHighestNumber() {
            var sut = sutFrom(1, 2, 3, 4, 5, 6);

            assertThat(sut.maxKey(x -> Math.abs(x))).hasValue(6);
        }

        @Test
        void returnsLongestString() {
            var sut = sutFrom("x", "xx", "xxx", "xxxx", "xxxxx", "xxxxxx");

            assertThat(sut.maxKey(x -> x.length())).hasValue("xxxxxx");
        }

        @Test
        void returnsTheLastOccurrenceOfLongestStringIfMoreThanOneItemFound() {
            var sut = sutFrom("x", "xx", "aaa", "x", "bbb");

            assertThat(sut.maxKey(x -> x.length())).hasValue("bbb");
        }
    }

    @Nested
    class MinKey {

        @Test
        void returnsSingleItem() {
            assertThat(sutFrom(1).minKey(x -> Math.abs(x))).hasValue(1);
        }

        @Test
        void returnsLowestNumber() {
            var sut = sutFrom(-8, -7, -6, -5, -4, -3);

            assertThat(sut.minKey(x -> x)).hasValue(-8);
        }

        @Test
        void returnsShortestString() {
            var sut = sutFrom("xxxxxx", "xxxxx", "xxxx", "x", "xx", "xxx");

            assertThat(sut.minKey(x -> x.length())).hasValue("x");
        }

        @Test
        void returnsTheLastOccurrenceOfShortestStringIfMoreThanOneItemFound() {
            var sut = sutFrom("x", "xx", "aaa", "x", "bbb");

            assertThat(sut.minKey(x -> x.length())).hasValue("x");
        }
    }

    @Nested
    class Nth {

        @Test
        void returnsValueAtIndex() {
            var sut = sutFrom("", "0", "01", "012");

            assertThat(sut.nth(0)).isEqualTo("");
            assertThat(sut.nth(1)).isEqualTo("0");
            assertThat(sut.nth(2)).isEqualTo("01");
            assertThat(sut.nth(3)).isEqualTo("012");
        }

        @Test
        void returnsDefaultValue() {
            var sut = sutFrom("x");

            assertThat(sut.nth(0, "y")).isEqualTo("x");
            assertThat(sut.nth(1, "y")).isEqualTo("y");
            assertThat(sut.nth(2, "y")).isEqualTo("y");
            assertThat(sut.nth(3, "y")).isEqualTo("y");
        }

        @Test
        void throwsForNegativeIndex() {
            var sut = sutFrom(1);

            assertThatExceptionOfType(IndexOutOfBoundsException.class)
                    .isThrownBy(() -> sut.nth(-1))
                    .withMessage("Index out of range: -1");
        }

        @Test
        void throwsIfIndexNotPresent() {
            var sut = sutFrom(1);

            assertThatExceptionOfType(IndexOutOfBoundsException.class)
                    .isThrownBy(() -> sut.nth(1))
                    .withMessage("Index out of range: 1");
        }
    }

    @Test
    void strReturnsConcatenatedStringRepresentationsOfAllItems() {
        assertThat(sutFrom("", "0", "1", "2", "3").str())
                .isEqualTo("0123");
        assertThat(sutFrom(new Object(), new Object(), new Object()).str())
                .matches("java\\.lang\\.Object@.+java\\.lang\\.Object@.+java\\.lang\\.Object@.+");
    }

    @Nested
    class Find {

        @Test
        void returnsOptionalOfValueAtIndex() {
            var sut = sutFrom("0", "01", "012", "0123");

            assertThat(sut.find(0)).hasValue("0");
            assertThat(sut.find(1)).hasValue("01");
            assertThat(sut.find(2)).hasValue("012");
            assertThat(sut.find(3)).hasValue("0123");
        }

        @Test
        void returnsEmptyOptionalForNegativeIndex() {
            assertThat(sutFrom(1).find(-1)).isEmpty();
        }

        @Test
        void returnsEmptyOptionalIfIndexNotPresent() {
            assertThat(sutFrom(1).find(1)).isEmpty();
        }
    }

    @Nested
    class FindFirst {

        @Test
        void returnsOptionalOfHead() {
            var sut = sutFrom(0, 1, 2, 3, 4);

            assertThat(sut.findFirst()).hasValue(0);
        }

        @Test
        void returnsEmptyOptionalWhenNoItemsMatchPred() {
            var sut = sutFrom(0, 1, 2, 3, 4);

            assertThat(sut.findFirst(x -> x < 0)).isEmpty();
        }

        @Test
        void returnsOptionalOfFirstMatchingItem() {
            var sut = sutFrom(0, 1, 2, 3, 4);

            assertThat(sut.findFirst(x -> x > 3)).hasValue(4);
        }
    }

    @Test
    void forceReturnsCopyOfThisSeq() {
        var sut = sutFrom(1, 2, 3);
        assertThat(sut.isRealized()).isTrue();

        var forced = sut.realize();
        assertThat(sut.isRealized()).isTrue();
        assertThat(forced).isSameAs(sut);
        assertThat(forced.isRealized()).isTrue();
    }

    @Nested
    class ToList {

        @Test
        void returnsListContainingTheItemsInThisSeq() {
            var sut = sutFrom(0, 1, 2, 3);

            assertThat(sut.toList())
                    .isInstanceOf(List.class)
                    .containsExactly(0, 1, 2, 3);
        }
    }

    @Nested
    class ToSet {

        @Test
        void returnsSetContainingTheUniqueItemsInThisSeq() {
            var sut = sutFrom(0, 1, 2, 3, 2, 1, 0, 4);

            assertThat(sut.toSet())
                    .isInstanceOf(Set.class)
                    .containsExactlyInAnyOrder(0, 1, 2, 3, 4);
        }
    }

    @Nested
    class ToMap {

        @Test
        void returnsMapForSeqOfEntries() {
            var sut = sutFrom("x", "xx", "xxx").map(x -> java.util.Map.entry(x.length(), x));

            var actual = sut.toMap();

            assertThat(actual)
                    .containsEntry(1, "x")
                    .containsEntry(2, "xx")
                    .containsEntry(3, "xxx");
        }

        @Test
        void returnsMapForSeqOfEntriesWithLastValueWinningOnCollision() {
            var sut = sutFrom("a", "aa", "b", "bb").map(x -> java.util.Map.entry(x.length(), x));

            var actual = sut.toMap();

            assertThat(actual).hasSize(2)
                    .containsEntry(1, "b")
                    .containsEntry(2, "bb");
        }

        @Test
        void throwsIfSeqIsNotOfTypeEntry() {
            var sut = sutFrom("x", "xx", "xxx");

            assertThatExceptionOfType(UnsupportedOperationException.class)
                    .isThrownBy(() -> sut.toMap())
                    .withMessage("ISeq is not of type Map.Entry. Provide key- and value-mappers");
        }

        @Test
        void returnsMapBasedOnKeyAndValueMapper() {
            var sut = sutFrom("x", "xx", "xxx");

            var actual = sut.toMap(k -> k.length(), v -> v);

            assertThat(actual)
                    .containsEntry(1, "x")
                    .containsEntry(2, "xx")
                    .containsEntry(3, "xxx");
        }

        @Test
        void throwsOnCollision() {
            var sut = sutFrom("a", "b");

            assertThatExceptionOfType(IllegalArgumentException.class)
                    .isThrownBy(() -> sut.toMap(k -> k.length(), v -> v))
                    .withMessage("duplicate key: 1");
        }

        @Test
        void returnsMapBasedOnKeyAndValueMapperWithApplyingMergerOnCollision() {
            var sut = sutFrom("a", "b", "aa", "bb");

            var actual = sut.toMap(k -> k.length(), v -> v, (a, b) -> b);

            assertThat(actual).hasSize(2)
                    .containsEntry(1, "b")
                    .containsEntry(2, "bb");
        }
    }

    @Nested
    class Iterator {

        @Test
        void returnsIterator() {
            var sut = sutFrom(0, 1);

            var actual = sut.iterator();

            assertThat(actual.hasNext()).isTrue();
            assertThat(actual.next()).isEqualTo(0);
            assertThat(actual.hasNext()).isTrue();
            assertThat(actual.next()).isEqualTo(1);
            assertThat(actual.hasNext()).isFalse();
        }
    }

    @Test
    void streamReturnsStream() {
        var sut = sutFrom(0, 1, 2);

        assertThat(sut.stream()).containsExactly(0, 1, 2);
    }

    @Test
    void parallelStreamReturnsStream() {
        var sut = sutFrom(0, 1, 2);

        assertThat(sut.parallelStream()).containsExactly(0, 1, 2);
    }

    @Test
    void forEachCallsConsumerForEveryItemPresent() {
        var consumer = Mockito.<Consumer<Integer>>mock();

        var sut = sutFrom(0, 1, 2, 3, 4);

        sut.forEach(consumer);

        verify(consumer).accept(0);
        verify(consumer).accept(1);
        verify(consumer).accept(2);
        verify(consumer).accept(3);
        verify(consumer).accept(4);
        verifyNoMoreInteractions(consumer);
    }

    @Test
    void sizeReturnsSizeOfSeq() {
        var sut = sutFrom(0, 1, 2, 3);

        assertThat(sut.size()).isEqualTo(4);
    }

    @Test
    void isEmptyReturnsFalse() {
        var sut = sutFrom(1);

        assertThat(sut.isEmpty()).isFalse();
    }

    @Nested
    class Get {

        @Test
        void returnsValueAtIndex() {
            var sut = sutFrom("", "0", "01", "012");

            assertThat(sut.get(0)).isEqualTo("");
            assertThat(sut.get(1)).isEqualTo("0");
            assertThat(sut.get(2)).isEqualTo("01");
            assertThat(sut.get(3)).isEqualTo("012");
        }

        @Test
        void throwsForNegativeIndex() {
            assertThatExceptionOfType(IndexOutOfBoundsException.class)
                    .isThrownBy(() -> sutFrom(1).get(-1))
                    .withMessage("Index out of range: -1");
        }

        @Test
        void throwsIfIndexNotPresent() {
            assertThatExceptionOfType(IndexOutOfBoundsException.class)
                    .isThrownBy(() -> sutFrom(1).get(1))
                    .withMessage("Index out of range: 1");
        }
    }

    @Test
    void toStringReturnsAllItems() {
        var sut = sutFrom(0, 1, 2, 3);

        assertThat(sut).hasToString("[0, 1, 2, 3]");
    }
}