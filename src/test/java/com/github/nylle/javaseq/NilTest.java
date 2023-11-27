package com.github.nylle.javaseq;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NilTest {

    @Test
    void firstReturnsNull() {
        assertThat(Nil.of().first()).isNull();
    }

    @Test
    void restReturnsNil() {
        assertThat(Nil.of().rest()).isEqualTo(Nil.of());
    }

    @Test
    void sizeReturnsZero() {
        assertThat(Nil.of().size()).isZero();
    }

    @Test
    void isEmptyReturnsTrue() {
        assertThat(Nil.of().isEmpty()).isTrue();
    }

    @Test
    void getReturnsNull() {
        assertThat(Nil.of().get(0)).isNull();
    }

    @Nested
    class Take {

        @Test
        void returnsNilWithNegativeItems() {
            var sut = Nil.of();

            assertThat(sut.take(-1)).isEqualTo(Nil.of());
        }

        @Test
        void returnsNilWithZeroItems() {
            var sut = Nil.of();

            assertThat(sut.take(0)).isEqualTo(Nil.of());
        }

        @Test
        void returnsNilForMoreThanZeroItems() {
            var sut = Nil.of();

            assertThat(sut.take(3)).isEqualTo(Nil.of());
        }
    }

    @Nested
    class Drop {

        @Test
        void returnsNilWithNegativeItems() {
            assertThat(Nil.of().drop(-1)).isEqualTo(Nil.of());
        }

        @Test
        void returnsNilWithZeroItems() {
            assertThat(Nil.of().drop(0)).isEqualTo(Nil.of());
        }

        @Test
        void returnsNilForMoreThanZeroItems() {
            assertThat(Nil.of().drop(12)).isEqualTo(Nil.of());
        }
    }

    @Test
    void filterReturnsNil() {
        assertThat(Nil.<Integer>of().filter(x -> x != null)).isEqualTo(Nil.of());
    }

    @Nested
    class Map {

        @Test
        void returnsNil() {
            assertThat(Nil.<Integer>of().map(x -> x * 100)).isEqualTo(Nil.of());
        }

        @Test
        void returnsNilWhenMappingWithOtherSeq() {
            assertThat(Nil.<Integer>of().map(Nil.<Integer>of(), (a, b) -> a + b)).isEqualTo(Nil.of());
            assertThat(Nil.<Integer>of().map(Seq.of(1, 2), (a, b) -> a + b)).isEqualTo(Nil.of());
        }
    }

    @Test
    void mapcatReturnsNil() {
        assertThat(Nil.<Integer>of().mapcat(x -> List.of(x, x))).isEqualTo(Nil.of());
    }

    @Test
    void takeWhileReturnsNil() {
        assertThat(Nil.of().takeWhile(x -> true)).isEqualTo(Nil.of());
    }

    @Test
    void dropWhileReturnsNil() {
        assertThat(Nil.of().dropWhile(x -> true)).isEqualTo(Nil.of());
    }

    @Nested
    class Partition {

        @Test
        void returnsNilForZeroSizeN() {
            assertThat(Nil.of().partition(0)).isEqualTo(Nil.of());
            assertThat(Nil.of().partition(0, 2)).isEqualTo(Nil.of());
            assertThat(Nil.of().partition(0, 2, List.of(1))).isEqualTo(Nil.of());
        }

        @Test
        void returnsNilForNegativeSizeN() {
            assertThat(Nil.of().partition(-1)).isEqualTo(Nil.of());
            assertThat(Nil.of().partition(-1, 2)).isEqualTo(Nil.of());
            assertThat(Nil.of().partition(-1, 2, List.of(1))).isEqualTo(Nil.of());
        }

        @Test
        void returnsNil() {
            assertThat(Nil.of().partition(3)).isEqualTo(Nil.of());
            assertThat(Nil.of().partition(3, 2)).isEqualTo(Nil.of());
            assertThat(Nil.of().partition(3, 3, List.of(1, 2, 3))).isEqualTo(Nil.of());
        }
    }

    @Nested
    class PartitionAll {

        @Test
        void returnsNilForZeroSizeN() {
            assertThat(Nil.of().partitionAll(0)).isEqualTo(Nil.of());
            assertThat(Nil.of().partitionAll(0, 2)).isEqualTo(Nil.of());
        }

        @Test
        void returnsNilForNegativeSizeN() {
            assertThat(Nil.of().partitionAll(-1)).isEqualTo(Nil.of());
            assertThat(Nil.of().partitionAll(-1, 2)).isEqualTo(Nil.of());
        }

        @Test
        void returnsNil() {
            assertThat(Nil.of().partitionAll(3)).isEqualTo(Nil.of());
            assertThat(Nil.of().partitionAll(3, 2)).isEqualTo(Nil.of());
        }
    }

    @Test
    void reductionsReturnsNil() {
        assertThat(Nil.<Integer>of().reductions((a, b) -> a + b)).isEqualTo(Nil.of());
    }

    @Test
    void reductionsReturnsSeqOfInit() {
        assertThat(Nil.<String>of().reductions("a", (a, b) -> a + b)).containsExactly("a");
    }

    @Test
    void reduceReturnsEmptyOptional() {
        assertThat(Nil.<Integer>of().reduce((a, b) -> a + b)).isEmpty();
    }

    @Test
    void reduceReturnsVal() {
        assertThat(Nil.<Integer>of().reduce(0, (a, b) -> a + b)).isEqualTo(0);
    }

    @Test
    void distinctReturnsNil() {
        assertThat(Nil.of().distinct()).isEqualTo(Nil.of());
    }

    @Test
    void sortedReturnsNil() {
        assertThat(Nil.of().sorted()).isEqualTo(Nil.of());
        assertThat(Nil.<Integer>of().sorted(Comparator.naturalOrder())).isEqualTo(Nil.of());
    }

    @Test
    void someReturnsFalse() {
        assertThat(Nil.<Integer>of().some(x -> true)).isFalse();
    }

    @Test
    void everyReturnsTrue() {
        assertThat(Nil.<Integer>of().every(x -> false)).isTrue();
    }

    @Test
    void notAnyReturnsTrue() {
        assertThat(Nil.<Integer>of().every(x -> false)).isTrue();
    }

    @Test
    void maxReturnsEmptyOptional() {
        assertThat(Nil.<Integer>of().max(Comparator.naturalOrder())).isEmpty();
    }

    @Test
    void toListReturnsEmptyList() {
        assertThat(Nil.of().toList())
                .isInstanceOf(List.class)
                .isEmpty();
    }
}