package com.github.nylle.javaseq;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class LazySeqTest {

    @Test
    void firstReturnsFirstItem() {
        var sut = ISeq.iterate(0, x -> x + 1);

        assertThat(sut.first()).isEqualTo(0);
    }

    @Test
    void restReturnsSeqWithItemsExceptFirst() {
        var sut = ISeq.iterate(0, x -> x + 1);

        var rest = sut.rest();

        assertThat(rest.first()).isEqualTo(1);
        assertThat(rest.rest().first()).isEqualTo(2);
        assertThat(rest.rest().rest().first()).isEqualTo(3);
    }

    @Test
    void sizeReturnsSizeOfFiniteSeqOrRunsForever() {
        var sut = ISeq.lazySeq(3, () -> ISeq.lazySeq(-2, () -> ISeq.lazySeq(8, () -> ISeq.of(1))));

        assertThat(sut.size()).isEqualTo(4);
    }

    @Test
    void isEmptyReturnsFalse() {
        var sut = ISeq.iterate("", x -> x + x.length());

        assertThat(sut.isEmpty()).isFalse();
    }

    @Nested
    class Get {

        @Test
        void returnsValueAtIndex() {
            var sut = ISeq.iterate("", x -> x + x.length());

            assertThat(sut.get(0)).isEqualTo("");
            assertThat(sut.get(1)).isEqualTo("0");
            assertThat(sut.get(2)).isEqualTo("01");
            assertThat(sut.get(3)).isEqualTo("012");
        }

        @Test
        void throwsForNegativeIndex() {
            assertThatExceptionOfType(IndexOutOfBoundsException.class)
                    .isThrownBy(() -> ISeq.range(1).get(-1))
                    .withMessage("Index out of range: -1");
        }

        @Test
        void throwsIfIndexNotPresent() {
            assertThatExceptionOfType(IndexOutOfBoundsException.class)
                    .isThrownBy(() -> ISeq.range(1).get(1))
                    .withMessage("Index out of range: 1");
        }
    }

    @Nested
    class Nth {

        @Test
        void returnsValueAtIndex() {
            var sut = ISeq.iterate("", x -> x + x.length());

            assertThat(sut.nth(0)).isEqualTo("");
            assertThat(sut.nth(1)).isEqualTo("0");
            assertThat(sut.nth(2)).isEqualTo("01");
            assertThat(sut.nth(3)).isEqualTo("012");
        }

        @Test
        void returnsDefaultValue() {
            var sut = ISeq.iterate("", x -> x + x.length()).take(1);

            assertThat(sut.nth(0, "x")).isEqualTo("");
            assertThat(sut.nth(1, "x")).isEqualTo("x");
            assertThat(sut.nth(2, "x")).isEqualTo("x");
            assertThat(sut.nth(3, "x")).isEqualTo("x");
        }

        @Test
        void throwsForNegativeIndex() {
            var sut = ISeq.iterate("", x -> x + x.length()).take(1);

            assertThatExceptionOfType(IndexOutOfBoundsException.class)
                    .isThrownBy(() -> sut.nth(-1))
                    .withMessage("Index out of range: -1");
        }

        @Test
        void throwsIfIndexNotPresent() {
            var sut = ISeq.iterate("", x -> x + x.length()).take(1);

            assertThatExceptionOfType(IndexOutOfBoundsException.class)
                    .isThrownBy(() -> sut.nth(1))
                    .withMessage("Index out of range: 1");
        }
    }

    @Nested
    class Take {

        @Test
        void returnsNilWithNegativeItems() {
            var sut = ISeq.iterate(0, x -> x + 1);

            assertThat(sut.take(-1))
                    .isExactlyInstanceOf(Nil.class)
                    .isEmpty();
        }

        @Test
        void returnsNilWithZeroItems() {
            var sut = ISeq.iterate(0, x -> x + 1);

            assertThat(sut.take(0))
                    .isExactlyInstanceOf(Nil.class)
                    .isEmpty();
        }

        @Test
        void returnsConsWithMoreThanZeroItems() {
            var sut = ISeq.iterate(0, x -> x + 1);

            assertThat(sut.take(3))
                    .isExactlyInstanceOf(Cons.class)
                    .containsExactly(0, 1, 2);
        }
    }

    @Nested
    class Drop {

        @Test
        void returnsUnchangedSeqWithNegativeItemsToDrop() {
            assertThat(ISeq.range(1, 5).drop(-1)).containsExactly(1, 2, 3, 4);
        }

        @Test
        void returnsUnchangedSeqWithZeroItemsToDrop() {
            assertThat(ISeq.range(1, 5).drop(0)).containsExactly(1, 2, 3, 4);
        }

        @Test
        void returnsSeqOfAllButTheFirstNItems() {
            assertThat(ISeq.range(1, 5).drop(2)).containsExactly(3, 4);
        }

        @Test
        void returnsEmptySeqIfMoreItemsAreDroppedThanPresent() {
            assertThat(ISeq.range(1, 5).drop(5)).isEmpty();
        }

        @Test
        void isLazy() {
            var sut = ISeq.range();

            assertThat(sut.drop(2).take(2)).containsExactly(2, 3);
        }
    }

    @Nested
    class Filter {

        @Test
        void returnsNilWhenNoItemsMatch() {
            var sut = ISeq.iterate(0, x -> x + 1).take(10);

            assertThat(sut.filter(x -> x < 0)).isEmpty();
        }

        @Test
        void returnsMatchingItems() {
            var sut = ISeq.iterate(0, x -> x + 1);

            assertThat(sut.filter(x -> x > 100).take(3)).containsExactly(101, 102, 103);
        }
    }

    @Nested
    class Map {

        @Test
        void returnsSingleMapResult() {
            var sut = ISeq.iterate("x", x -> x + "x");

            assertThat(sut.map(x -> x.length()).take(1)).isEqualTo(ISeq.of(1));
        }

        @Test
        void returnsAllMapResults() {
            var sut = ISeq.iterate("x", x -> x + "x");

            assertThat(sut.map(x -> x.length()).take(3)).isEqualTo(ISeq.of(1, 2, 3));
        }

        @Test
        void returnsInfiniteMapResults() {
            var sut = ISeq.iterate(0, x -> x + 1);

            assertThat(sut.map(x -> x * 100).take(3)).containsExactly(0, 100, 200);
        }

        @Nested
        class WithOtherSeq {

            @Test
            void returnsEmptySeqWhenProvidingEmptyOther() {
                assertThat(ISeq.range(1, 4).map(ISeq.<Integer>of(), (a, b) -> a + b)).isEmpty();
            }

            @Test
            void returnsANewSeqWithTheItemsOfBothInitialSeqsAreCombinedUsingF() {
                var sut = ISeq.range(1, 4);

                assertThat(sut.map(ISeq.of("a", "b", "c"), (a, b) -> a + b)).containsExactly("1a", "2b", "3c");
            }

            @Test
            void ignoresRemainingItemsIfOneOfTheSeqsIsExhausted() {
                var sut = ISeq.range(1, 4);

                assertThat(sut.map(ISeq.of("a", "b"), (a, b) -> a + b)).containsExactly("1a", "2b");
                assertThat(sut.map(ISeq.of("a", "b", "c", "d"), (a, b) -> a + b)).containsExactly("1a", "2b", "3c");
            }

            @Test
            void isLazy() {
                var sut = ISeq.iterate(0, x -> x + 1);
                var other = ISeq.iterate(0, x -> x + 1);

                assertThat(sut.map(other, (a, b) -> a + b).take(4)).containsExactly(0, 2, 4, 6);
            }
        }
    }

    @Nested
    class Mapcat {

        @Test
        void returnsFlattenedSeq() {
            var sut = ISeq.of(ISeq.range(3), ISeq.range(3, 6));

            assertThat(sut.mapcat(x -> x)).containsExactly(0, 1, 2, 3, 4, 5);
        }

        @Test
        void isLazy() {
            var sut = ISeq.iterate(0, x -> x + 1);

            assertThat(sut.mapcat(x -> List.of(x, x)).take(6)).containsExactly(0, 0, 1, 1, 2, 2);
        }

        @Test
        void ignoresEmptyResults() {
            var sut = ISeq.iterate(0, x -> x + 1);

            assertThat(sut.mapcat(x -> x == 0 ? List.of() : List.of(x, x)).take(6)).containsExactly(1, 1, 2, 2, 3, 3);
        }

        @Nested
        class WithOtherColl {

            @Test
            void returnsEmptySeqWhenProvidingEmptyOther() {
                assertThat(ISeq.range(1, 4).mapcat(List.<Integer>of(), (a, b) -> List.of(a + b, a + b))).isEmpty();
            }

            @Test
            void returnsANewSeqWithTheItemsOfBothInitialSeqsAreCombinedUsingF() {
                var sut = ISeq.range(1, 4);

                assertThat(sut.mapcat(List.of("a", "b", "c"), (a, b) -> List.of(a + b, a + b)))
                        .containsExactly("1a", "1a", "2b", "2b", "3c", "3c");
            }

            @Test
            void ignoresRemainingItemsIfOneOfTheSeqsIsExhausted() {
                var sut = ISeq.range(1, 4);

                assertThat(sut.mapcat(List.of("a", "b"), (a, b) -> List.of(a + b, a + b)))
                        .containsExactly("1a", "1a", "2b", "2b");
                assertThat(sut.mapcat(List.of("a", "b", "c", "d"), (a, b) -> List.of(a + b, a + b)))
                        .containsExactly("1a", "1a", "2b", "2b", "3c", "3c");
            }

            @Test
            void isLazy() {
                var sut = ISeq.iterate(0, x -> x + 1);
                var other = ISeq.iterate(0, x -> x + 1);

                assertThat(sut.mapcat(other, (a, b) -> List.of(a + b, a + b)).take(8))
                        .containsExactly(0, 0, 2, 2, 4, 4, 6, 6);
            }
        }
    }

    @Nested
    class TakeWhile {

        @Test
        void returnsEmptySeqWhenFirstItemDoesNotMatch() {
            assertThat(ISeq.iterate(0, x -> x + 1).takeWhile(x -> x > 0)).isEmpty();
        }

        @Test
        void returnsSeqWithSingleMatchingItem() {
            assertThat(ISeq.iterate(0, x -> x + 1).takeWhile(x -> x < 1)).containsExactly(0);
        }

        @Test
        void returnsSeqWithMatchingItems() {
            assertThat(ISeq.iterate(0, x -> x + 1).takeWhile(x -> x < 3)).containsExactly(0, 1, 2);
        }

        @Test
        void returnsSeqWithAllMatchingItems() {
            assertThat(ISeq.iterate(0, x -> x + 1).takeWhile(x -> true).take(4)).containsExactly(0, 1, 2, 3);
        }
    }

    @Nested
    class DropWhile {

        @Test
        void returnsEmptySeqWhenAllItemsMatch() {
            assertThat(ISeq.range(1, 5).dropWhile(x -> x > 0)).isEmpty();
        }

        @Test
        void returnsSeqWithItemsThatDoNotMatch() {
            assertThat(ISeq.iterate(0, x -> x + 1).dropWhile(x -> x < 2).take(4)).containsExactly(2, 3, 4, 5);
        }

        @Test
        void returnsEntireSeqWhenFirstItemDoesNotMatch() {
            assertThat(ISeq.iterate(0, x -> x + 1).dropWhile(x -> x > 2).take(4)).containsExactly(0, 1, 2, 3);
        }
    }

    @Nested
    class Partition {

        @Test
        void returnsEmptySeqForNegativeSizeN() {
            var sut = ISeq.iterate(0, x -> x + 1);

            assertThat(sut.partition(-1)).isEmpty();
            assertThat(sut.partition(-1, 10)).isEmpty();
            assertThat(sut.partition(-1, 3)).isEmpty();
            assertThat(sut.partition(-1, 1)).isEmpty();
            assertThat(sut.partition(-1, 0)).isEmpty();
            assertThat(sut.partition(-1, -1)).isEmpty();
        }

        @Test
        void returnsInfiniteSeqOfEmptyListsForZeroSizeN() {
            var sut = ISeq.iterate(0, x -> x + 1);

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
        void returnsSeqOfListsOfOneItemEachAtOffsetsStepApart() {
            var sut = ISeq.iterate(0, x -> x + 1);

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
            var sut = ISeq.iterate(0, x -> x + 1);

            assertThat(sut.partition(3).take(3)).containsExactly(
                    List.of(0, 1, 2),
                    List.of(3, 4, 5),
                    List.of(6, 7, 8));

            assertThat(sut.partition(3, 3).take(3)).containsExactly(
                    List.of(0, 1, 2),
                    List.of(3, 4, 5),
                    List.of(6, 7, 8));

            assertThat(sut.partition(4, 6).take(3)).containsExactly(
                    List.of(0, 1, 2, 3),
                    List.of(6, 7, 8, 9),
                    List.of(12, 13, 14, 15));
        }

        @Test
        void dropsItemsThatDoNotMakeACompleteLastPartition() {
            var sut = ISeq.iterate(0, x -> x + 1).take(14);

            assertThat(sut.partition(4)).containsExactly(
                    List.of(0, 1, 2, 3),
                    List.of(4, 5, 6, 7),
                    List.of(8, 9, 10, 11));

            assertThat(sut.partition(4, 4)).containsExactly(
                    List.of(0, 1, 2, 3),
                    List.of(4, 5, 6, 7),
                    List.of(8, 9, 10, 11));

            assertThat(sut.partition(3, 4)).containsExactly(
                    List.of(0, 1, 2),
                    List.of(4, 5, 6),
                    List.of(8, 9, 10));
        }

        @Test
        void returnsSeqOfOneEmptyListForStepGreaterThanOrEqualToSizeN() {
            var sut = ISeq.range(1, 4);

            assertThat(sut.partition(0, 3)).containsExactly(
                    List.of());

            assertThat(sut.partition(0, 4)).containsExactly(
                    List.of());
        }

        @Test
        void returnsASlidingWindowIfStepIsLowerThanSizeN() {
            var sut = ISeq.iterate(0, x -> x + 1);

            assertThat(sut.partition(3, 2).take(3)).containsExactly(
                    List.of(0, 1, 2),
                    List.of(2, 3, 4),
                    List.of(4, 5, 6));
        }

        @Nested
        class WhenPadIsProvidedAndNotNull {

            @Test
            void fillsIncompleteLastPartitionWithItemsFromPad() {
                var sut = ISeq.iterate(0, x -> x + 1).take(14);

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
                var sut = ISeq.iterate(0, x -> x + 1).take(14);

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
            var sut = ISeq.iterate(0, x -> x + 1);

            assertThat(sut.partitionAll(-1)).isEmpty();
            assertThat(sut.partitionAll(-1, 10)).isEmpty();
            assertThat(sut.partitionAll(-1, 3)).isEmpty();
            assertThat(sut.partitionAll(-1, 1)).isEmpty();
            assertThat(sut.partitionAll(-1, 0)).isEmpty();
            assertThat(sut.partitionAll(-1, -1)).isEmpty();
        }

        @Test
        void returnsInfiniteSeqOfEmptyListsForZeroSizeN() {
            var sut = ISeq.iterate(0, x -> x + 1);

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
            var sut = ISeq.iterate(0, x -> x + 1);

            assertThat(sut.partitionAll(1).take(3)).containsExactly(
                    List.of(0),
                    List.of(1),
                    List.of(2));

            assertThat(sut.partitionAll(1, 1).take(3)).containsExactly(
                    List.of(0),
                    List.of(1),
                    List.of(2));

            assertThat(sut.partitionAll(1, 2).take(3)).containsExactly(
                    List.of(0),
                    List.of(2),
                    List.of(4));
        }

        @Test
        void returnsSeqOfListsOfNItemsEachAtOffsetsStepApart() {
            var sut = ISeq.iterate(0, x -> x + 1);

            assertThat(sut.partitionAll(3).take(3)).containsExactly(
                    List.of(0, 1, 2),
                    List.of(3, 4, 5),
                    List.of(6, 7, 8));

            assertThat(sut.partitionAll(3, 3).take(3)).containsExactly(
                    List.of(0, 1, 2),
                    List.of(3, 4, 5),
                    List.of(6, 7, 8));

            assertThat(sut.partitionAll(4, 6).take(3)).containsExactly(
                    List.of(0, 1, 2, 3),
                    List.of(6, 7, 8, 9),
                    List.of(12, 13, 14, 15));
        }

        @Test
        void returnsSeqOfOneEmptyListForStepGreaterThanOrEqualToSizeN() {
            var sut = ISeq.range(1, 4);

            assertThat(sut.partitionAll(0, 3)).containsExactly(
                    List.of());

            assertThat(sut.partitionAll(0, 4)).containsExactly(
                    List.of());
        }

        @Test
        void returnsASlidingWindowIfStepIsLowerThanSizeN() {
            var sut = ISeq.iterate(0, x -> x + 1);

            assertThat(sut.partitionAll(3, 2).take(3)).containsExactly(
                    List.of(0, 1, 2),
                    List.of(2, 3, 4),
                    List.of(4, 5, 6));
        }

        @Test
        void returnsAnIncompleteLastPartition() {
            var sut = ISeq.iterate(0, x -> x + 1).take(14);

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
            var sut = ISeq.iterate(1, x -> x + 1);

            assertThat(sut.reductions((a, b) -> a + b).take(3)).containsExactly(1, 3, 6);
        }

        @Test
        void returnsASeqWithTheIntermediateValuesOfTheReductionStartingWithInit() {
            var sut = ISeq.iterate(1, x -> x + 1);

            assertThat(sut.reductions(0, (a, b) -> a + b).take(4)).containsExactly(0, 1, 3, 6);
        }
    }

    @Nested
    @DisplayName("cons")
    class ConsTest {

        @Test
        void returnsNewSeqWithItemPrepended() {
            var sut = ISeq.range();

            var actual = sut.cons(-1);

            assertThat(actual.first()).isEqualTo(-1);
            assertThat(actual.rest().take(3)).containsExactly(0, 1, 2);
            assertThat(actual.take(4)).containsExactly(-1, 0, 1, 2);
        }

        @Test
        void acceptsNullAsItem() {
            var sut = ISeq.range();

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
            var sut = ISeq.iterate(0, x -> x + 1);

            assertThat(sut.take(4).reduce((a, b) -> a + b)).hasValue(6);
        }

        @Test
        void returnsResultWhenValIsSupplied() {
            var sut = ISeq.iterate(1, x -> x + 1);

            assertThat(sut.take(3).reduce(0, (a, b) -> a + b)).isEqualTo(6);
        }

        @Test
        void returnsResultOfDifferentTypeThanSeq() {
            var sut = ISeq.iterate(0, x -> x + 1);

            assertThat(sut.take(4).reduce("", (acc, x) -> acc + x.toString())).isEqualTo("0123");
        }
    }

    @Nested
    class Distinct {

        @Test
        void returnsSeqWithSingleItem() {
            assertThat(ISeq.range(1).distinct()).containsExactly(0);
        }

        @Test
        void returnsSeqThatAlreadyIsDistinct() {
            var sut = ISeq.iterate(0, x -> x + 1);

            assertThat(sut.distinct().take(4)).containsExactly(0, 1, 2, 3);
        }

        @Test
        void returnsSeqWithSingleItemForSeqWithIdenticalItems() {
            var sut = ISeq.iterate("a", x -> x);

            assertThat(sut.take(4).distinct()).containsExactly("a");
        }

        @Test
        void returnsDistinctItemsInSameOrderAsEncounteredFirst() {
            var sut = ISeq.sequence(List.of("a", "c", "a", "b", "b", "d", "f", "e", "g", "e"));

            assertThat(sut.distinct()).containsExactly("a", "c", "b", "d", "f", "e", "g");
        }
    }

    @Nested
    class Sorted {

        @Test
        void returnsSeqWithSingleItem() {
            assertThat(ISeq.range(1).sorted()).isEqualTo(ISeq.of(0));
        }

        @Test
        void returnsSeqWithAllItemsSortedUsingDefaultComparator() {
            var sut = ISeq.iterate(10, x -> x - 1);

            assertThat(sut.take(4).sorted()).containsExactly(7, 8, 9, 10);
        }

        @Test
        void returnsSeqWithAllItemsSortedUsingSuppliedComparator() {
            var sut = ISeq.iterate(0, x -> x + 1);

            assertThat(sut.take(4).sorted(Comparator.reverseOrder())).containsExactly(3, 2, 1, 0);
        }
    }

    @Nested
    class Some {

        @Test
        void returnsFalseIfNoneOfTheItemsMatchPred() {
            var sut = ISeq.iterate(0, x -> x + 1);

            assertThat(sut.take(10).some(x -> x < 0)).isFalse();
        }

        @Test
        void returnsTrueIfAllItemsMatchPred() {
            var sut = ISeq.iterate(0, x -> x + 1);

            assertThat(sut.take(10).some(x -> x >= 0)).isTrue();
        }

        @Test
        void returnsTrueIfFirstItemInInfiniteSeqMatchesPred() {
            var sut = ISeq.iterate(0, x -> x + 1);

            assertThat(sut.some(x -> x == 0)).isTrue();
        }

        @Test
        void returnsTrueIfSomeItemInInfiniteSeqMatchesPred() {
            var sut = ISeq.iterate(0, x -> x + 1);

            assertThat(sut.some(x -> x == 5)).isTrue();
        }

        @Test
        void returnsTrueIfLastItemMatchesPred() {
            var sut = ISeq.iterate(0, x -> x + 1);

            assertThat(sut.take(10).some(x -> x == 9)).isTrue();
        }
    }

    @Nested
    class Every {

        @Test
        void returnsTrueIfAllItemsInSeqMatchPred() {
            var sut = ISeq.iterate(1, x -> x + 1);

            assertThat(sut.take(100).every(x -> x > 0)).isTrue();
        }

        @Test
        void returnsFalseIfFirstItemInInfiniteSeqDoesNotMatchPred() {
            var sut = ISeq.iterate(0, x -> x + 1);

            assertThat(sut.every(x -> x > 0)).isFalse();
        }

        @Test
        void returnsFalseIfAnyItemInInfiniteSeqDoesNotMatchPred() {
            var sut = ISeq.iterate(0, x -> x + 1);

            assertThat(sut.every(x -> x < 100)).isFalse();
        }

        @Test
        void returnsFalseIfLastItemInInfiniteSeqDoesNotMatchPred() {
            var sut = ISeq.iterate(1, x -> x + 1);

            assertThat(sut.take(100).every(x -> x < 100)).isFalse();
        }
    }

    @Nested
    class NotAny {

        @Test
        void returnsFalseIfFirstItemMatchesPred() {
            var sut = ISeq.iterate(0, x -> x + 1);

            assertThat(sut.notAny(x -> x == 0)).isFalse();
        }

        @Test
        void returnsFalseIfAnyItemMatchesPred() {
            var sut = ISeq.iterate(0, x -> x + 1);

            assertThat(sut.notAny(x -> x == 100)).isFalse();
        }

        @Test
        void returnsFalseIfAllItemsMatchPred() {
            var sut = ISeq.iterate(1, x -> x + 1);

            assertThat(sut.notAny(x -> x > 0)).isFalse();
        }

        @Test
        void returnsTrueIfNoItemMatchesPred() {
            var sut = ISeq.iterate(0, x -> x + 1);

            assertThat(sut.take(100).notAny(x -> x < 0)).isTrue();
        }
    }

    @Nested
    class IsRealized {

        @Test
        void returnsFalseForUnrealisedLazySeq() {
            var sut = ISeq.iterate(0, x -> x + 1);

            assertThat(sut.isRealized()).isFalse();
        }

        @Test
        void returnsFalseForPartiallyRealisedLazySeq() {
            var sut = ISeq.iterate(0, x -> x + 1);

            sut.get(2);

            assertThat(sut.isRealized()).isTrue();
        }

        @Test
        void returnsTrueForFullyRealisedSeq() {
            var sut = ISeq.range(1, 4);

            sut.forEach(x -> {});

            assertThat(sut.isRealized()).isTrue();
        }
    }

    @Nested
    class Max {

        @Test
        void returnsSingleItem() {
            assertThat(ISeq.range(1).max(Comparator.naturalOrder())).hasValue(0);
        }

        @Test
        void returnsHighestNumber() {
            var sut = ISeq.iterate(1, x -> x + 1);

            assertThat(sut.take(100).max(Comparator.naturalOrder())).hasValue(100);
        }

        @Test
        void returnsLongestString() {
            var sut = ISeq.iterate("x", x -> x + "x");

            assertThat(sut.take(6).max(Comparator.comparingInt(x -> x.length()))).hasValue("xxxxxx");
        }

        @Test
        void returnsTheLastOccurrenceOfLongestStringIfMoreThanOneItemFound() {
            var sut = ISeq.sequence(List.of("x", "xx", "aaa", "x", "bbb"));

            assertThat(sut.max(Comparator.comparingInt(x -> x.length()))).hasValue("bbb");
        }
    }

    @Nested
    class Min {

        @Test
        void returnsSingleItem() {
            assertThat(ISeq.range(1).min(Comparator.naturalOrder())).hasValue(0);
        }

        @Test
        void returnsLowestNumber() {
            var sut = ISeq.iterate(-1, x -> x - 1);

            assertThat(sut.take(100).min(Comparator.naturalOrder())).hasValue(-100);
        }

        @Test
        void returnsShortestString() {
            var sut = ISeq.sequence(List.of("xxxxxx", "xxxxx", "xxxx", "x", "xx", "xxx"));

            assertThat(sut.min(Comparator.comparingInt(x -> x.length()))).hasValue("x");
        }

        @Test
        void returnsTheLastOccurrenceOfShortestStringIfMoreThanOneItemFound() {
            var sut = ISeq.sequence(List.of("a", "xx", "aaa", "x", "bbb", "b"));

            assertThat(sut.min(Comparator.comparingInt(x -> x.length()))).hasValue("b");
        }
    }

    @Nested
    class MaxKey {

        @Test
        void returnsSingleItem() {
            assertThat(ISeq.range(1).maxKey(x -> Math.abs(x))).hasValue(0);
        }

        @Test
        void returnsHighestNumber() {
            var sut = ISeq.iterate(1, x -> x + 1);

            assertThat(sut.take(100).maxKey(x -> Math.abs(x))).hasValue(100);
        }

        @Test
        void returnsLongestString() {
            var sut = ISeq.iterate("x", x -> x + "x");

            assertThat(sut.take(6).maxKey(x -> x.length())).hasValue("xxxxxx");
        }

        @Test
        void returnsTheLastOccurrenceOfLongestStringIfMoreThanOneItemFound() {
            var sut = ISeq.sequence(List.of("x", "xx", "aaa", "x", "bbb"));

            assertThat(sut.maxKey(x -> x.length())).hasValue("bbb");
        }
    }

    @Nested
    class MinKey {

        @Test
        void returnsSingleItem() {
            assertThat(ISeq.range(1).minKey(x -> Math.abs(x))).hasValue(0);
        }

        @Test
        void returnsLowestNumber() {
            var sut = ISeq.iterate(-1, x -> x - 1);

            assertThat(sut.take(100).minKey(x -> x)).hasValue(-100);
        }

        @Test
        void returnsShortestString() {
            var sut = ISeq.sequence(List.of("xxxxxx", "xxxxx", "xxxx", "x", "xx", "xxx"));

            assertThat(sut.minKey(x -> x.length())).hasValue("x");
        }

        @Test
        void returnsTheLastOccurrenceOfShortestStringIfMoreThanOneItemFound() {
            var sut = ISeq.sequence(List.of("x", "xx", "aaa", "x", "bbb"));

            assertThat(sut.minKey(x -> x.length())).hasValue("x");
        }
    }

    @Nested
    class Find {

        @Test
        void returnsOptionalOfValueAtIndex() {
            var sut = ISeq.iterate("", x -> x + x.length());

            assertThat(sut.find(0)).hasValue("");
            assertThat(sut.find(1)).hasValue("0");
            assertThat(sut.find(2)).hasValue("01");
            assertThat(sut.find(3)).hasValue("012");
        }

        @Test
        void returnsEmptyOptionalForNegativeIndex() {
            assertThat(ISeq.range(1).find(-1)).isEmpty();
        }

        @Test
        void returnsEmptyOptionalIfIndexNotPresent() {
            assertThat(ISeq.range(1).find(1)).isEmpty();
        }
    }

    @Nested
    class FindFirst {

        @Test
        void returnsOptionalOfHead() {
            var sut = ISeq.iterate(0, x -> x + 1);

            assertThat(sut.findFirst()).hasValue(0);
        }

        @Test
        void returnsEmptyOptionalWhenNoItemsMatchPred() {
            var sut = ISeq.iterate(0, x -> x + 1).take(10);

            assertThat(sut.findFirst(x -> x < 0)).isEmpty();
        }

        @Test
        void returnsOptionalOfFirstMatchingItem() {
            var sut = ISeq.iterate(0, x -> x + 1);

            assertThat(sut.findFirst(x -> x > 100)).hasValue(101);
        }
    }

    @Test
    void realizeRealizesThisSeqAndReturnsIt() {
        var sut = ISeq.range(4);
        assertThat(sut.isRealized()).isFalse();

        var forced = sut.realize();
        assertThat(sut.isRealized()).isTrue();
        assertThat(forced).isSameAs(sut);
        assertThat(forced.isRealized()).isTrue();
    }

    @Nested
    class ToList {

        @Test
        void returnsFullyRealizedList() {
            var sut = ISeq.iterate(0, x -> x + 1);

            assertThat(sut.isRealized()).isFalse();
            assertThat(sut.take(4).toList())
                    .isInstanceOf(List.class)
                    .containsExactly(0, 1, 2, 3);
            assertThat(sut.isRealized()).isTrue();
        }

        @Test
        @Disabled("cannot handle large collections right now")
        void canHandleLargeCollections() {
            var sut = ISeq.iterate(0, x -> x + 1);

            assertThat(sut.isRealized()).isFalse();
            assertThat(sut.take(10000).toList()).hasSize(10000);
            assertThat(sut.isRealized()).isTrue();
        }
    }

    @Test
    void forEachCallsConsumerForEveryItemPresent() {
        var consumer = Mockito.<Consumer<Integer>>mock();

        var sut = ISeq.iterate(0, x -> x + 1);

        sut.take(5).forEach(consumer);

        verify(consumer).accept(0);
        verify(consumer).accept(1);
        verify(consumer).accept(2);
        verify(consumer).accept(3);
        verify(consumer).accept(4);
        verifyNoMoreInteractions(consumer);
    }

    @Test
    void runCallsProcForEveryItemPresent() {
        var proc = Mockito.<Consumer<Integer>>mock();

        var sut = ISeq.iterate(0, x -> x + 1);

        sut.take(5).run(proc);

        verify(proc).accept(0);
        verify(proc).accept(1);
        verify(proc).accept(2);
        verify(proc).accept(3);
        verify(proc).accept(4);
        verifyNoMoreInteractions(proc);
    }

    @Nested
    class Iterator {

        @Test
        void returnsIterator() {
            var sut = ISeq.iterate(0, x -> x + 1);

            var actual = sut.take(2).iterator();

            assertThat(actual.hasNext()).isTrue();
            assertThat(actual.next()).isEqualTo(0);
            assertThat(actual.hasNext()).isTrue();
            assertThat(actual.next()).isEqualTo(1);
            assertThat(actual.hasNext()).isFalse();
        }

        @Test
        void returnsInfiniteIterator() {
            var sut = ISeq.iterate(0, x -> x + 1);

            var actual = sut.iterator();

            assertThat(actual.hasNext()).isTrue();
            assertThat(actual.next()).isEqualTo(0);
            assertThat(actual.hasNext()).isTrue();
            assertThat(actual.next()).isEqualTo(1);
            assertThat(actual.hasNext()).isTrue();
        }

        @Test
        void iteratorThrowsWhenTryingToAccessNextWhenThereIsNone() {
            var actual = ISeq.iterate(0, x -> x + 1).take(2).iterator();

            assertThat(actual.next()).isEqualTo(0);
            assertThat(actual.next()).isEqualTo(1);
            assertThat(actual.hasNext()).isFalse();
            assertThatExceptionOfType(NoSuchElementException.class)
                    .isThrownBy(() -> actual.next());
        }
    }

    @Test
    void streamReturnsStream() {
        var sut = ISeq.iterate(0, x -> x + 1);

        assertThat(sut.stream().limit(3)).containsExactly(0, 1, 2);
    }

    @Test
    void parallelStreamReturnsStream() {
        var sut = ISeq.iterate(0, x -> x + 1);

        assertThat(sut.parallelStream().limit(3)).containsExactly(0, 1, 2);
    }

    @Nested
    class ToMap {

        @Test
        void returnsMapForSeqOfEntries() {
            var sut = ISeq.iterate("x", x -> x + "x").map(x -> java.util.Map.entry(x.length(), x)).take(3);

            var actual = sut.toMap();

            assertThat(actual)
                    .containsEntry(1, "x")
                    .containsEntry(2, "xx")
                    .containsEntry(3, "xxx");
        }

        @Test
        void returnsMapForSeqOfEntriesWithLastValueWinningOnCollision() {
            var sut = ISeq.sequence(List.of("a", "aa", "b", "bb")).map(x -> java.util.Map.entry(x.length(), x));

            var actual = sut.toMap();

            assertThat(actual).hasSize(2)
                    .containsEntry(1, "b")
                    .containsEntry(2, "bb");
        }

        @Test
        void throwsIfSeqIsNotOfTypeEntry() {
            var sut = ISeq.iterate("x", x -> x + "x").take(3);

            assertThatExceptionOfType(UnsupportedOperationException.class)
                    .isThrownBy(() -> sut.toMap())
                    .withMessage("ISeq is not of type Map.Entry. Provide key- and value-mappers");
        }

        @Test
        void returnsMapBasedOnKeyAndValueMapper() {
            var sut = ISeq.iterate("x", x -> x + "x");

            var actual = sut.take(3).toMap(k -> k.length(), v -> v);

            assertThat(actual)
                    .containsEntry(1, "x")
                    .containsEntry(2, "xx")
                    .containsEntry(3, "xxx");
        }

        @Test
        void throwsOnCollision() {
            var sut = ISeq.sequence(List.of("a", "b"));

            assertThatExceptionOfType(IllegalArgumentException.class)
                    .isThrownBy(() -> sut.toMap(k -> k.length(), v -> v))
                    .withMessage("duplicate key: 1");
        }

        @Test
        void returnsMapBasedOnKeyAndValueMapperWithApplyingMergerOnCollision() {
            var sut = ISeq.sequence(List.of("a", "b", "aa", "bb"));

            var actual = sut.toMap(k -> k.length(), v -> v, (a, b) -> b);

            assertThat(actual).hasSize(2)
                    .containsEntry(1, "b")
                    .containsEntry(2, "bb");
        }
    }

    @Nested
    class ToString {

        @Test
        void returnsFirstItemOnlyInSeq() {
            var sut = ISeq.iterate(0, x -> x + 1);

            assertThat(sut).hasToString("[0, ?]");
        }

        @Test
        void returnsRealisedItemsInSeq() {
            var sut = ISeq.iterate(0, x -> x + 1);

            sut.get(2);

            assertThat(sut).hasToString("[0, 1, 2, ?]");
        }

        @Test
        void returnsAllItemsInFullyRealisedSeq() {
            var sut = ISeq.iterate(0, x -> x + 1).take(4);

            sut.forEach(x -> {});

            assertThat(sut).hasToString("[0, 1, 2, 3]");
        }
    }
}