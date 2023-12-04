package com.github.nylle.javaseq;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

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
        class WithOtherSeq {

            @Test
            void returnsEmptySeqWhenProvidingEmptyOther() {
                assertThat(ISeq.sequence("foo").map(ISeq.<Integer>of(), (a, b) -> a + b)).isEmpty();
            }

            @Test
            void returnsANewSeqWithTheItemsOfBothInitialSeqsAreCombinedUsingF() {
                var sut = ISeq.sequence("123");

                assertThat(sut.map(ISeq.sequence("abc"), (a, b) -> "" + a + b)).containsExactly("1a", "2b", "3c");
                assertThat(sut.map(ISeq.of("a", "b", "c"), (a, b) -> a + b)).containsExactly("1a", "2b", "3c");
            }

            @Test
            void ignoresRemainingItemsIfOneOfTheSeqsIsExhausted() {
                var sut = ISeq.sequence("123");

                assertThat(sut.map(ISeq.sequence("ab"), (a, b) -> "" + a + b)).containsExactly("1a", "2b");
                assertThat(sut.map(ISeq.sequence("abcd"), (a, b) -> "" + a + b)).containsExactly("1a", "2b", "3c");

                assertThat(sut.map(ISeq.of("a", "b"), (a, b) -> a + b)).containsExactly("1a", "2b");
                assertThat(sut.map(ISeq.of("a", "b", "c", "d"), (a, b) -> a + b)).containsExactly("1a", "2b", "3c");
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
    }

    @Test
    void consPrependsCharacter() {
        var sut = ISeq.sequence("oo");

        assertThat(sut.cons('f')).containsExactly('f', 'o', 'o');
    }

    @Test
    void isEmptyReturnsFalse() {
        assertThat(ISeq.sequence("foo").isEmpty()).isFalse();
    }
}