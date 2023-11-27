package com.github.nylle.javaseq;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;

import static com.github.nylle.javaseq.Seq.cons;
import static org.assertj.core.api.Assertions.assertThat;

class ConsTest {

    @Test
    void firstReturnsHead() {
        var sut = Seq.iterate(0, x -> x + 1);

        assertThat(sut.first()).isEqualTo(0);
    }

    @Test
    void restReturnsTail() {
        var sut = Seq.iterate(0, x -> x + 1);

        var rest = sut.rest();

        assertThat(rest.first()).isEqualTo(1);
        assertThat(rest.rest().first()).isEqualTo(2);
        assertThat(rest.rest().rest().first()).isEqualTo(3);
    }

    @Test
    void sizeReturnsSizeOfFiniteSeqOrRunsForever() {
        var sut = cons(3, () -> cons(-2, () -> cons(8, () -> Seq.of(1))));

        assertThat(sut.size()).isEqualTo(4);
    }

    @Test
    void isEmptyReturnsFalse() {
        var sut = Seq.iterate("", x -> x + x.length());

        assertThat(sut.isEmpty()).isFalse();
    }

    @Nested
    class Get {

        @Test
        void returnsValueAtIndex() {
            var sut = Seq.iterate("", x -> x + x.length());

            assertThat(sut.get(0)).isEqualTo("");
            assertThat(sut.get(1)).isEqualTo("0");
            assertThat(sut.get(2)).isEqualTo("01");
            assertThat(sut.get(3)).isEqualTo("012");
        }

        @Test
        void returnsNullForNegativeIndex() {
            assertThat(Seq.of(1).get(-1)).isNull();
        }

        @Test
        void returnsNullIfIndexNotPresent() {
            assertThat(Seq.of(1).get(1)).isNull();
        }
    }

    @Nested
    class Take {

        @Test
        void returnsNilWithNegativeItems() {
            var sut = Seq.iterate(0, x -> x + 1);

            assertThat(sut.take(-1))
                    .isExactlyInstanceOf(Nil.class)
                    .isEmpty();
        }

        @Test
        void returnsNilWithZeroItems() {
            var sut = Seq.iterate(0, x -> x + 1);

            assertThat(sut.take(0))
                    .isExactlyInstanceOf(Nil.class)
                    .isEmpty();
        }

        @Test
        void returnsConsWithMoreThanZeroItems() {
            var sut = Seq.iterate(0, x -> x + 1);

            assertThat(sut.take(3))
                    .isExactlyInstanceOf(Cons.class)
                    .containsExactly(0, 1, 2);
        }
    }

    @Nested
    class Drop {

        @Test
        void returnsUnchangedSeqWithNegativeElementsToDrop() {
            assertThat(Seq.of(1, 2, 3, 4).drop(-1)).containsExactly(1, 2, 3, 4);
        }

        @Test
        void returnsUnchangedSeqWithZeroElementsToDrop() {
            assertThat(Seq.of(1, 2, 3, 4).drop(0)).containsExactly(1, 2, 3, 4);
        }

        @Test
        void returnsSeqOfAllButTheFirstNItems() {
            assertThat(Seq.of(1, 2, 3, 4).drop(2)).containsExactly(3, 4);
        }

        @Test
        void returnsEmptySeqIfMoreElementsAreDroppedThanPresent() {
            assertThat(Seq.of(1, 2, 3, 4).drop(5)).isEmpty();
        }

        @Test
        void isLazy() {
            var sut = Seq.iterate(0, x -> x + 1);

            assertThat(sut.drop(2).take(2)).containsExactly(2, 3);
        }
    }

    @Nested
    class Filter {

        @Test
        void returnsNilWhenNoElementsMatch() {
            var sut = Seq.iterate(0, x -> x + 1).take(10);

            assertThat(sut.filter(x -> x < 0)).isEmpty();
        }

        @Test
        void returnsMatchingElements() {
            var sut = Seq.iterate(0, x -> x + 1);

            assertThat(sut.filter(x -> x > 100).take(3)).containsExactly(101, 102, 103);
        }
    }

    @Nested
    class Map {

        @Test
        void returnsSingleMapResult() {
            var sut = Seq.of("xxx");

            assertThat(sut.map(x -> x.length())).isEqualTo(Seq.of(3));
        }

        @Test
        void returnsAllMapResults() {
            var sut = Seq.of("xxx", "ab", "baz", "foobar");

            assertThat(sut.map(x -> x.length())).isEqualTo(Seq.of(3, 2, 3, 6));
        }

        @Test
        void returnsInfiniteMapResults() {
            var sut = Seq.iterate(0, x -> x + 1);

            assertThat(sut.map(x -> x * 100).take(3)).containsExactly(0, 100, 200);
        }

        @Nested
        class WithOtherSeq {

            @Test
            void returnsEmptySeqWhenProvidingEmptyOther() {
                assertThat(Seq.of(1, 2, 3).map(Seq.<Integer>of(), (a, b) -> a + b)).isEmpty();
            }

            @Test
            void returnsANewSeqWithTheItemsOfBothInitialSeqsAreCombinedUsingF() {
                var sut = Seq.of(1, 2, 3);

                assertThat(sut.map(Seq.of("a", "b", "c"), (a, b) -> a + b)).containsExactly("1a", "2b", "3c");
            }

            @Test
            void ignoresRemainingItemsIfOneOfTheSeqsIsExhausted() {
                var sut = Seq.of(1, 2, 3);

                assertThat(sut.map(Seq.of("a", "b"), (a, b) -> a + b)).containsExactly("1a", "2b");
                assertThat(sut.map(Seq.of("a", "b", "c", "d"), (a, b) -> a + b)).containsExactly("1a", "2b", "3c");
            }

            @Test
            void isLazy() {
                var sut = Seq.iterate(0, x -> x + 1);
                var other = Seq.iterate(0, x -> x + 1);

                assertThat(sut.map(other, (a, b) -> a + b).take(4)).containsExactly(0, 2, 4, 6);
            }
        }
    }

    @Nested
    class Mapcat {

        @Test
        void returnsFlattenedSeq() {
            var sut = Seq.of(Seq.of(0, 1, 2), Seq.of(3, 4, 5));

            assertThat(sut.mapcat(x -> x)).containsExactly(0, 1, 2, 3, 4, 5);
        }

        @Test
        void isLazy() {
            var sut = Seq.iterate(0, x -> x + 1);

            assertThat(sut.mapcat(x -> List.of(x, x)).take(6)).containsExactly(0, 0, 1, 1, 2, 2);
        }

        @Test
        void ignoresEmptyResults() {
            var sut = Seq.iterate(0, x -> x + 1);

            assertThat(sut.mapcat(x -> x == 0 ? List.of() : List.of(x, x)).take(6)).containsExactly(1, 1, 2, 2, 3, 3);
        }
    }

    @Nested
    class TakeWhile {

        @Test
        void returnsEmptySeqWhenFirstItemDoesNotMatch() {
            assertThat(Seq.of(1).takeWhile(x -> x > 1)).isEmpty();
            assertThat(Seq.iterate(0, x -> x + 1).takeWhile(x -> x > 0)).isEmpty();
        }

        @Test
        void returnsSeqWithSingleMatchingItem() {
            assertThat(Seq.of(1).takeWhile(x -> x > 0)).containsExactly(1);
            assertThat(Seq.iterate(0, x -> x + 1).takeWhile(x -> x < 1)).containsExactly(0);
        }

        @Test
        void returnsSeqWithMatchingItems() {
            assertThat(Seq.iterate(0, x -> x + 1).takeWhile(x -> x < 3)).containsExactly(0, 1, 2);
        }

        @Test
        void returnsSeqWithAllMatchingItems() {
            assertThat(Seq.of(1, 2, 3).takeWhile(x -> true)).containsExactly(1, 2, 3);
            assertThat(Seq.iterate(0, x -> x + 1).takeWhile(x -> true).take(4)).containsExactly(0, 1, 2, 3);
        }
    }

    @Nested
    class DropWhile {

        @Test
        void returnsEmptySeqWhenAllItemsMatch() {
            assertThat(Seq.of(1).dropWhile(x -> x > 0)).isEmpty();
            assertThat(Seq.of(1, 2, 3, 4).dropWhile(x -> x > 0)).isEmpty();
        }

        @Test
        void returnsSeqWithItemsThatDoNotMatch() {
            assertThat(Seq.of(1).dropWhile(x -> x > 1)).containsExactly(1);
            assertThat(Seq.of(1, 2, 3, 4).dropWhile(x -> x < 3)).containsExactly(3, 4);
            assertThat(Seq.iterate(0, x -> x + 1).dropWhile(x -> x < 2).take(4)).containsExactly(2, 3, 4, 5);
        }

        @Test
        void returnsEntireSeqWhenFirstItemDoesNotMatch() {
            assertThat(Seq.of(1, 2, 3, 4).dropWhile(x -> x > 2)).containsExactly(1, 2, 3, 4);
            assertThat(Seq.iterate(0, x -> x + 1).dropWhile(x -> x > 2).take(4)).containsExactly(0, 1, 2, 3);
        }
    }

    @Nested
    class Partition {

        @Test
        void returnsEmptySeqForNegativeSizeN() {
            var sut = Seq.of(1, 2, 3);

            assertThat(sut.partition(-1)).isEmpty();
            assertThat(sut.partition(-1, 10)).isEmpty();
            assertThat(sut.partition(-1, 3)).isEmpty();
            assertThat(sut.partition(-1, 1)).isEmpty();
            assertThat(sut.partition(-1, 0)).isEmpty();
            assertThat(sut.partition(-1, -1)).isEmpty();
        }

        @Test
        void returnsInfiniteSeqOfEmptyListsForZeroSizeN() {
            var sut = Seq.of(1, 2, 3);

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
            var sut = Seq.iterate(0, x -> x + 1);

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
            var sut = Seq.iterate(0, x -> x + 1);

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
            var sut = Seq.iterate(0, x -> x + 1).take(14);

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
            var sut = Seq.of(1, 2, 3);

            assertThat(sut.partition(0, 3)).containsExactly(
                    List.of());

            assertThat(sut.partition(0, 4)).containsExactly(
                    List.of());
        }

        @Test
        void returnsASlidingWindowIfStepIsLowerThanSizeN() {
            var sut = Seq.iterate(0, x -> x + 1);

            assertThat(sut.partition(3, 2).take(3)).containsExactly(
                    List.of(0, 1, 2),
                    List.of(2, 3, 4),
                    List.of(4, 5, 6));
        }

        @Nested
        class WhenPadIsProvidedAndNotNull {

            @Test
            void fillsIncompleteLastPartitionWithItemsFromPad() {
                var sut = Seq.iterate(0, x -> x + 1).take(14);

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
                var sut = Seq.iterate(0, x -> x + 1).take(14);

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
            var sut = Seq.of(1, 2, 3);

            assertThat(sut.partitionAll(-1)).isEmpty();
            assertThat(sut.partitionAll(-1, 10)).isEmpty();
            assertThat(sut.partitionAll(-1, 3)).isEmpty();
            assertThat(sut.partitionAll(-1, 1)).isEmpty();
            assertThat(sut.partitionAll(-1, 0)).isEmpty();
            assertThat(sut.partitionAll(-1, -1)).isEmpty();
        }

        @Test
        void returnsInfiniteSeqOfEmptyListsForZeroSizeN() {
            var sut = Seq.of(1, 2, 3);

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
            var sut = Seq.iterate(0, x -> x + 1);

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
            var sut = Seq.iterate(0, x -> x + 1);

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
            var sut = Seq.of(1, 2, 3);

            assertThat(sut.partitionAll(0, 3)).containsExactly(
                    List.of());

            assertThat(sut.partitionAll(0, 4)).containsExactly(
                    List.of());
        }

        @Test
        void returnsASlidingWindowIfStepIsLowerThanSizeN() {
            var sut = Seq.iterate(0, x -> x + 1);

            assertThat(sut.partitionAll(3, 2).take(3)).containsExactly(
                    List.of(0, 1, 2),
                    List.of(2, 3, 4),
                    List.of(4, 5, 6));
        }

        @Test
        void returnsAnIncompleteLastPartition() {
            var sut = Seq.iterate(0, x -> x + 1).take(14);

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
            var sut = Seq.iterate(1, x -> x + 1);

            assertThat(sut.reductions((a, b) -> a + b).take(3)).containsExactly(1, 3, 6);
        }

        @Test
        void returnsASeqWithTheIntermediateValuesOfTheReductionStartingWithInit() {
            var sut = Seq.iterate(1, x -> x + 1);

            assertThat(sut.reductions(0, (a, b) -> a + b).take(4)).containsExactly(0, 1, 3, 6);
        }
    }

    @Nested
    class Reduce {

        @Test
        void returnsOptionalResultWhenValIsNotSupplied() {
            var sut = Seq.iterate(0, x -> x + 1).take(4);

            assertThat(sut.reduce((a, b) -> a + b)).hasValue(6);
        }

        @Test
        void returnsResultWhenValIsSupplied() {
            var sut = Seq.iterate(1, x -> x + 1).take(3);

            assertThat(sut.reduce(0, (a, b) -> a + b)).isEqualTo(6);
        }

        @Test
        void returnsResultOfDifferentTypeThanSeq() {
            var sut = Seq.of("a", "bb", "ccc", "dddd");

            assertThat(sut.reduce(0, (acc, x) -> acc + x.length())).isEqualTo(10);
        }
    }

    @Nested
    class Distinct {

        @Test
        void returnsSeqWithSingleItem() {
            assertThat(Seq.of(1).distinct()).containsExactly(1);
        }

        @Test
        void returnsSeqThatAlreadyIsDistinct() {
            var sut = Seq.iterate(0, x -> x + 1);

            assertThat(sut.distinct().take(4)).containsExactly(0, 1, 2, 3);
        }

        @Test
        void returnsSeqWithSingleItemForSeqWithIdenticalItems() {
            var sut = Seq.cons("a", () -> Seq.of("a"));

            assertThat(sut.distinct().take(4)).containsExactly("a");
        }

        @Test
        void returnsDistinctItemsInSameOrderAsEncounteredFirst() {
            var sut = Seq.of("a", "c", "a", "b", "b", "d", "f", "e", "g", "e");

            assertThat(sut.distinct()).containsExactly("a", "c", "b", "d", "f", "e", "g");
        }
    }

    @Nested
    class Sorted {

        @Test
        void returnsSeqWithSingleItem() {
            assertThat(Seq.of(1).sorted()).isEqualTo(Seq.of(1));
        }

        @Test
        void returnsSeqWithAllItemsSortedUsingDefaultComparator() {
            var sut = Seq.iterate(10, x -> x - 1);

            assertThat(sut.take(4).sorted()).containsExactly(7, 8, 9, 10);
        }

        @Test
        void returnsSeqWithAllItemsSortedUsingSuppliedComparator() {
            var sut = Seq.iterate(0, x -> x + 1);

            assertThat(sut.take(4).sorted(Comparator.reverseOrder())).containsExactly(3, 2, 1, 0);
        }
    }

    @Nested
    class Some {

        @Test
        void returnsFalseIfNoneOfTheItemsMatchPred() {
            var sut = Seq.iterate(0, x -> x + 1);

            assertThat(sut.take(10).some(x -> x < 0)).isFalse();
        }

        @Test
        void returnsTrueIfAllItemsMatchPred() {
            var sut = Seq.iterate(0, x -> x + 1);

            assertThat(sut.take(10).some(x -> x >= 0)).isTrue();
        }

        @Test
        void returnsTrueIfFirstItemInInfiniteSeqMatchesPred() {
            var sut = Seq.iterate(0, x -> x + 1);

            assertThat(sut.some(x -> x == 0)).isTrue();
        }

        @Test
        void returnsTrueIfSomeItemInInfiniteSeqMatchesPred() {
            var sut = Seq.iterate(0, x -> x + 1);

            assertThat(sut.some(x -> x == 5)).isTrue();
        }

        @Test
        void returnsTrueIfLastItemMatchesPred() {
            var sut = Seq.iterate(0, x -> x + 1);

            assertThat(sut.take(10).some(x -> x == 9)).isTrue();
        }
    }

    @Nested
    class Every {

        @Test
        void returnsTrueIfAllItemsInSeqMatchPred() {
            var sut = Seq.iterate(1, x -> x + 1);

            assertThat(sut.take(100).every(x -> x > 0)).isTrue();
        }

        @Test
        void returnsFalseIfFirstItemInInfiniteSeqDoesNotMatchPred() {
            var sut = Seq.iterate(0, x -> x + 1);

            assertThat(sut.every(x -> x > 0)).isFalse();
        }

        @Test
        void returnsFalseIfAnyItemInInfiniteSeqDoesNotMatchPred() {
            var sut = Seq.iterate(0, x -> x + 1);

            assertThat(sut.every(x -> x < 100)).isFalse();
        }

        @Test
        void returnsFalseIfLastItemInInfiniteSeqDoesNotMatchPred() {
            var sut = Seq.iterate(1, x -> x + 1);

            assertThat(sut.take(100).every(x -> x < 100)).isFalse();
        }
    }

    @Nested
    class NotAny {

        @Test
        void returnsFalseIfFirstItemMatchesPred() {
            var sut = Seq.iterate(0, x -> x + 1);

            assertThat(sut.notAny(x -> x == 0)).isFalse();
        }

        @Test
        void returnsFalseIfAnyItemMatchesPred() {
            var sut = Seq.iterate(0, x -> x + 1);

            assertThat(sut.notAny(x -> x == 100)).isFalse();
        }

        @Test
        void returnsFalseIfAllItemsMatchPred() {
            var sut = Seq.iterate(1, x -> x + 1);

            assertThat(sut.notAny(x -> x > 0)).isFalse();
        }

        @Test
        void returnsTrueIfNoItemMatchesPred() {
            var sut = Seq.iterate(0, x -> x + 1);

            assertThat(sut.take(100).notAny(x -> x < 0)).isTrue();
        }
    }

    @Nested
    class Max {

        @Test
        void returnsSingleItem() {
            assertThat(Seq.of(1).max(Comparator.naturalOrder())).hasValue(1);
        }

        @Test
        void returnsHighestNumber() {
            var sut = Seq.iterate(1, x -> x + 1);

            assertThat(sut.take(100).max(Comparator.naturalOrder())).hasValue(100);
        }

        @Test
        void returnsLongestString() {
            var sut = Seq.iterate("x", x -> x + "x");

            assertThat(sut.take(6).max(Comparator.comparingInt(x -> x.length()))).hasValue("xxxxxx");
        }
    }

    @Nested
    class Min {

        @Test
        void returnsSingleItem() {
            assertThat(Seq.of(1).min(Comparator.naturalOrder())).hasValue(1);
        }

        @Test
        void returnsHighestNumber() {
            var sut = Seq.iterate(-1, x -> x - 1);

            assertThat(sut.take(100).min(Comparator.naturalOrder())).hasValue(-100);
        }

        @Test
        void returnsLongestString() {
            var sut = Seq.of("xxxxxx", "xxxxx", "xxxx", "x", "xx", "xxx");

            assertThat(sut.take(6).min(Comparator.comparingInt(x -> x.length()))).hasValue("x");
        }
    }

    @Test
    void toListReturnsFullyRealizedList() {
        assertThat(Seq.iterate(0, x -> x + 1).take(4).toList())
                .isInstanceOf(List.class)
                .containsExactly(0, 1, 2, 3);
    }
}