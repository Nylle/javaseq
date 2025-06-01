package com.github.nylle.javaseq;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class StringSeqTest {

    private static ISeq<Character> sutFromString(String str) {
        return new StringSeq(str, 0, str.length());
    }

    @Test
    void constructorThrows() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> new StringSeq(null, 0, 1))
                .withMessage("string is null or empty");

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> new StringSeq("", 0, 1))
                .withMessage("string is null or empty");

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> new StringSeq("abc", 3, 4))
                .withMessage("index 3 is out of range for string abc");

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> new StringSeq("abc", 2, 4))
                .withMessage("end 4 is out of range for string abc");

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> new StringSeq("abc", 2, 2))
                .withMessage("end 2 must be greater than index 2");
    }

    @Nested
    class First {

        @Test
        void returnsFirstCharacter() {
            assertThat(sutFromString("foo").first()).isEqualTo('f');
        }

        @Test
        void returnsNUllIfSeqIsEmpty() {
            assertThat(sutFromString("1").drop(1).first()).isNull();
        }
    }

    @Nested
    class Second {

        @Test
        void returnsSecondCharacter() {
            var sut = sutFromString("foo");

            assertThat(sut.second()).isEqualTo('o');
        }

        @Test
        void returnsNullIfSeqHasOnlyOneElement() {
            var sut = sutFromString("f");

            assertThat(sut.second()).isNull();
        }
    }

    @Nested
    class Last {

        @Test
        void returnsLastCharacter() {
            assertThat(sutFromString("foo").last()).isEqualTo('o');
        }

        @Test
        void returnsNullIfSeqIsEmpty() {
            assertThat(sutFromString("1").drop(1).last()).isNull();
        }
    }

    @Test
    void restReturnsSeqOfAllCharactersButFirst() {
        var sut = sutFromString("bar");

        assertThat(sut.rest().first()).isEqualTo('a');
        assertThat(sut.rest().rest().first()).isEqualTo('r');
        assertThat(sut.rest().rest().rest()).isEqualTo(Nil.empty());
    }

    @Test
    void isRealizedReturnsTrue() {
        assertThat(sutFromString("foo").isRealized()).isTrue();
    }

    @Nested
    class Filter {

        @Test
        void returnsNilWhenNoItemsMatch() {
            assertThat(sutFromString("foobar").filter(x -> x == 'x')).isEmpty();
            assertThat(sutFromString("xfoobar").rest().filter(x -> x == 'x')).isEmpty();
        }

        @Test
        void returnsMatchingItems() {
            assertThat(sutFromString("foobarbaz").filter(x -> x > 'f')).containsExactly('o', 'o', 'r', 'z');
            assertThat(sutFromString("xfoobarbaz").rest().filter(x -> x > 'f')).containsExactly('o', 'o', 'r', 'z');
        }
    }

    @Nested
    class Map {

        @Test
        void returnsAllMapResults() {
            var sut = sutFromString("foo");

            assertThat(sut.map(x -> x.toString().toUpperCase().charAt(0))).containsExactly('F', 'O', 'O');
            assertThat(sut.map(x -> x.toString().toUpperCase())).containsExactly("F", "O", "O");
        }

        @Test
        void returnsSeqWithInfiniteSeqs() {
            var sut = sutFromString("0123");

            var actual = sut.map(x -> ISeq.iterate("0", i -> "" + x + i));

            assertThat(actual).hasSize(4);
            assertThat(actual.nth(0).take(3)).containsExactly("0", "00", "000");
            assertThat(actual.nth(1).take(3)).containsExactly("0", "10", "110");
            assertThat(actual.nth(2).take(3)).containsExactly("0", "20", "220");
            assertThat(actual.nth(3).take(3)).containsExactly("0", "30", "330");
        }

        @Nested
        class WithOtherColl {

            @Test
            void returnsEmptySeqWhenProvidingEmptyOther() {
                assertThat(sutFromString("foo").map(Nil.<Integer>empty(), (a, b) -> a + b)).isEmpty();
                assertThat(sutFromString("foo").map(Collections.<Integer>emptyIterator(), (a, b) -> a + b)).isEmpty();
                assertThat(sutFromString("foo").map(List.<Integer>of(), (a, b) -> a + b)).isEmpty();
                assertThat(sutFromString("foo").map(Stream.<Integer>of(), (a, b) -> a + b)).isEmpty();
                assertThat(sutFromString("foo").map(new Integer[0], (a, b) -> a + b)).isEmpty();
                assertThat(sutFromString("foo").map("", (a, b) -> "" + a + b)).isEmpty();
            }

            @Test
            void returnsANewSeqWithTheItemsOfBothInitialSeqsAreCombinedUsingF() {
                var sut = sutFromString("123");

                assertThat(sut.map(sutFromString("abc"), (a, b) -> "" + a + b)).containsExactly("1a", "2b", "3c");
                assertThat(sut.map(List.of('a', 'b', 'c').iterator(), (a, b) -> "" + a + b)).containsExactly("1a", "2b", "3c");
                assertThat(sut.map(List.of("a", "b", "c"), (a, b) -> a + b)).containsExactly("1a", "2b", "3c");
                assertThat(sut.map(Stream.of("a", "b", "c"), (a, b) -> a + b)).containsExactly("1a", "2b", "3c");
                assertThat(sut.map(new String[]{"a", "b", "c"}, (a, b) -> a + b)).containsExactly("1a", "2b", "3c");
                assertThat(sut.map("abc", (a, b) -> "" + a + b)).containsExactly("1a", "2b", "3c");
            }

            @Test
            void ignoresRemainingItemsIfOneOfTheSeqsIsExhausted() {
                var sut = sutFromString("123");

                assertThat(sut.map(sutFromString("ab"), (a, b) -> "" + a + b)).containsExactly("1a", "2b");
                assertThat(sut.map(sutFromString("abcd"), (a, b) -> "" + a + b)).containsExactly("1a", "2b", "3c");

                assertThat(sut.map(sutFromString("ab").iterator(), (a, b) -> "" + a + b)).containsExactly("1a", "2b");
                assertThat(sut.map(sutFromString("abcd").iterator(), (a, b) -> "" + a + b)).containsExactly("1a", "2b", "3c");

                assertThat(sut.map(List.of("a", "b"), (a, b) -> a + b)).containsExactly("1a", "2b");
                assertThat(sut.map(List.of("a", "b", "c", "d"), (a, b) -> a + b)).containsExactly("1a", "2b", "3c");

                assertThat(sut.map(Stream.of("a", "b"), (a, b) -> a + b)).containsExactly("1a", "2b");
                assertThat(sut.map(Stream.of("a", "b", "c", "d"), (a, b) -> a + b)).containsExactly("1a", "2b", "3c");

                assertThat(sut.map(new String[]{"a", "b"}, (a, b) -> a + b)).containsExactly("1a", "2b");
                assertThat(sut.map(new String[]{"a", "b", "c", "d"}, (a, b) -> a + b)).containsExactly("1a", "2b", "3c");

                assertThat(sut.map("ab", (a, b) -> "" + a + b)).containsExactly("1a", "2b");
                assertThat(sut.map("abcd", (a, b) -> "" + a + b)).containsExactly("1a", "2b", "3c");

                assertThat(sut.map(ISeq.iterate("x", x -> x + "x"), (a, b) -> "" + a + b)).containsExactly("1x", "2xx", "3xxx");
            }

            @Test
            void returnsSeqWithInfiniteSeqsIfMappingResultIsInfinite() {
                var sut = sutFromString("0123");

                var actual = sut.map(ISeq.iterate(0, x -> x + 1), (a, b) -> ISeq.iterate("0", i -> i + a + b));

                assertThat(actual).hasSize(4);
                assertThat(actual.nth(0).take(3)).containsExactly("0", "000", "00000");
                assertThat(actual.nth(1).take(3)).containsExactly("0", "011", "01111");
                assertThat(actual.nth(2).take(3)).containsExactly("0", "022", "02222");
                assertThat(actual.nth(3).take(3)).containsExactly("0", "033", "03333");
            }
        }
    }

    @Nested
    class Mapcat {

        @Test
        void returnsFlattenedMapResult() {
            var sut = sutFromString("foo");

            assertThat(sut.mapcat(x -> List.of(x, x))).containsExactly('f', 'f', 'o', 'o', 'o', 'o');
        }

        @Test
        void returnsFlattenedSeq() {
            var sut = ISeq.of(sutFromString("foo"), sutFromString("bar"));

            assertThat(sut.mapcat(x -> x)).containsExactly('f', 'o', 'o', 'b', 'a', 'r');
        }

        @Test
        void ignoresEmptyResults() {
            var sut = sutFromString("foo");

            assertThat(sut.mapcat(x -> x == 'f' ? List.of() : List.of(x, x))).containsExactly('o', 'o', 'o', 'o');
        }

        @Test
        void returnsInfiniteLazySeqIfMappingResultIsInfinite() {
            var sut = sutFromString("0123");

            assertThat(sut.mapcat(x -> ISeq.iterate("Y", y -> y + "Y")).take(4)).containsExactly("Y", "YY", "YYY", "YYYY");
        }

        @Nested
        class WithOtherColl {

            @Test
            void returnsEmptySeqWhenProvidingEmptyOther() {
                assertThat(sutFromString("foo").mapcat(List.<Integer>of(), (a, b) -> List.of(a, b))).isEmpty();
            }

            @Test
            void returnsANewSeqWithTheItemsOfBothInitialCollsAreCombinedUsingF() {
                var sut = sutFromString("123");

                assertThat(sut.mapcat(sutFromString("abc"), (a, b) -> List.of("" + a + b, "" + a + b)))
                        .containsExactly("1a", "1a", "2b", "2b", "3c", "3c");
                assertThat(sut.mapcat(List.of("a", "b", "c"), (a, b) -> List.of(a + b, a + b)))
                        .containsExactly("1a", "1a", "2b", "2b", "3c", "3c");
            }

            @Test
            void ignoresRemainingItemsIfOneOfTheSeqsIsExhausted() {
                var sut = sutFromString("123");

                assertThat(sut.mapcat(sutFromString("ab"), (a, b) -> List.of("" + a + b, "" + a + b)))
                        .containsExactly("1a", "1a", "2b", "2b");
                assertThat(sut.mapcat(sutFromString("abcd"), (a, b) -> List.of("" + a + b, "" + a + b)))
                        .containsExactly("1a", "1a", "2b", "2b", "3c", "3c");

                assertThat(sut.mapcat(List.of("a", "b"), (a, b) -> List.of(a + b, a + b)))
                        .containsExactly("1a", "1a", "2b", "2b");
                assertThat(sut.mapcat(List.of("a", "b", "c", "d"), (a, b) -> List.of(a + b, a + b)))
                        .containsExactly("1a", "1a", "2b", "2b", "3c", "3c");

                assertThat(sut.mapcat(ISeq.iterate("Y", y -> y + "Y"), (a, b) -> List.of(a + b, a + b)))
                        .containsExactly("1Y", "1Y", "2YY", "2YY", "3YYY", "3YYY");
            }

            @Test
            void returnsInfiniteLazySeqIfMappingResultIsInfinite() {
                var sut = sutFromString("123");
                var other = List.of("a", "b", "c");

                assertThat(sut.mapcat(other, (a, b) -> ISeq.iterate("Y", y -> y + a + b)).take(4)).containsExactly("Y", "Y1a", "Y1a1a", "Y1a1a1a");
            }
        }
    }

    @Test
    void consPrependsCharacter() {
        var sut = sutFromString("oo");

        assertThat(sut.cons('f')).containsExactly('f', 'o', 'o');
    }

    @Nested
    class Nth {

        @Test
        void returnsValueAtIndex() {
            var sut = sutFromString("xfoo");

            assertThat(sut.nth(0)).isEqualTo('x');
            assertThat(sut.nth(1)).isEqualTo('f');
            assertThat(sut.nth(2)).isEqualTo('o');
            assertThat(sut.nth(3)).isEqualTo('o');

            assertThat(sut.drop(1).nth(0)).isEqualTo('f');
            assertThat(sut.drop(1).nth(1)).isEqualTo('o');
            assertThat(sut.drop(1).nth(2)).isEqualTo('o');
        }

        @Test
        void returnsDefaultValue() {
            var sut = sutFromString("xfoo");

            assertThat(sut.nth(0, 'Y')).isEqualTo('x');
            assertThat(sut.nth(1, 'Y')).isEqualTo('f');
            assertThat(sut.nth(2, 'Y')).isEqualTo('o');
            assertThat(sut.nth(3, 'Y')).isEqualTo('o');
            assertThat(sut.nth(4, 'Y')).isEqualTo('Y');

            assertThat(sut.drop(1).nth(0, 'Y')).isEqualTo('f');
            assertThat(sut.drop(1).nth(1, 'Y')).isEqualTo('o');
            assertThat(sut.drop(1).nth(2, 'Y')).isEqualTo('o');
            assertThat(sut.drop(1).nth(3, 'Y')).isEqualTo('Y');
        }

        @Test
        void throwsForNegativeIndex() {
            var sut = sutFromString("foo");

            assertThatExceptionOfType(IndexOutOfBoundsException.class)
                    .isThrownBy(() -> sut.nth(-1))
                    .withMessage("Index out of range: -1");
        }

        @Test
        void throwsIfIndexNotPresent() {
            var sut = sutFromString("01");

            assertThatExceptionOfType(IndexOutOfBoundsException.class)
                    .isThrownBy(() -> sut.nth(2))
                    .withMessage("Index out of range: 2");

            assertThatExceptionOfType(IndexOutOfBoundsException.class)
                    .isThrownBy(() -> sut.drop(1).nth(1))
                    .withMessage("Index out of range: 1");
        }
    }

    @Nested
    class Take {

        @Test
        void returnsNilWithNegativeItems() {
            assertThat(sutFromString("foo").take(-1)).isEqualTo(Nil.empty());
            assertThat(sutFromString("xfoo").rest().take(-1)).isEqualTo(Nil.empty());
        }

        @Test
        void returnsNilWithZeroItems() {
            assertThat(sutFromString("foo").take(0)).isEqualTo(Nil.empty());
            assertThat(sutFromString("xfoo").rest().take(0)).isEqualTo(Nil.empty());
        }

        @Test
        void returnsStringSeqWithMoreThanZeroItems() {
            assertThat(sutFromString("foobar").take(3)).containsExactly('f', 'o', 'o');
            assertThat(sutFromString("xfoobar").rest().take(3)).containsExactly('f', 'o', 'o');
        }

        @Test
        void returnsEntireSeqWhenTakingMoreThanPresent() {
            assertThat(sutFromString("foobar").take(7))
                    .isExactlyInstanceOf(StringSeq.class)
                    .containsExactly('f', 'o', 'o', 'b', 'a', 'r');
            assertThat(sutFromString("xfoobar").rest().take(7))
                    .isExactlyInstanceOf(StringSeq.class)
                    .containsExactly('f', 'o', 'o', 'b', 'a', 'r');
        }
    }

    @Nested
    class Drop {

        @Test
        void returnsUnchangedSeqWithNegativeItemsToDrop() {
            assertThat(sutFromString("foo").drop(-1)).containsExactly('f', 'o', 'o');
            assertThat(sutFromString("xfoo").rest().drop(-1)).containsExactly('f', 'o', 'o');
        }

        @Test
        void returnsUnchangedSeqWithZeroItemsToDrop() {
            assertThat(sutFromString("foo").drop(0)).containsExactly('f', 'o', 'o');
            assertThat(sutFromString("xfoo").rest().drop(0)).containsExactly('f', 'o', 'o');
        }

        @Test
        void returnsSeqOfAllButTheFirstNItems() {
            assertThat(sutFromString("hello").drop(2)).containsExactly('l', 'l', 'o');
            assertThat(sutFromString("xhello").rest().drop(2)).containsExactly('l', 'l', 'o');
        }

        @Test
        void returnsEmptySeqIfMoreItemsAreDroppedThanPresent() {
            assertThat(sutFromString("foo").drop(5)).isEmpty();
            assertThat(sutFromString("xfoo").rest().drop(5)).isEmpty();
        }
    }

    @Nested
    class TakeWhile {

        @Test
        void returnsEmptySeqWhenFirstItemDoesNotMatch() {
            assertThat(sutFromString("foo").takeWhile(x -> x != 'f')).isEmpty();
            assertThat(sutFromString("xfoo").rest().takeWhile(x -> x != 'f')).isEmpty();
        }

        @Test
        void returnsSeqWithSingleMatchingItem() {
            assertThat(sutFromString("foo").takeWhile(x -> x == 'f')).containsExactly('f');
            assertThat(sutFromString("xfoo").rest().takeWhile(x -> x == 'f')).containsExactly('f');
        }

        @Test
        void returnsSeqWithMatchingItems() {
            assertThat(sutFromString("foobar").takeWhile(x -> x > 'a')).containsExactly('f', 'o', 'o', 'b');
            assertThat(sutFromString("xfoobar").rest().takeWhile(x -> x > 'a')).containsExactly('f', 'o', 'o', 'b');
        }

        @Test
        void returnsSeqWithAllMatchingItems() {
            assertThat(sutFromString("foo").takeWhile(x -> x > 'a')).containsExactly('f', 'o', 'o');
            assertThat(sutFromString("xfoo").rest().takeWhile(x -> x > 'a')).containsExactly('f', 'o', 'o');
        }
    }

    @Nested
    class DropWhile {

        @Test
        void returnsEmptySeqWhenAllItemsMatch() {
            assertThat(sutFromString("foo").dropWhile(x -> x > 'a')).isEmpty();
            assertThat(sutFromString("xfoo").rest().dropWhile(x -> x > 'a')).isEmpty();
        }

        @Test
        void returnsSeqWithItemsThatDoNotMatch() {
            assertThat(sutFromString("foo").dropWhile(x -> x < 'o')).containsExactly('o', 'o');
            assertThat(sutFromString("xfoo").rest().dropWhile(x -> x < 'o')).containsExactly('o', 'o');
        }

        @Test
        void returnsEntireSeqWhenFirstItemDoesNotMatch() {
            assertThat(sutFromString("foo").dropWhile(x -> x > 'f')).containsExactly('f', 'o', 'o');
            assertThat(sutFromString("xfoo").rest().dropWhile(x -> x > 'f')).containsExactly('f', 'o', 'o');
        }
    }

    @Nested
    class Partition {

        @Test
        void returnsEmptySeqForNegativeSizeN() {
            var sut = sutFromString("foo");

            assertThat(sut.partition(-1)).isEmpty();
            assertThat(sut.partition(-1, 10)).isEmpty();
            assertThat(sut.partition(-1, 3)).isEmpty();
            assertThat(sut.partition(-1, 1)).isEmpty();
            assertThat(sut.partition(-1, 0)).isEmpty();
            assertThat(sut.partition(-1, -1)).isEmpty();
        }

        @Test
        void returnsInfiniteSeqOfEmptyListsForZeroSizeN() {
            var sut = sutFromString("foo");

            assertThat(sut.partition(0).take(2)).containsExactly(
                    ISeq.of(),
                    ISeq.of());

            assertThat(sut.partition(0, 1).take(2)).containsExactly(
                    ISeq.of(),
                    ISeq.of());

            assertThat(sut.partition(0, 0).take(2)).containsExactly(
                    ISeq.of(),
                    ISeq.of());

            assertThat(sut.partition(0, -1).take(2)).containsExactly(
                    ISeq.of(),
                    ISeq.of());
        }

        @Test
        void returnsSeqOfListsOfOneItemEachAtOffsetsStepApart() {
            var sut = sutFromString("01234");

            assertThat(sut.partition(1).take(3)).containsExactly(
                    ISeq.of('0'),
                    ISeq.of('1'),
                    ISeq.of('2'));

            assertThat(sut.partition(1, 1).take(3)).containsExactly(
                    ISeq.of('0'),
                    ISeq.of('1'),
                    ISeq.of('2'));

            assertThat(sut.partition(1, 2).take(3)).containsExactly(
                    ISeq.of('0'),
                    ISeq.of('2'),
                    ISeq.of('4'));
        }

        @Test
        void returnsSeqOfListsOfNItemsEachAtOffsetsStepApart() {
            var sut = sutFromString("abcdefghijklmnop");

            assertThat(sut.partition(3).take(3)).containsExactly(
                    ISeq.of('a', 'b', 'c'),
                    ISeq.of('d', 'e', 'f'),
                    ISeq.of('g', 'h', 'i'));

            assertThat(sut.partition(3, 3).take(3)).containsExactly(
                    ISeq.of('a', 'b', 'c'),
                    ISeq.of('d', 'e', 'f'),
                    ISeq.of('g', 'h', 'i'));

            assertThat(sut.partition(4, 6)).containsExactly(
                    ISeq.of('a', 'b', 'c', 'd'),
                    ISeq.of('g', 'h', 'i', 'j'),
                    ISeq.of('m', 'n', 'o', 'p'));
        }

        @Test
        void dropsItemsThatDoNotMakeACompleteLastPartition() {
            var sut = sutFromString("abcdefghijklmn");

            assertThat(sut.partition(4)).containsExactly(
                    ISeq.of('a', 'b', 'c', 'd'),
                    ISeq.of('e', 'f', 'g', 'h'),
                    ISeq.of('i', 'j', 'k', 'l'));

            assertThat(sut.partition(4, 4)).containsExactly(
                    ISeq.of('a', 'b', 'c', 'd'),
                    ISeq.of('e', 'f', 'g', 'h'),
                    ISeq.of('i', 'j', 'k', 'l'));

            assertThat(sut.partition(3, 4)).containsExactly(
                    ISeq.of('a', 'b', 'c'),
                    ISeq.of('e', 'f', 'g'),
                    ISeq.of('i', 'j', 'k'));
        }

        @Test
        void returnsSeqOfOneEmptyListForStepGreaterThanOrEqualToSizeN() {
            var sut = sutFromString("foo");

            assertThat(sut.partition(0, 3)).containsExactly(
                    ISeq.of());

            assertThat(sut.partition(0, 4)).containsExactly(
                    ISeq.of());
        }

        @Test
        void returnsASlidingWindowIfStepIsLowerThanSizeN() {
            var sut = sutFromString("abcde");

            assertThat(sut.partition(3, 1)).containsExactly(
                    ISeq.of('a', 'b', 'c'),
                    ISeq.of('b', 'c', 'd'),
                    ISeq.of('c', 'd', 'e'));
        }

        @Nested
        class WhenPadIsProvidedAndNotNull {

            @Test
            void fillsIncompleteLastPartitionWithItemsFromPad() {
                var sut = sutFromString("abcdefghijklmn");

                assertThat(sut.partition(4, 4, List.of('0', '1', '2', '3'))).containsExactly(
                        ISeq.of('a', 'b', 'c', 'd'),
                        ISeq.of('e', 'f', 'g', 'h'),
                        ISeq.of('i', 'j', 'k', 'l'),
                        ISeq.of('m', 'n', '0', '1'));

                assertThat(sut.partition(3, 4, List.of('0', '1', '2', '3'))).containsExactly(
                        ISeq.of('a', 'b', 'c'),
                        ISeq.of('e', 'f', 'g'),
                        ISeq.of('i', 'j', 'k'),
                        ISeq.of('m', 'n', '0'));
            }

            @Test
            void returnsAnIncompleteLastPartitionIfItemsInPadAreFewerThanRequired() {
                var sut = sutFromString("abcdefghijklmn");

                assertThat(sut.partition(4, 4, List.of())).containsExactly(
                        ISeq.of('a', 'b', 'c', 'd'),
                        ISeq.of('e', 'f', 'g', 'h'),
                        ISeq.of('i', 'j', 'k', 'l'),
                        ISeq.of('m', 'n'));

                assertThat(sut.partition(3, 4, List.of())).containsExactly(
                        ISeq.of('a', 'b', 'c'),
                        ISeq.of('e', 'f', 'g'),
                        ISeq.of('i', 'j', 'k'),
                        ISeq.of('m', 'n'));
            }
        }
    }

    @Nested
    class PartitionAll {

        @Test
        void returnsEmptySeqForNegativeSizeN() {
            var sut = sutFromString("foo");

            assertThat(sut.partitionAll(-1)).isEmpty();
            assertThat(sut.partitionAll(-1, 10)).isEmpty();
            assertThat(sut.partitionAll(-1, 3)).isEmpty();
            assertThat(sut.partitionAll(-1, 1)).isEmpty();
            assertThat(sut.partitionAll(-1, 0)).isEmpty();
            assertThat(sut.partitionAll(-1, -1)).isEmpty();
        }

        @Test
        void returnsInfiniteSeqOfEmptyListsForZeroSizeN() {
            var sut = sutFromString("foo");

            assertThat(sut.partitionAll(0).take(2)).containsExactly(
                    ISeq.of(),
                    ISeq.of());

            assertThat(sut.partitionAll(0, 1).take(2)).containsExactly(
                    ISeq.of(),
                    ISeq.of());

            assertThat(sut.partitionAll(0, 0).take(2)).containsExactly(
                    ISeq.of(),
                    ISeq.of());

            assertThat(sut.partitionAll(0, -1).take(2)).containsExactly(
                    ISeq.of(),
                    ISeq.of());
        }

        @Test
        void returnsSeqOfListsOf1ItemEachAtOffsetsStepApart() {
            var sut = sutFromString("01234");

            assertThat(sut.partitionAll(1).take(3)).containsExactly(
                    ISeq.of('0'),
                    ISeq.of('1'),
                    ISeq.of('2'));

            assertThat(sut.partitionAll(1, 1).take(3)).containsExactly(
                    ISeq.of('0'),
                    ISeq.of('1'),
                    ISeq.of('2'));

            assertThat(sut.partitionAll(1, 2).take(3)).containsExactly(
                    ISeq.of('0'),
                    ISeq.of('2'),
                    ISeq.of('4'));
        }

        @Test
        void returnsSeqOfListsOfNItemsEachAtOffsetsStepApart() {
            var sut = sutFromString("abcdefghijklmnop");

            assertThat(sut.partitionAll(3).take(3)).containsExactly(
                    ISeq.of('a', 'b', 'c'),
                    ISeq.of('d', 'e', 'f'),
                    ISeq.of('g', 'h', 'i'));

            assertThat(sut.partitionAll(3, 3).take(3)).containsExactly(
                    ISeq.of('a', 'b', 'c'),
                    ISeq.of('d', 'e', 'f'),
                    ISeq.of('g', 'h', 'i'));

            assertThat(sut.partitionAll(4, 6)).containsExactly(
                    ISeq.of('a', 'b', 'c', 'd'),
                    ISeq.of('g', 'h', 'i', 'j'),
                    ISeq.of('m', 'n', 'o', 'p'));
        }

        @Test
        void returnsSeqOfOneEmptyListForStepGreaterThanOrEqualToSizeN() {
            var sut = sutFromString("foo");

            assertThat(sut.partitionAll(0, 3)).containsExactly(ISeq.of());
            assertThat(sut.partitionAll(0, 4)).containsExactly(ISeq.of());
        }

        @Test
        void returnsASlidingWindowIfStepIsLowerThanSizeN() {
            var sut = sutFromString("abcde");

            assertThat(sut.partitionAll(3, 1)).containsExactly(
                    ISeq.of('a', 'b', 'c'),
                    ISeq.of('b', 'c', 'd'),
                    ISeq.of('c', 'd', 'e'),
                    ISeq.of('d', 'e'),
                    ISeq.of('e'));
        }

        @Test
        void returnsAnIncompleteLastPartition() {
            var sut = sutFromString("abcdefghijklmn");

            assertThat(sut.partitionAll(4, 4)).containsExactly(
                    ISeq.of('a', 'b', 'c', 'd'),
                    ISeq.of('e', 'f', 'g', 'h'),
                    ISeq.of('i', 'j', 'k', 'l'),
                    ISeq.of('m', 'n'));

            assertThat(sut.partitionAll(3, 4)).containsExactly(
                    ISeq.of('a', 'b', 'c'),
                    ISeq.of('e', 'f', 'g'),
                    ISeq.of('i', 'j', 'k'),
                    ISeq.of('m', 'n'));
        }
    }

    @Nested
    class Reductions {

        @Test
        void returnsASeqWithTheIntermediateValuesOfTheReduction() {
            var sut = sutFromString("foo");

            assertThat(sut.reductions((a, b) -> b)).containsExactly('f', 'o', 'o');
        }

        @Test
        void returnsASeqWithTheIntermediateValuesOfTheReductionStartingWithInit() {
            var sut = sutFromString("foo");

            assertThat(sut.reductions("", (a, b) -> a + b).take(4)).containsExactly("", "f", "fo", "foo");
        }
    }

    @Nested
    class Reduce {

        @Test
        void returnsOptionalResultWhenValIsNotSupplied() {
            var sut = sutFromString("foo");

            assertThat(sut.reduce((a, b) -> b)).hasValue('o');
            assertThat(sut.drop(2).reduce((a, b) -> b)).hasValue('o');
        }

        @Test
        void returnsResultWhenValIsSupplied() {
            var sut = sutFromString("foo");

            assertThat(sut.reduce("", (a, b) -> a + b)).isEqualTo("foo");
        }
    }

    @Nested
    class Reverse {

        @Test
        void returnsReversedSeq() {
            var sut = sutFromString("abcde");

            assertThat(sut.reverse()).containsExactly('e', 'd', 'c', 'b', 'a');
        }

        @Test
        void returnsReversedSubSeq() {
            var sut = sutFromString("abcde").drop(1).take(3);

            assertThat(sut.reverse()).containsExactly('d', 'c', 'b');
        }
    }

    @Nested
    class Some {

        @Test
        void returnsFalseIfNoneOfTheItemsMatchPred() {
            var sut = sutFromString("foo");

            assertThat(sut.some(x -> x > 'p')).isFalse();
        }

        @Test
        void returnsTrueIfFirstItemMatchesPred() {
            var sut = sutFromString("foo");

            assertThat(sut.some(x -> x == 'f')).isTrue();
        }

        @Test
        void returnsTrueIfAllItemsMatchPred() {
            var sut = sutFromString("foo");

            assertThat(sut.some(x -> x > 'e')).isTrue();
        }

        @Test
        void returnsTrueIfSomeItemMatchesPred() {
            var sut = sutFromString("bar");

            assertThat(sut.some(x -> x == 'a')).isTrue();
        }

        @Test
        void returnsTrueIfLastItemMatchesPred() {
            var sut = sutFromString("bar");

            assertThat(sut.some(x -> x == 'r')).isTrue();
        }
    }

    @Nested
    class Every {

        @Test
        void returnsTrueIfAllItemsInSeqMatchPred() {
            var sut = sutFromString("foo");

            assertThat(sut.every(x -> x > 'e')).isTrue();
        }

        @Test
        void returnsFalseIfFirstItemDoesNotMatchPred() {
            var sut = sutFromString("foo");

            assertThat(sut.every(x -> x > 'f')).isFalse();
        }

        @Test
        void returnsFalseIfAnyItemDoesNotMatchPred() {
            var sut = sutFromString("bar");

            assertThat(sut.every(x -> x > 'a')).isFalse();
        }

        @Test
        void returnsFalseIfLastItemDoesNotMatchPred() {
            var sut = sutFromString("bar");

            assertThat(sut.every(x -> x < 'r')).isFalse();
        }
    }

    @Nested
    class NotAny {

        @Test
        void returnsFalseIfFirstItemMatchesPred() {
            var sut = sutFromString("bar");

            assertThat(sut.notAny(x -> x == 'b')).isFalse();
        }

        @Test
        void returnsFalseIfAnyItemMatchesPred() {
            var sut = sutFromString("bar");

            assertThat(sut.notAny(x -> x == 'a')).isFalse();
        }

        @Test
        void returnsFalseIfAllItemsMatchPred() {
            var sut = sutFromString("bar");

            assertThat(sut.notAny(x -> x >= 'a')).isFalse();
        }

        @Test
        void returnsTrueIfNoItemMatchesPred() {
            var sut = sutFromString("bar");

            assertThat(sut.notAny(x -> x > 'r')).isTrue();
        }
    }

    @Nested
    class Max {

        @Test
        void returnsSingleItem() {
            assertThat(sutFromString("1").max(Comparator.naturalOrder())).hasValue('1');
        }

        @Test
        void returnsHighestValue() {
            var sut = sutFromString("bar");

            assertThat(sut.max(Comparator.naturalOrder())).hasValue('r');
        }
    }

    @Nested
    class Min {

        @Test
        void returnsSingleItem() {
            assertThat(sutFromString("1").min(Comparator.naturalOrder())).hasValue('1');
        }

        @Test
        void returnsLowestValue() {
            var sut = sutFromString("bar");

            assertThat(sut.min(Comparator.naturalOrder())).hasValue('a');
        }
    }

    @Nested
    class MaxKey {

        @Test
        void returnsSingleItem() {
            assertThat(sutFromString("1").maxKey(x -> x.toString())).hasValue('1');
        }

        @Test
        void returnsHighestValue() {
            var sut = sutFromString("bar");

            assertThat(sut.maxKey(x -> x.toString())).hasValue('r');
        }
    }

    @Nested
    class MinKey {

        @Test
        void returnsSingleItem() {
            assertThat(sutFromString("1").minKey(x -> x.toString())).hasValue('1');
        }

        @Test
        void returnsLowestValue() {
            var sut = sutFromString("bar");

            assertThat(sut.minKey(x -> x.toString())).hasValue('a');
        }
    }

    @Test
    void strReturnsStringRepresentationOfSeq() {
        assertThat(sutFromString("foobar").str()).isEqualTo("foobar");
    }

    @Nested
    class Find {

        @Test
        void returnsOptionalOfValueAtIndex() {
            var sut = sutFromString("bar");

            assertThat(sut.find(0)).hasValue('b');
            assertThat(sut.find(1)).hasValue('a');
            assertThat(sut.find(2)).hasValue('r');
        }

        @Test
        void returnsEmptyOptionalForNegativeIndex() {
            assertThat(sutFromString("bar").find(-1)).isEmpty();
        }

        @Test
        void returnsEmptyOptionalIfIndexNotPresent() {
            assertThat(sutFromString("bar").find(3)).isEmpty();
        }
    }

    @Nested
    class FindFirst {

        @Test
        void returnsOptionalOfHead() {
            var sut = sutFromString("bar");

            assertThat(sut.findFirst()).hasValue('b');
        }

        @Test
        void returnsEmptyOptionalWhenNoItemsMatchPred() {
            var sut = sutFromString("bar");

            assertThat(sut.findFirst(x -> x > 'r')).isEmpty();
        }

        @Test
        void returnsOptionalOfFirstMatchingItem() {
            var sut = sutFromString("bar");

            assertThat(sut.findFirst(x -> x == 'a')).hasValue('a');
        }
    }

    @Test
    void runCallsProcForEveryItemPresent() {
        var proc = Mockito.<Consumer<Character>>mock();

        var sut = sutFromString("012");

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
            var sut = sutFromString("0123");

            assertThat(sut.get(0)).isEqualTo('0');
            assertThat(sut.get(1)).isEqualTo('1');
            assertThat(sut.get(2)).isEqualTo('2');
            assertThat(sut.get(3)).isEqualTo('3');
        }

        @Test
        void throwsForNegativeIndex() {
            assertThatExceptionOfType(IndexOutOfBoundsException.class)
                    .isThrownBy(() -> sutFromString("0123").get(-1))
                    .withMessage("Index out of range: -1");
        }

        @Test
        void throwsIfIndexNotPresent() {
            assertThatExceptionOfType(IndexOutOfBoundsException.class)
                    .isThrownBy(() -> sutFromString("0").get(1))
                    .withMessage("Index out of range: 1");
        }
    }

    @Test
    void isEmptyReturnsFalse() {
        assertThat(sutFromString("foo").isEmpty()).isFalse();
    }

    @Test
    void countReturnsStringLength() {
        assertThat(sutFromString("foo").count()).isEqualTo(3);
        assertThat(sutFromString("xfoo").rest().count()).isEqualTo(3);
    }

    @Test
    void reifyReturnsList() {
        var sut = sutFromString("bar");

        assertThat(sut.reify())
                .isInstanceOf(List.class)
                .containsExactly('b', 'a', 'r');
    }

    @Test
    void forEachCallsConsumerForEveryItemPresent() {
        var consumer = Mockito.<Consumer<Character>>mock();

        var sut = sutFromString("x012").rest();

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
            var sut = sutFromString("xbar").rest();

            var actual = sut.iterator();

            assertThat(actual.hasNext()).isTrue();
            assertThat(actual.next()).isEqualTo('b');
            assertThat(actual.hasNext()).isTrue();
            assertThat(actual.next()).isEqualTo('a');
            assertThat(actual.hasNext()).isTrue();
            assertThat(actual.next()).isEqualTo('r');
            assertThat(actual.hasNext()).isFalse();
        }
    }

    @Test
    void streamReturnsStream() {
        var sut = sutFromString("bar");

        assertThat(sut.stream()).containsExactly('b', 'a', 'r');
    }

    @Test
    void parallelStreamReturnsStream() {
        var sut = sutFromString("bar");

        assertThat(sut.parallelStream()).containsExactly('b', 'a', 'r');
    }

    @Nested
    class ToMap {

        @Test
        void throwsWithoutKeyAndValueMapper() {
            var sut = sutFromString("bar");

            assertThatExceptionOfType(UnsupportedOperationException.class)
                    .isThrownBy(() -> sut.toMap())
                    .withMessage("ISeq is not of type Map.Entry. Provide key- and value-mappers");
        }

        @Test
        void returnsMapBasedOnKeyAndValueMapper() {
            var sut = sutFromString("xbar").rest();

            var actual = sut.toMap(k -> k, v -> v.toString());

            assertThat(actual)
                    .containsEntry('b', "b")
                    .containsEntry('a', "a")
                    .containsEntry('r', "r");
        }

        @Test
        void throwsOnCollision() {
            var sut = sutFromString("xfoo").rest();

            assertThatExceptionOfType(IllegalArgumentException.class)
                    .isThrownBy(() -> sut.toMap(k -> k, v -> v.toString()))
                    .withMessage("duplicate key: o");
        }

        @Test
        void returnsMapBasedOnKeyAndValueMapperWithApplyingMergerOnCollision() {
            var sut = sutFromString("xfoo").rest();

            var actual = sut.toMap(k -> k.toString(), v -> List.of(v), (a, b) -> List.of(a.get(0), b.get(0)));

            assertThat(actual).hasSize(2)
                    .containsEntry("f", List.of('f'))
                    .containsEntry("o", List.of('o', 'o'));
        }
    }

    @Test
    void frequenciesReturnsAMapFromDistinctItemsToTheNumberOfTimesTheyAppear() {
        var sut = sutFromString("x0121013").rest().take(6);

        var actual = sut.frequencies();

        assertThat(actual).hasSize(3).contains(entry('0', 2), entry('1', 3), entry('2', 1));
    }

    @Test
    void toStringReturnsAllItemsInSeq() {
        var sut = sutFromString("xbar").rest();

        assertThat(sut).hasToString("[b, a, r]");
    }
}