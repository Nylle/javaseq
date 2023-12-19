package com.github.nylle.javaseq;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Comparator;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class ArraySeqTest {

    private static <T> ISeq<T> sutFrom(T... items) {
        return new ArraySeq<>(items);
    }

    @Nested
    class Construct {

        @Test
        void withoutArgumentsThrows() {
            assertThatExceptionOfType(IllegalArgumentException.class)
                    .isThrownBy(() -> new ArraySeq<>())
                    .withMessage("items is null or empty");
        }

        @Test
        void withNullOrEmptyArrayThrows() {
            assertThatExceptionOfType(IllegalArgumentException.class)
                    .isThrownBy(() -> new ArraySeq<Integer>(null))
                    .withMessage("items is null or empty");

            assertThatExceptionOfType(IllegalArgumentException.class)
                    .isThrownBy(() -> new ArraySeq<>(new Integer[0]))
                    .withMessage("items is null or empty");
        }

        @Test
        void withIndexAndEndThrows() {
            assertThatExceptionOfType(IllegalArgumentException.class)
                    .isThrownBy(() -> new ArraySeq<Integer>(null, 0, 1))
                    .withMessage("array is null or empty");

            assertThatExceptionOfType(IllegalArgumentException.class)
                    .isThrownBy(() -> new ArraySeq<>(new Integer[0], 0, 1))
                    .withMessage("array is null or empty");

            assertThatExceptionOfType(IllegalArgumentException.class)
                    .isThrownBy(() -> new ArraySeq<>(new String[] {"1", "2", "3"}, 3, 4))
                    .withMessage("index 3 is out of range for array [1, 2, 3]");

            assertThatExceptionOfType(IllegalArgumentException.class)
                    .isThrownBy(() -> new ArraySeq<>(new String[] {"1", "2", "3"}, 2, 4))
                    .withMessage("end 4 is out of range for array [1, 2, 3]");

            assertThatExceptionOfType(IllegalArgumentException.class)
                    .isThrownBy(() -> new ArraySeq<>(new String[] {"1", "2", "3"}, 2, 2))
                    .withMessage("end 2 must be greater than index 2");
        }
    }

    @Test
    void firstReturnsFirstItem() {
        assertThat(sutFrom("1", "2", "3", "4").first()).isEqualTo("1");
        assertThat(sutFrom("1", "2", "3", "4").rest().first()).isEqualTo("2");
    }

    @Nested
    class Second {

        @Test
        void returnsSecondItem() {
            var sut = sutFrom("1", "2", "3", "4");

            assertThat(sut.second()).isEqualTo("2");
            assertThat(sut.rest().second()).isEqualTo("3");
        }

        @Test
        void returnsNullIfSeqHasOnlyOneElement() {
            var sut = sutFrom("1");

            assertThat(sut.second()).isNull();
        }
    }

    @Test
    void restReturnsSeqOfAllItemsButFirst() {
        var sut = sutFrom("1", "2", "3");

        assertThat(sut.rest()).containsExactly("2", "3");
        assertThat(sut.rest().first()).isEqualTo("2");
        assertThat(sut.rest().rest().first()).isEqualTo("3");
        assertThat(sut.rest().rest().rest()).isEqualTo(Nil.empty());
    }

    @Test
    void isRealizedIsTrue() {
        assertThat(sutFrom(0, 1, 2, 3, 4, 5).isRealized()).isTrue();
    }

    @Test
    void isEmptyIsFalse() {
        assertThat(sutFrom(0).isEmpty()).isFalse();
    }

    @Nested
    class Filter {

        @Test
        void returnsNilWhenNoItemsMatch() {
            assertThat(sutFrom(0, 1, 2, 3, 4, 5).filter(x -> x < 0)).isEmpty();
            assertThat(sutFrom(0, 1, 2, 3, 4, 5).rest().filter(x -> x < 0)).isEmpty();
        }

        @Test
        void returnsMatchingItems() {
            assertThat(sutFrom(0, 1, 2, 3, 4, 5).filter(x -> x > 2)).containsExactly(3, 4, 5);
            assertThat(sutFrom(0, 1, 2, 3, 4, 5).rest().filter(x -> x > 2)).containsExactly(3, 4, 5);
        }

        @Test
        void isLazy() {
            var pred = Mockito.<Predicate<Integer>>mock();
            when(pred.test(1)).thenAnswer((arg) -> arg.getArgument(0).equals(1));

            sutFrom(1, 2, 3).filter(pred).take(1).str();

            verify(pred).test(1);
            verifyNoMoreInteractions(pred);
        }
    }

    @Nested
    class Take {

        @Test
        void returnsNilWithNegativeItems() {
            assertThat(sutFrom(0, 1, 2, 3, 4, 5).take(-1)).isEqualTo(Nil.empty());
            assertThat(sutFrom(0, 1, 2, 3, 4, 5).rest().take(-1)).isEqualTo(Nil.empty());
        }

        @Test
        void returnsNilWithZeroItems() {
            assertThat(sutFrom(0, 1, 2, 3, 4, 5).take(0)).isEqualTo(Nil.empty());
            assertThat(sutFrom(0, 1, 2, 3, 4, 5).rest().take(0)).isEqualTo(Nil.empty());
        }

        @Test
        void returnsArraySeqWithThreeItems() {
            assertThat(sutFrom(0, 1, 2, 3, 4, 5).take(3))
                    .isExactlyInstanceOf(ArraySeq.class)
                    .containsExactly(0, 1, 2);
            assertThat(sutFrom(0, 1, 2, 3, 4, 5).rest().take(3))
                    .isExactlyInstanceOf(ArraySeq.class)
                    .containsExactly(1, 2, 3);
        }

        @Test
        void returnsEntireSeqWhenTakingMoreThanPresent() {
            assertThat(sutFrom(0, 1, 2, 3, 4, 5).take(7))
                    .isExactlyInstanceOf(ArraySeq.class)
                    .containsExactly(0, 1, 2, 3, 4, 5);
            assertThat(sutFrom(0, 1, 2, 3, 4, 5).rest().take(7))
                    .isExactlyInstanceOf(ArraySeq.class)
                    .containsExactly(1, 2, 3, 4, 5);
        }
    }

    @Nested
    class Drop {

        @Test
        void returnsUnchangedSeqWithNegativeItemsToDrop() {
            assertThat(sutFrom(0, 1, 2, 3, 4, 5).drop(-1)).containsExactly(0, 1, 2, 3, 4, 5);
            assertThat(sutFrom(0, 1, 2, 3, 4, 5).rest().drop(-1)).containsExactly(1, 2, 3, 4, 5);
        }

        @Test
        void returnsUnchangedSeqWithZeroItemsToDrop() {
            assertThat(sutFrom(0, 1, 2, 3, 4, 5).drop(0))
                    .isExactlyInstanceOf(ArraySeq.class)
                    .containsExactly(0, 1, 2, 3, 4, 5);
            assertThat(sutFrom(0, 1, 2, 3, 4, 5).rest().drop(0))
                    .isExactlyInstanceOf(ArraySeq.class)
                    .containsExactly(1, 2, 3, 4, 5);
        }

        @Test
        void returnsSeqOfAllButTheFirstNItems() {
            assertThat(sutFrom(0, 1, 2, 3, 4, 5).drop(2))
                    .isExactlyInstanceOf(ArraySeq.class)
                    .containsExactly(2, 3, 4, 5);
            assertThat(sutFrom(0, 1, 2, 3, 4, 5).rest().drop(2))
                    .isExactlyInstanceOf(ArraySeq.class)
                    .containsExactly(3, 4, 5);
        }

        @Test
        void returnsEmptySeqIfMoreItemsAreDroppedThanPresent() {
            assertThat(sutFrom(0, 1, 2, 3, 4, 5).drop(7)).isEmpty();
            assertThat(sutFrom(0, 1, 2, 3, 4, 5).rest().drop(7)).isEmpty();
        }
    }

    @Nested
    class TakeWhile {

        @Test
        void returnsEmptySeqWhenFirstItemDoesNotMatch() {
            assertThat(sutFrom(0, 1, 2, 3, 4, 5).takeWhile(x -> x > 0)).isEmpty();
            assertThat(sutFrom(0, 1, 2, 3, 4, 5).rest().takeWhile(x -> x > 1)).isEmpty();
        }

        @Test
        void returnsSeqWithSingleMatchingItem() {
            assertThat(sutFrom(0, 1, 2, 3, 4, 5).takeWhile(x -> x == 0))
                    .isExactlyInstanceOf(ArraySeq.class)
                    .containsExactly(0);
            assertThat(sutFrom(0, 1, 2, 3, 4, 5).rest().takeWhile(x -> x == 1))
                    .isExactlyInstanceOf(ArraySeq.class)
                    .containsExactly(1);
        }

        @Test
        void returnsSeqWithMatchingItems() {
            assertThat(sutFrom(0, 1, 2, 3, 4, 5).takeWhile(x -> x < 4))
                    .isExactlyInstanceOf(ArraySeq.class)
                    .containsExactly(0, 1, 2, 3);
            assertThat(sutFrom(0, 1, 2, 3, 4, 5).rest().takeWhile(x -> x < 5))
                    .isExactlyInstanceOf(ArraySeq.class)
                    .containsExactly(1, 2, 3, 4);
        }

        @Test
        void returnsSeqWithAllMatchingItems() {
            assertThat(sutFrom(0, 1, 2, 3, 4, 5).takeWhile(x -> x > -1))
                    .isExactlyInstanceOf(ArraySeq.class)
                    .containsExactly(0, 1, 2, 3, 4, 5);
            assertThat(sutFrom(0, 1, 2, 3, 4, 5).rest().takeWhile(x -> x > -1))
                    .isExactlyInstanceOf(ArraySeq.class)
                    .containsExactly(1, 2, 3, 4, 5);
        }
    }

    @Nested
    class DropWhile {

        @Test
        void returnsEmptySeqWhenAllItemsMatch() {
            assertThat(sutFrom(0, 1, 2, 3, 4, 5).dropWhile(x -> x > -1)).isEmpty();
            assertThat(sutFrom(0, 1, 2, 3, 4, 5).rest().dropWhile(x -> x > -1)).isEmpty();
        }

        @Test
        void returnsSeqWithItemsThatDoNotMatch() {
            assertThat(sutFrom(0, 1, 2, 3, 4, 5).dropWhile(x -> x < 3))
                    .isExactlyInstanceOf(ArraySeq.class)
                    .containsExactly(3, 4, 5);
            assertThat(sutFrom(0, 1, 2, 3, 4, 5).rest().dropWhile(x -> x < 3))
                    .isExactlyInstanceOf(ArraySeq.class)
                    .containsExactly(3, 4, 5);
        }

        @Test
        void returnsEntireSeqWhenFirstItemDoesNotMatch() {
            assertThat(sutFrom(0, 1, 2, 3, 4, 5).dropWhile(x -> x > 0))
                    .isExactlyInstanceOf(ArraySeq.class)
                    .containsExactly(0, 1, 2, 3, 4, 5);
            assertThat(sutFrom(0, 1, 2, 3, 4, 5).rest().dropWhile(x -> x > 1))
                    .isExactlyInstanceOf(ArraySeq.class)
                    .containsExactly(1, 2, 3, 4, 5);
        }
    }

    @Nested
    class Reduce {

        @Test
        void returnsOptionalResultWhenValIsNotSupplied() {
            var sut = sutFrom(0, 1, 2, 3);

            assertThat(sut.reduce((a, b) -> a + b)).hasValue(6);
            assertThat(sut.take(0).reduce((a, b) -> a + b)).isEmpty();
            assertThat(sut.take(1).reduce((a, b) -> a + b)).hasValue(0);
            assertThat(sut.drop(2).reduce((a, b) -> a + b)).hasValue(5);
        }

        @Test
        void returnsResultWhenValIsSupplied() {
            var sut = sutFrom(0, 1, 2, 3);

            assertThat(sut.reduce("", (a, b) -> a + b)).isEqualTo("0123");
            assertThat(sut.take(0).reduce("", (a, b) -> a + b)).isEqualTo("");
        }
    }

    @Nested
    class Some {

        @Test
        void returnsFalseIfNoneOfTheItemsMatchPred() {
            var sut = sutFrom(0, 1, 2);

            assertThat(sut.some(x -> x > 3)).isFalse();
        }

        @Test
        void returnsTrueIfFirstItemMatchesPred() {
            var sut = sutFrom(0, 1, 2);

            assertThat(sut.some(x -> x == 0)).isTrue();
        }

        @Test
        void returnsTrueIfAllItemsMatchPred() {
            var sut = sutFrom(0, 1, 2);

            assertThat(sut.some(x -> x > -1)).isTrue();
        }

        @Test
        void returnsTrueIfSomeItemMatchesPred() {
            var sut = sutFrom(0, 1, 2);

            assertThat(sut.some(x -> x == 1)).isTrue();
        }

        @Test
        void returnsTrueIfLastItemMatchesPred() {
            var sut = sutFrom(0, 1, 2);

            assertThat(sut.some(x -> x == 2)).isTrue();
        }
    }

    @Nested
    class Every {

        @Test
        void returnsTrueIfAllItemsInSeqMatchPred() {
            var sut = sutFrom(0, 1, 2);

            assertThat(sut.every(x -> x > -1)).isTrue();
        }

        @Test
        void returnsFalseIfFirstItemDoesNotMatchPred() {
            var sut = sutFrom(0, 1, 2);

            assertThat(sut.every(x -> x > 0)).isFalse();
        }

        @Test
        void returnsFalseIfAnyItemDoesNotMatchPred() {
            var sut = sutFrom(0, 1, 2);

            assertThat(sut.every(x -> x < 1)).isFalse();
        }

        @Test
        void returnsFalseIfLastItemDoesNotMatchPred() {
            var sut = sutFrom(0, 1, 2);

            assertThat(sut.every(x -> x < 2)).isFalse();
        }
    }

    @Nested
    class Max {

        @Test
        void returnsSingleItem() {
            assertThat(sutFrom(0).max(Comparator.naturalOrder())).hasValue(0);
        }

        @Test
        void returnsHighestValue() {
            var sut = sutFrom(0, 1, 2);

            assertThat(sut.max(Comparator.naturalOrder())).hasValue(2);
        }
    }

    @Test
    void runCallsProcForEveryItemPresent() {
        var proc = Mockito.<Consumer<Integer>>mock();

        var sut = sutFrom(0, 1, 2);

        sut.run(proc);

        verify(proc).accept(0);
        verify(proc).accept(1);
        verify(proc).accept(2);
        verifyNoMoreInteractions(proc);
    }

    @Nested
    class Nth {

        @Test
        void returnsValueAtIndex() {
            var sut = sutFrom(0, 1, 2, 3);

            assertThat(sut.nth(0)).isEqualTo(0);
            assertThat(sut.nth(1)).isEqualTo(1);
            assertThat(sut.nth(2)).isEqualTo(2);
            assertThat(sut.nth(3)).isEqualTo(3);

            assertThat(sut.rest().nth(0)).isEqualTo(1);
            assertThat(sut.rest().nth(1)).isEqualTo(2);
            assertThat(sut.rest().nth(2)).isEqualTo(3);
        }

        @Test
        void returnsDefaultValue() {
            var sut = sutFrom(0, 1, 2, 3);

            assertThat(sut.nth(0, 99)).isEqualTo(0);
            assertThat(sut.nth(1, 99)).isEqualTo(1);
            assertThat(sut.nth(2, 99)).isEqualTo(2);
            assertThat(sut.nth(3, 99)).isEqualTo(3);
            assertThat(sut.nth(4, 99)).isEqualTo(99);

            assertThat(sut.rest().nth(0, 99)).isEqualTo(1);
            assertThat(sut.rest().nth(1, 99)).isEqualTo(2);
            assertThat(sut.rest().nth(2, 99)).isEqualTo(3);
            assertThat(sut.rest().nth(3, 99)).isEqualTo(99);
        }

        @Test
        void throwsForNegativeIndex() {
            var sut = sutFrom(0, 1, 2, 3, 4, 5);

            assertThatExceptionOfType(IndexOutOfBoundsException.class)
                    .isThrownBy(() -> sut.nth(-1))
                    .withMessage("Index out of range: -1");
        }

        @Test
        void throwsIfIndexNotPresent() {
            var sut = sutFrom(0, 1, 2, 3, 4, 5);

            assertThatExceptionOfType(IndexOutOfBoundsException.class)
                    .isThrownBy(() -> sut.nth(6))
                    .withMessage("Index out of range: 6");

            assertThatExceptionOfType(IndexOutOfBoundsException.class)
                    .isThrownBy(() -> sut.drop(1).nth(5))
                    .withMessage("Index out of range: 5");

            assertThatExceptionOfType(IndexOutOfBoundsException.class)
                    .isThrownBy(() -> sut.drop(1).take(4).nth(4))
                    .withMessage("Index out of range: 4");
        }
    }

    @Test
    void countReturnsNumberOfItemsInThisSeq() {
        assertThat(sutFrom(0, 1, 2, 3, 4, 5).count()).isEqualTo(6);
        assertThat(sutFrom(0, 1, 2, 3, 4, 5).rest().count()).isEqualTo(5);
        assertThat(sutFrom(0, 1, 2, 3, 4, 5).take(3).count()).isEqualTo(3);
    }

    @Test
    void toArrayReturnsArray() {
        assertThat(sutFrom(0, 1, 2, 3, 4, 5).toArray()).containsExactly(0, 1, 2, 3, 4, 5);
        assertThat(sutFrom(0, 1, 2, 3, 4, 5).drop(2).take(3).toArray()).containsExactly(2, 3, 4);
    }

    @Test
    void indexOfReturnsIndexOfSuppliedObjectInThisSeq() {
        var sut = sutFrom("0", "1", "2", "3");

        assertThat(sut.indexOf("1")).isEqualTo(1);
        assertThat(sut.indexOf("4")).isEqualTo(-1);
        assertThat(sut.drop(1).take(3).indexOf("1")).isEqualTo(0);
    }

    @Test
    void lastIndexOfReturnsIndexOfLastOccurrenceOfSuppliedObject() {
        var sut = sutFrom("a", "b", "c", "a", "d");

        assertThat(sut.lastIndexOf("a")).isEqualTo(3);
        assertThat(sut.lastIndexOf("e")).isEqualTo(-1);
        assertThat(sut.drop(1).take(3).lastIndexOf("a")).isEqualTo(2);
    }
}