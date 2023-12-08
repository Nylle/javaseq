package com.github.nylle.javaseq;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class StringSeqTest {

    @Test
    void constructorThrows() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> new StringSeq(null, 0))
                .withMessage("string is null or empty");

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> new StringSeq("", 0))
                .withMessage("string is null or empty");
    }

    @Test
    void firstReturnsFirstCharacter() {
        assertThat(ISeq.sequence("foo").first()).isEqualTo('f');
    }

    @Test
    void restReturnsSeqOfAllCharactersButFirst() {
        var sut = ISeq.sequence("bar");

        assertThat(sut.rest().first()).isEqualTo('a');
        assertThat(sut.rest().rest().first()).isEqualTo('r');
        assertThat(sut.rest().rest().rest()).isEqualTo(ISeq.of());
    }

    @Test
    void isRealizedReturnsTrue() {
        assertThat(ISeq.sequence("foo").isRealized()).isTrue();
    }

    @Nested
    class Filter {

        @Test
        void returnsNilWhenNoItemsMatch() {
            var sut = ISeq.sequence("foobar");

            assertThat(sut.filter(x -> x == 'x')).isEmpty();
        }

        @Test
        void returnsMatchingItems() {
            var sut = ISeq.sequence("foobarbaz");

            assertThat(sut.filter(x -> x == 'a')).containsExactly('a', 'a');
        }
    }

    @Nested
    class Map {

        @Test
        void returnsAllMapResults() {
            var sut = ISeq.sequence("foo");

            assertThat(sut.map(x -> x.toString().toUpperCase().charAt(0))).containsExactly('F', 'O', 'O');
            assertThat(sut.map(x -> x.toString().toUpperCase())).containsExactly("F", "O", "O");
        }

        @Nested
        class WithOtherColl {

            @Test
            void returnsEmptySeqWhenProvidingEmptyOther() {
                assertThat(ISeq.sequence("foo").map(ISeq.sequence(""), (a, b) -> a + b)).isEmpty();
                assertThat(ISeq.sequence("foo").map(ISeq.sequence("").iterator(), (a, b) -> a + b)).isEmpty();
                assertThat(ISeq.sequence("foo").map(List.<Integer>of(), (a, b) -> a + b)).isEmpty();
                assertThat(ISeq.sequence("foo").map(Stream.<Integer>of(), (a, b) -> a + b)).isEmpty();
                assertThat(ISeq.sequence("foo").map(new Integer[0], (a, b) -> a + b)).isEmpty();
                assertThat(ISeq.sequence("foo").map("", (a, b) -> "" + a + b)).isEmpty();
            }

            @Test
            void returnsANewSeqWithTheItemsOfBothInitialSeqsAreCombinedUsingF() {
                var sut = ISeq.sequence("123");

                assertThat(sut.map(ISeq.sequence("abc"), (a, b) -> "" + a + b)).containsExactly("1a", "2b", "3c");
                assertThat(sut.map(ISeq.sequence("abc").iterator(), (a, b) -> "" + a + b)).containsExactly("1a", "2b", "3c");
                assertThat(sut.map(List.of("a", "b", "c"), (a, b) -> a + b)).containsExactly("1a", "2b", "3c");
                assertThat(sut.map(Stream.of("a", "b", "c"), (a, b) -> a + b)).containsExactly("1a", "2b", "3c");
                assertThat(sut.map(new String[]{"a", "b", "c"}, (a, b) -> a + b)).containsExactly("1a", "2b", "3c");
                assertThat(sut.map("abc", (a, b) -> "" + a + b)).containsExactly("1a", "2b", "3c");
            }

            @Test
            void ignoresRemainingItemsIfOneOfTheSeqsIsExhausted() {
                var sut = ISeq.sequence("123");

                assertThat(sut.map(ISeq.sequence("ab"), (a, b) -> "" + a + b)).containsExactly("1a", "2b");
                assertThat(sut.map(ISeq.sequence("abcd"), (a, b) -> "" + a + b)).containsExactly("1a", "2b", "3c");

                assertThat(sut.map(ISeq.sequence("ab").iterator(), (a, b) -> "" + a + b)).containsExactly("1a", "2b");
                assertThat(sut.map(ISeq.sequence("abcd").iterator(), (a, b) -> "" + a + b)).containsExactly("1a", "2b", "3c");

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
            var sut = ISeq.sequence("foo");

            assertThat(sut.mapcat(x -> List.of(x, x))).containsExactly('f', 'f', 'o', 'o', 'o', 'o');
        }

        @Test
        void returnsFlattenedSeq() {
            var sut = ISeq.of(ISeq.sequence("foo"), ISeq.sequence("bar"));

            assertThat(sut.mapcat(x -> x)).containsExactly('f', 'o', 'o', 'b', 'a', 'r');
        }

        @Test
        void ignoresEmptyResults() {
            var sut = ISeq.sequence("foo");

            assertThat(sut.mapcat(x -> x == 'f' ? List.of() : List.of(x, x))).containsExactly('o', 'o', 'o', 'o');
        }

        @Nested
        class WithOtherColl {

            @Test
            void returnsEmptySeqWhenProvidingEmptyOther() {
                assertThat(ISeq.sequence("foo").mapcat(List.<Integer>of(), (a, b) -> List.of(a, b))).isEmpty();
            }

            @Test
            void returnsANewSeqWithTheItemsOfBothInitialCollsAreCombinedUsingF() {
                var sut = ISeq.sequence("123");

                assertThat(sut.mapcat(ISeq.sequence("abc"), (a, b) -> List.of("" + a + b, "" + a + b)))
                        .containsExactly("1a", "1a", "2b", "2b", "3c", "3c");
                assertThat(sut.mapcat(List.of("a", "b", "c"), (a, b) -> List.of(a + b, a + b)))
                        .containsExactly("1a", "1a", "2b", "2b", "3c", "3c");
            }

            @Test
            void ignoresRemainingItemsIfOneOfTheSeqsIsExhausted() {
                var sut = ISeq.sequence("123");

                assertThat(sut.mapcat(ISeq.sequence("ab"), (a, b) -> List.of("" + a + b, "" + a + b)))
                        .containsExactly("1a", "1a", "2b", "2b");
                assertThat(sut.mapcat(ISeq.sequence("abcd"), (a, b) -> List.of("" + a + b, "" + a + b)))
                        .containsExactly("1a", "1a", "2b", "2b", "3c", "3c");

                assertThat(sut.mapcat(List.of("a", "b"), (a, b) -> List.of(a + b, a + b)))
                        .containsExactly("1a", "1a", "2b", "2b");
                assertThat(sut.mapcat(List.of("a", "b", "c", "d"), (a, b) -> List.of(a + b, a + b)))
                        .containsExactly("1a", "1a", "2b", "2b", "3c", "3c");
            }
        }
    }

    @Test
    void consPrependsCharacter() {
        var sut = ISeq.sequence("oo");

        assertThat(sut.cons('f')).containsExactly('f', 'o', 'o');
    }

    @Nested
    class Nth {

        @Test
        void returnsValueAtIndex() {
            var sut = ISeq.sequence("foo");

            assertThat(sut.nth(0)).isEqualTo('f');
            assertThat(sut.nth(1)).isEqualTo('o');
            assertThat(sut.nth(2)).isEqualTo('o');
        }

        @Test
        void returnsDefaultValue() {
            var sut = ISeq.sequence("foo");

            assertThat(sut.nth(0, 'x')).isEqualTo('f');
            assertThat(sut.nth(1, 'x')).isEqualTo('o');
            assertThat(sut.nth(2, 'x')).isEqualTo('o');
            assertThat(sut.nth(3, 'x')).isEqualTo('x');
        }

        @Test
        void throwsForNegativeIndex() {
            var sut = ISeq.sequence("foo");

            assertThatExceptionOfType(IndexOutOfBoundsException.class)
                    .isThrownBy(() -> sut.nth(-1))
                    .withMessage("Index out of range: -1");
        }

        @Test
        void throwsIfIndexNotPresent() {
            var sut = ISeq.sequence("0");

            assertThatExceptionOfType(IndexOutOfBoundsException.class)
                    .isThrownBy(() -> sut.nth(1))
                    .withMessage("Index out of range: 1");
        }
    }

    @Nested
    class Take {

        @Test
        void returnsNilWithNegativeItems() {
            var sut = ISeq.sequence("foo");

            assertThat(sut.take(-1))
                    .isExactlyInstanceOf(Nil.class)
                    .isEmpty();
        }

        @Test
        void returnsNilWithZeroItems() {
            var sut = ISeq.sequence("foo");

            assertThat(sut.take(0))
                    .isExactlyInstanceOf(Nil.class)
                    .isEmpty();
        }

        @Test
        void returnsStringSeqWithMoreThanZeroItems() {
            var sut = ISeq.sequence("foo");

            assertThat(sut.take(3))
                    .containsExactly('f', 'o', 'o');
        }
    }

    @Nested
    class Drop {

        @Test
        void returnsUnchangedSeqWithNegativeItemsToDrop() {
            assertThat(ISeq.sequence("foo").drop(-1)).containsExactly('f', 'o', 'o');
        }

        @Test
        void returnsUnchangedSeqWithZeroItemsToDrop() {
            assertThat(ISeq.sequence("foo").drop(0)).containsExactly('f', 'o', 'o');
        }

        @Test
        void returnsSeqOfAllButTheFirstNItems() {
            assertThat(ISeq.sequence("hello").drop(2)).containsExactly('l', 'l', 'o');
        }

        @Test
        void returnsEmptySeqIfMoreItemsAreDroppedThanPresent() {
            assertThat(ISeq.sequence("foo").drop(5)).isEmpty();
        }
    }

    @Nested
    class TakeWhile {

        @Test
        void returnsEmptySeqWhenFirstItemDoesNotMatch() {
            assertThat(ISeq.sequence("foo").takeWhile(x -> x != 'f')).isEmpty();
        }

        @Test
        void returnsSeqWithSingleMatchingItem() {
            assertThat(ISeq.sequence("foo").takeWhile(x -> x == 'f')).containsExactly('f');
        }

        @Test
        void returnsSeqWithMatchingItems() {
            assertThat(ISeq.sequence("foobar").takeWhile(x -> x > 'a')).containsExactly('f', 'o', 'o', 'b');
        }

        @Test
        void returnsSeqWithAllMatchingItems() {
            assertThat(ISeq.sequence("foo").takeWhile(x -> x > 'a')).containsExactly('f', 'o', 'o');
        }
    }

    @Nested
    class DropWhile {

        @Test
        void returnsEmptySeqWhenAllItemsMatch() {
            assertThat(ISeq.sequence("foo").dropWhile(x -> x > 'a')).isEmpty();
        }

        @Test
        void returnsSeqWithItemsThatDoNotMatch() {
            assertThat(ISeq.sequence("foo").dropWhile(x -> x < 'o').take(4)).containsExactly('o', 'o');
        }

        @Test
        void returnsEntireSeqWhenFirstItemDoesNotMatch() {
            assertThat(ISeq.sequence("foo").dropWhile(x -> x > 'f').take(4)).containsExactly('f', 'o', 'o');
        }
    }

    @Nested
    class Partition {

        @Test
        void returnsEmptySeqForNegativeSizeN() {
            var sut = ISeq.sequence("foo");

            assertThat(sut.partition(-1)).isEmpty();
            assertThat(sut.partition(-1, 10)).isEmpty();
            assertThat(sut.partition(-1, 3)).isEmpty();
            assertThat(sut.partition(-1, 1)).isEmpty();
            assertThat(sut.partition(-1, 0)).isEmpty();
            assertThat(sut.partition(-1, -1)).isEmpty();
        }

        @Test
        void returnsInfiniteSeqOfEmptyListsForZeroSizeN() {
            var sut = ISeq.sequence("foo");

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
            var sut = ISeq.sequence("01234");

            assertThat(sut.partition(1).take(3)).containsExactly(
                    List.of('0'),
                    List.of('1'),
                    List.of('2'));

            assertThat(sut.partition(1, 1).take(3)).containsExactly(
                    List.of('0'),
                    List.of('1'),
                    List.of('2'));

            assertThat(sut.partition(1, 2).take(3)).containsExactly(
                    List.of('0'),
                    List.of('2'),
                    List.of('4'));
        }

        @Test
        void returnsSeqOfListsOfNItemsEachAtOffsetsStepApart() {
            var sut = ISeq.sequence("abcdefghijklmnop");

            assertThat(sut.partition(3).take(3)).containsExactly(
                    List.of('a', 'b', 'c'),
                    List.of('d', 'e', 'f'),
                    List.of('g', 'h', 'i'));

            assertThat(sut.partition(3, 3).take(3)).containsExactly(
                    List.of('a', 'b', 'c'),
                    List.of('d', 'e', 'f'),
                    List.of('g', 'h', 'i'));

            assertThat(sut.partition(4, 6)).containsExactly(
                    List.of('a', 'b', 'c', 'd'),
                    List.of('g', 'h', 'i', 'j'),
                    List.of('m', 'n', 'o', 'p'));
        }

        @Test
        void dropsItemsThatDoNotMakeACompleteLastPartition() {
            var sut = ISeq.sequence("abcdefghijklmn");

            assertThat(sut.partition(4)).containsExactly(
                    List.of('a', 'b', 'c', 'd'),
                    List.of('e', 'f', 'g', 'h'),
                    List.of('i', 'j', 'k', 'l'));

            assertThat(sut.partition(4, 4)).containsExactly(
                    List.of('a', 'b', 'c', 'd'),
                    List.of('e', 'f', 'g', 'h'),
                    List.of('i', 'j', 'k', 'l'));

            assertThat(sut.partition(3, 4)).containsExactly(
                    List.of('a', 'b', 'c'),
                    List.of('e', 'f', 'g'),
                    List.of('i', 'j', 'k'));
        }

        @Test
        void returnsSeqOfOneEmptyListForStepGreaterThanOrEqualToSizeN() {
            var sut = ISeq.sequence("foo");

            assertThat(sut.partition(0, 3)).containsExactly(
                    List.of());

            assertThat(sut.partition(0, 4)).containsExactly(
                    List.of());
        }

        @Test
        void returnsASlidingWindowIfStepIsLowerThanSizeN() {
            var sut = ISeq.sequence("abcde");

            assertThat(sut.partition(3, 1)).containsExactly(
                    List.of('a', 'b', 'c'),
                    List.of('b', 'c', 'd'),
                    List.of('c', 'd', 'e'));
        }

        @Nested
        class WhenPadIsProvidedAndNotNull {

            @Test
            void fillsIncompleteLastPartitionWithItemsFromPad() {
                var sut = ISeq.sequence("abcdefghijklmn");

                assertThat(sut.partition(4, 4, List.of('0', '1', '2', '3'))).containsExactly(
                        List.of('a', 'b', 'c', 'd'),
                        List.of('e', 'f', 'g', 'h'),
                        List.of('i', 'j', 'k', 'l'),
                        List.of('m', 'n', '0', '1'));

                assertThat(sut.partition(3, 4, List.of('0', '1', '2', '3'))).containsExactly(
                        List.of('a', 'b', 'c'),
                        List.of('e', 'f', 'g'),
                        List.of('i', 'j', 'k'),
                        List.of('m', 'n', '0'));
            }

            @Test
            void returnsAnIncompleteLastPartitionIfItemsInPadAreFewerThanRequired() {
                var sut = ISeq.sequence("abcdefghijklmn");

                assertThat(sut.partition(4, 4, List.of())).containsExactly(
                        List.of('a', 'b', 'c', 'd'),
                        List.of('e', 'f', 'g', 'h'),
                        List.of('i', 'j', 'k', 'l'),
                        List.of('m', 'n'));

                assertThat(sut.partition(3, 4, List.of())).containsExactly(
                        List.of('a', 'b', 'c'),
                        List.of('e', 'f', 'g'),
                        List.of('i', 'j', 'k'),
                        List.of('m', 'n'));
            }
        }
    }

    @Nested
    class PartitionAll {

        @Test
        void returnsEmptySeqForNegativeSizeN() {
            var sut = ISeq.sequence("foo");

            assertThat(sut.partitionAll(-1)).isEmpty();
            assertThat(sut.partitionAll(-1, 10)).isEmpty();
            assertThat(sut.partitionAll(-1, 3)).isEmpty();
            assertThat(sut.partitionAll(-1, 1)).isEmpty();
            assertThat(sut.partitionAll(-1, 0)).isEmpty();
            assertThat(sut.partitionAll(-1, -1)).isEmpty();
        }

        @Test
        void returnsInfiniteSeqOfEmptyListsForZeroSizeN() {
            var sut = ISeq.sequence("foo");

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
            var sut = ISeq.sequence("01234");

            assertThat(sut.partitionAll(1).take(3)).containsExactly(
                    List.of('0'),
                    List.of('1'),
                    List.of('2'));

            assertThat(sut.partitionAll(1, 1).take(3)).containsExactly(
                    List.of('0'),
                    List.of('1'),
                    List.of('2'));

            assertThat(sut.partitionAll(1, 2).take(3)).containsExactly(
                    List.of('0'),
                    List.of('2'),
                    List.of('4'));
        }

        @Test
        void returnsSeqOfListsOfNItemsEachAtOffsetsStepApart() {
            var sut = ISeq.sequence("abcdefghijklmnop");

            assertThat(sut.partitionAll(3).take(3)).containsExactly(
                    List.of('a', 'b', 'c'),
                    List.of('d', 'e', 'f'),
                    List.of('g', 'h', 'i'));

            assertThat(sut.partitionAll(3, 3).take(3)).containsExactly(
                    List.of('a', 'b', 'c'),
                    List.of('d', 'e', 'f'),
                    List.of('g', 'h', 'i'));

            assertThat(sut.partitionAll(4, 6)).containsExactly(
                    List.of('a', 'b', 'c', 'd'),
                    List.of('g', 'h', 'i', 'j'),
                    List.of('m', 'n', 'o', 'p'));
        }

        @Test
        void returnsSeqOfOneEmptyListForStepGreaterThanOrEqualToSizeN() {
            var sut = ISeq.sequence("foo");

            assertThat(sut.partitionAll(0, 3)).containsExactly(
                    List.of());

            assertThat(sut.partitionAll(0, 4)).containsExactly(
                    List.of());
        }

        @Test
        void returnsASlidingWindowIfStepIsLowerThanSizeN() {
            var sut = ISeq.sequence("abcde");

            assertThat(sut.partitionAll(3, 1)).containsExactly(
                    List.of('a', 'b', 'c'),
                    List.of('b', 'c', 'd'),
                    List.of('c', 'd', 'e'),
                    List.of('d', 'e'),
                    List.of('e'));
        }

        @Test
        void returnsAnIncompleteLastPartition() {
            var sut = ISeq.sequence("abcdefghijklmn");

            assertThat(sut.partitionAll(4, 4)).containsExactly(
                    List.of('a', 'b', 'c', 'd'),
                    List.of('e', 'f', 'g', 'h'),
                    List.of('i', 'j', 'k', 'l'),
                    List.of('m', 'n'));

            assertThat(sut.partitionAll(3, 4)).containsExactly(
                    List.of('a', 'b', 'c'),
                    List.of('e', 'f', 'g'),
                    List.of('i', 'j', 'k'),
                    List.of('m', 'n'));
        }
    }

    @Nested
    class Reductions {

        @Test
        void returnsASeqWithTheIntermediateValuesOfTheReduction() {
            var sut = ISeq.sequence("foo");

            assertThat(sut.reductions((a, b) -> b)).containsExactly('f', 'o', 'o');
        }

        @Test
        void returnsASeqWithTheIntermediateValuesOfTheReductionStartingWithInit() {
            var sut = ISeq.sequence("foo");

            assertThat(sut.reductions("", (a, b) -> a + b).take(4)).containsExactly("", "f", "fo", "foo");
        }
    }

    @Nested
    class Reduce {

        @Test
        void returnsOptionalResultWhenValIsNotSupplied() {
            var sut = ISeq.sequence("foo");

            assertThat(sut.reduce((a, b) -> b)).hasValue('o');
        }

        @Test
        void returnsResultWhenValIsSupplied() {
            var sut = ISeq.sequence("foo");

            assertThat(sut.reduce("", (a, b) -> a + b)).isEqualTo("foo");
        }
    }

    @Nested
    class Distinct {

        @Test
        void returnsSeqWithSingleItem() {
            assertThat(ISeq.sequence("foo").distinct()).containsExactly('f', 'o');
        }

        @Test
        void returnsSeqThatAlreadyIsDistinct() {
            var sut = ISeq.sequence("abc");

            assertThat(sut.distinct()).containsExactly('a', 'b', 'c');
        }

        @Test
        void returnsSeqWithSingleItemForSeqWithIdenticalItems() {
            var sut = ISeq.sequence("aaaaa");

            assertThat(sut.distinct()).containsExactly('a');
        }

        @Test
        void returnsDistinctItemsInSameOrderAsEncounteredFirst() {
            var sut = ISeq.sequence("acabbdfege");

            assertThat(sut.distinct()).containsExactly('a', 'c', 'b', 'd', 'f', 'e', 'g');
        }
    }

    @Nested
    class Sorted {

        @Test
        void returnsSeqWithSingleItem() {
            assertThat(ISeq.sequence("a").sorted()).isEqualTo(ISeq.of('a'));
        }

        @Test
        void returnsSeqWithAllItemsSortedUsingDefaultComparator() {
            var sut = ISeq.sequence("acdfgeb");

            assertThat(sut.sorted()).containsExactly('a', 'b', 'c', 'd', 'e', 'f', 'g');
        }

        @Test
        void returnsSeqWithAllItemsSortedUsingSuppliedComparator() {
            var sut = ISeq.sequence("acdfgeb");

            assertThat(sut.sorted(Comparator.reverseOrder())).containsExactly('g', 'f', 'e', 'd', 'c', 'b', 'a');
        }
    }

    @Nested
    class Some {

        @Test
        void returnsFalseIfNoneOfTheItemsMatchPred() {
            var sut = ISeq.sequence("foo");

            assertThat(sut.some(x -> x > 'p')).isFalse();
        }

        @Test
        void returnsTrueIfFirstItemMatchesPred() {
            var sut = ISeq.sequence("foo");

            assertThat(sut.some(x -> x == 'f')).isTrue();
        }

        @Test
        void returnsTrueIfAllItemsMatchPred() {
            var sut = ISeq.sequence("foo");

            assertThat(sut.some(x -> x > 'e')).isTrue();
        }

        @Test
        void returnsTrueIfSomeItemMatchesPred() {
            var sut = ISeq.sequence("bar");

            assertThat(sut.some(x -> x == 'a')).isTrue();
        }

        @Test
        void returnsTrueIfLastItemMatchesPred() {
            var sut = ISeq.sequence("bar");

            assertThat(sut.some(x -> x == 'r')).isTrue();
        }
    }

    @Nested
    class Every {

        @Test
        void returnsTrueIfAllItemsInSeqMatchPred() {
            var sut = ISeq.sequence("foo");

            assertThat(sut.every(x -> x > 'e')).isTrue();
        }

        @Test
        void returnsFalseIfFirstItemDoesNotMatchPred() {
            var sut = ISeq.sequence("foo");

            assertThat(sut.every(x -> x > 'f')).isFalse();
        }

        @Test
        void returnsFalseIfAnyItemDoesNotMatchPred() {
            var sut = ISeq.sequence("bar");

            assertThat(sut.every(x -> x > 'a')).isFalse();
        }

        @Test
        void returnsFalseIfLastItemDoesNotMatchPred() {
            var sut = ISeq.sequence("bar");

            assertThat(sut.every(x -> x < 'r')).isFalse();
        }
    }

    @Nested
    class NotAny {

        @Test
        void returnsFalseIfFirstItemMatchesPred() {
            var sut = ISeq.sequence("bar");

            assertThat(sut.notAny(x -> x == 'b')).isFalse();
        }

        @Test
        void returnsFalseIfAnyItemMatchesPred() {
            var sut = ISeq.sequence("bar");

            assertThat(sut.notAny(x -> x == 'a')).isFalse();
        }

        @Test
        void returnsFalseIfAllItemsMatchPred() {
            var sut = ISeq.sequence("bar");

            assertThat(sut.notAny(x -> x >= 'a')).isFalse();
        }

        @Test
        void returnsTrueIfNoItemMatchesPred() {
            var sut = ISeq.sequence("bar");

            assertThat(sut.notAny(x -> x > 'r')).isTrue();
        }
    }

    @Nested
    class Max {

        @Test
        void returnsSingleItem() {
            assertThat(ISeq.sequence("1").max(Comparator.naturalOrder())).hasValue('1');
        }

        @Test
        void returnsHighestValue() {
            var sut = ISeq.sequence("bar");

            assertThat(sut.max(Comparator.naturalOrder())).hasValue('r');
        }
    }

    @Nested
    class Min {

        @Test
        void returnsSingleItem() {
            assertThat(ISeq.sequence("1").min(Comparator.naturalOrder())).hasValue('1');
        }

        @Test
        void returnsLowestValue() {
            var sut = ISeq.sequence("bar");

            assertThat(sut.min(Comparator.naturalOrder())).hasValue('a');
        }
    }

    @Nested
    class MaxKey {

        @Test
        void returnsSingleItem() {
            assertThat(ISeq.sequence("1").maxKey(x -> x.toString())).hasValue('1');
        }

        @Test
        void returnsHighestValue() {
            var sut = ISeq.sequence("bar");

            assertThat(sut.maxKey(x -> x.toString())).hasValue('r');
        }
    }

    @Nested
    class MinKey {

        @Test
        void returnsSingleItem() {
            assertThat(ISeq.sequence("1").minKey(x -> x.toString())).hasValue('1');
        }

        @Test
        void returnsLowestValue() {
            var sut = ISeq.sequence("bar");

            assertThat(sut.minKey(x -> x.toString())).hasValue('a');
        }
    }

    @Nested
    class Find {

        @Test
        void returnsOptionalOfValueAtIndex() {
            var sut = ISeq.sequence("bar");

            assertThat(sut.find(0)).hasValue('b');
            assertThat(sut.find(1)).hasValue('a');
            assertThat(sut.find(2)).hasValue('r');
        }

        @Test
        void returnsEmptyOptionalForNegativeIndex() {
            assertThat(ISeq.sequence("bar").find(-1)).isEmpty();
        }

        @Test
        void returnsEmptyOptionalIfIndexNotPresent() {
            assertThat(ISeq.sequence("bar").find(3)).isEmpty();
        }
    }

    @Nested
    class FindFirst {

        @Test
        void returnsOptionalOfHead() {
            var sut = ISeq.sequence("bar");

            assertThat(sut.findFirst()).hasValue('b');
        }

        @Test
        void returnsEmptyOptionalWhenNoItemsMatchPred() {
            var sut = ISeq.sequence("bar");

            assertThat(sut.findFirst(x -> x > 'r')).isEmpty();
        }

        @Test
        void returnsOptionalOfFirstMatchingItem() {
            var sut = ISeq.sequence("bar");

            assertThat(sut.findFirst(x -> x == 'a')).hasValue('a');
        }
    }

    @Test
    void runCallsProcForEveryItemPresent() {
        var proc = Mockito.<Consumer<Character>>mock();

        var sut = ISeq.sequence("012");

        sut.run(proc);

        verify(proc).accept('0');
        verify(proc).accept('1');
        verify(proc).accept('2');
        verifyNoMoreInteractions(proc);
    }

    @Nested
    class Get {

        @Test
        void returnsValueAtIndex() {
            var sut = ISeq.sequence("0123");

            assertThat(sut.get(0)).isEqualTo('0');
            assertThat(sut.get(1)).isEqualTo('1');
            assertThat(sut.get(2)).isEqualTo('2');
            assertThat(sut.get(3)).isEqualTo('3');
        }

        @Test
        void throwsForNegativeIndex() {
            assertThatExceptionOfType(IndexOutOfBoundsException.class)
                    .isThrownBy(() -> ISeq.sequence("0123").get(-1))
                    .withMessage("Index out of range: -1");
        }

        @Test
        void throwsIfIndexNotPresent() {
            assertThatExceptionOfType(IndexOutOfBoundsException.class)
                    .isThrownBy(() -> ISeq.sequence("0").get(1))
                    .withMessage("Index out of range: 1");
        }
    }

    @Test
    void isEmptyReturnsFalse() {
        assertThat(ISeq.sequence("foo").isEmpty()).isFalse();
    }

    @Test
    void sizeReturnsStringLength() {
        assertThat(ISeq.sequence("").size()).isEqualTo(0);
        assertThat(ISeq.sequence("foo").size()).isEqualTo(3);
    }

    @Test
    void toListReturnsList() {
        var sut = ISeq.sequence("bar");

        assertThat(sut.toList())
                .isInstanceOf(List.class)
                .containsExactly('b', 'a', 'r');
    }

    @Test
    void toSetReturnsSet() {
        var sut = ISeq.sequence("bar");

        assertThat(sut.toSet())
                .isInstanceOf(Set.class)
                .containsExactlyInAnyOrder('b', 'a', 'r');
    }

    @Test
    void forEachCallsConsumerForEveryItemPresent() {
        var consumer = Mockito.<Consumer<Character>>mock();

        var sut = ISeq.sequence("012");

        sut.forEach(consumer);

        verify(consumer).accept('0');
        verify(consumer).accept('1');
        verify(consumer).accept('2');
        verifyNoMoreInteractions(consumer);
    }

    @Nested
    class Iterator {

        @Test
        void returnsIterator() {
            var sut = ISeq.sequence("bar");

            var actual = sut.iterator();

            assertThat(actual.hasNext()).isTrue();
            assertThat(actual.next()).isEqualTo('b');
            assertThat(actual.hasNext()).isTrue();
            assertThat(actual.next()).isEqualTo('a');
            assertThat(actual.hasNext()).isTrue();
            assertThat(actual.next()).isEqualTo('r');
            assertThat(actual.hasNext()).isFalse();
        }

        @Test
        void iteratorThrowsWhenTryingToAccessNextWhenThereIsNone() {
            var actual = ISeq.sequence("01").iterator();

            assertThat(actual.next()).isEqualTo('0');
            assertThat(actual.next()).isEqualTo('1');
            assertThat(actual.hasNext()).isFalse();
            assertThatExceptionOfType(NoSuchElementException.class)
                    .isThrownBy(() -> actual.next());
        }
    }

    @Test
    void streamReturnsStream() {
        var sut = ISeq.sequence("bar");

        assertThat(sut.stream()).containsExactly('b', 'a', 'r');
    }

    @Test
    void parallelStreamReturnsStream() {
        var sut = ISeq.sequence("bar");

        assertThat(sut.parallelStream()).containsExactly('b', 'a', 'r');
    }

    @Nested
    class ToMap {

        @Test
        void throwsWithoutKeyAndValueMapper() {
            var sut = ISeq.sequence("bar");

            assertThatExceptionOfType(UnsupportedOperationException.class)
                    .isThrownBy(() -> sut.toMap())
                    .withMessage("ISeq is not of type Map.Entry. Provide key- and value-mappers");
        }

        @Test
        void returnsMapBasedOnKeyAndValueMapper() {
            var sut = ISeq.sequence("bar");

            var actual = sut.toMap(k -> k, v -> v.toString());

            assertThat(actual)
                    .containsEntry('b', "b")
                    .containsEntry('a', "a")
                    .containsEntry('r', "r");
        }

        @Test
        void throwsOnCollision() {
            var sut = ISeq.sequence("foo");

            assertThatExceptionOfType(IllegalArgumentException.class)
                    .isThrownBy(() -> sut.toMap(k -> k, v -> v.toString()))
                    .withMessage("duplicate key: o");
        }

        @Test
        void returnsMapBasedOnKeyAndValueMapperWithApplyingMergerOnCollision() {
            var sut = ISeq.sequence("foo");

            var actual = sut.toMap(k -> k.toString(), v -> List.of(v), (a, b) -> List.of(a.get(0), b.get(0)));

            assertThat(actual).hasSize(2)
                    .containsEntry("f", List.of('f'))
                    .containsEntry("o", List.of('o', 'o'));
        }
    }

    @Test
    void toStringReturnsAllItemsInSeq() {
        var sut = ISeq.sequence("bar");

        assertThat(sut).hasToString("[b, a, r]");
    }
}