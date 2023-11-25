package com.github.nylle.javaseq;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

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
}