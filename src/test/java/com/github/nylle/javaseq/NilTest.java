package com.github.nylle.javaseq;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.verifyNoInteractions;

public class NilTest {

    @Test
    void firstReturnsNull() {
        assertThat(Nil.empty().first()).isNull();
    }

    @Test
    void restReturnsNil() {
        assertThat(Nil.empty().rest()).isEqualTo(Nil.empty());
    }

    @Test
    void sizeReturnsZero() {
        assertThat(Nil.empty().size()).isZero();
    }

    @Test
    void isEmptyReturnsTrue() {
        assertThat(Nil.empty().isEmpty()).isTrue();
    }

    @Test
    void getThrows() {
        assertThatExceptionOfType(IndexOutOfBoundsException.class)
                .isThrownBy(() -> Nil.empty().get(0))
                .withMessage("Index out of range: 0");
    }

    @Nested
    class Nth {

        @Test
        void throwsIndexOutOfBoundsException() {
            assertThatExceptionOfType(IndexOutOfBoundsException.class)
                    .isThrownBy(() -> Nil.empty().nth(0))
                    .withMessage("Index out of range: 0");
        }

        @Test
        void returnsDefault() {
            assertThat(Nil.empty().nth(0, "x")).isEqualTo("x");
        }
    }

    @Nested
    class Take {

        @Test
        void returnsNilWithNegativeItems() {
            var sut = Nil.empty();

            assertThat(sut.take(-1)).isEqualTo(Nil.empty());
        }

        @Test
        void returnsNilWithZeroItems() {
            var sut = Nil.empty();

            assertThat(sut.take(0)).isEqualTo(Nil.empty());
        }

        @Test
        void returnsNilForMoreThanZeroItems() {
            var sut = Nil.empty();

            assertThat(sut.take(3)).isEqualTo(Nil.empty());
        }
    }

    @Nested
    class Drop {

        @Test
        void returnsNilWithNegativeItems() {
            assertThat(Nil.empty().drop(-1)).isEqualTo(Nil.empty());
        }

        @Test
        void returnsNilWithZeroItems() {
            assertThat(Nil.empty().drop(0)).isEqualTo(Nil.empty());
        }

        @Test
        void returnsNilForMoreThanZeroItems() {
            assertThat(Nil.empty().drop(12)).isEqualTo(Nil.empty());
        }
    }

    @Test
    void filterReturnsNil() {
        assertThat(Nil.<Integer>empty().filter(x -> x != null)).isEqualTo(Nil.empty());
    }

    @Nested
    class Map {

        @Test
        void returnsNil() {
            assertThat(Nil.<Integer>empty().map(x -> x * 100)).isEqualTo(Nil.empty());
        }

        @Test
        void returnsNilWhenMappingWithOtherColl() {
            assertThat(Nil.<Integer>empty().map(Nil.<Integer>empty(), (a, b) -> a + b)).isEqualTo(Nil.empty());
            assertThat(Nil.<Integer>empty().map(ISeq.of(1, 2), (a, b) -> a + b)).isEqualTo(Nil.empty());
            assertThat(Nil.<Integer>empty().map(Stream.of(1, 2), (a, b) -> a + b)).isEqualTo(Nil.empty());
            assertThat(Nil.<Integer>empty().map(ISeq.of(1, 2).iterator(), (a, b) -> a + b)).isEqualTo(Nil.empty());
            assertThat(Nil.<Integer>empty().map(List.of(1, 2), (a, b) -> a + b)).isEqualTo(Nil.empty());
            assertThat(Nil.<Integer>empty().map(new Integer[]{1, 2}, (a, b) -> a + b)).isEqualTo(Nil.empty());
            assertThat(Nil.<Integer>empty().map("", (a, b) -> a + b)).isEqualTo(Nil.empty());
        }
    }

    @Nested
    class MapCat {

        @Test
        void returnsNil() {
            assertThat(Nil.<Integer>empty().mapcat(x -> List.of(x, x))).isEqualTo(Nil.empty());
        }

        @Test
        void returnsNilWhenMappingWithOtherColl() {
            assertThat(Nil.<Integer>empty().mapcat(Nil.<Integer>empty(), (a, b) -> List.of(a, b))).isEqualTo(Nil.empty());
            assertThat(Nil.<Integer>empty().mapcat(List.of(1, 2), (a, b) -> List.of(a, b))).isEqualTo(Nil.empty());
        }
    }

    @Test
    void takeWhileReturnsNil() {
        assertThat(Nil.empty().takeWhile(x -> true)).isEqualTo(Nil.empty());
    }

    @Test
    void dropWhileReturnsNil() {
        assertThat(Nil.empty().dropWhile(x -> true)).isEqualTo(Nil.empty());
    }

    @Nested
    class Partition {

        @Test
        void returnsNilForZeroSizeN() {
            assertThat(Nil.empty().partition(0)).isEqualTo(Nil.empty());
            assertThat(Nil.empty().partition(0, 2)).isEqualTo(Nil.empty());
            assertThat(Nil.empty().partition(0, 2, List.of(1))).isEqualTo(Nil.empty());
        }

        @Test
        void returnsNilForNegativeSizeN() {
            assertThat(Nil.empty().partition(-1)).isEqualTo(Nil.empty());
            assertThat(Nil.empty().partition(-1, 2)).isEqualTo(Nil.empty());
            assertThat(Nil.empty().partition(-1, 2, List.of(1))).isEqualTo(Nil.empty());
        }

        @Test
        void returnsNil() {
            assertThat(Nil.empty().partition(3)).isEqualTo(Nil.empty());
            assertThat(Nil.empty().partition(3, 2)).isEqualTo(Nil.empty());
            assertThat(Nil.empty().partition(3, 3, List.of(1, 2, 3))).isEqualTo(Nil.empty());
        }
    }

    @Nested
    class PartitionAll {

        @Test
        void returnsNilForZeroSizeN() {
            assertThat(Nil.empty().partitionAll(0)).isEqualTo(Nil.empty());
            assertThat(Nil.empty().partitionAll(0, 2)).isEqualTo(Nil.empty());
        }

        @Test
        void returnsNilForNegativeSizeN() {
            assertThat(Nil.empty().partitionAll(-1)).isEqualTo(Nil.empty());
            assertThat(Nil.empty().partitionAll(-1, 2)).isEqualTo(Nil.empty());
        }

        @Test
        void returnsNil() {
            assertThat(Nil.empty().partitionAll(3)).isEqualTo(Nil.empty());
            assertThat(Nil.empty().partitionAll(3, 2)).isEqualTo(Nil.empty());
        }
    }

    @Nested
    class Reductions {

        @Test
        void returnsNil() {
            assertThat(Nil.<Integer>empty().reductions((a, b) -> a + b)).isEqualTo(Nil.empty());
        }

        @Test
        void returnsSeqOfInit() {
            assertThat(Nil.<String>empty().reductions("a", (a, b) -> a + b)).containsExactly("a");
        }
    }

    @Test
    void consReturnsNewSeqWithItemPrepended() {
        var actual = Nil.<String>empty().cons("x");

        assertThat(actual.first()).isEqualTo("x");
        assertThat(actual.rest()).isEmpty();
    }

    @Nested
    class Reduce {

        @Test
        void returnsEmptyOptional() {
            assertThat(Nil.<Integer>empty().reduce((a, b) -> a + b)).isEmpty();
        }

        @Test
        void returnsVal() {
            assertThat(Nil.<Integer>empty().reduce(0, (a, b) -> a + b)).isEqualTo(0);
        }
    }

    @Test
    void distinctReturnsNil() {
        assertThat(Nil.empty().distinct()).isEqualTo(Nil.empty());
    }

    @Test
    void sortedReturnsNil() {
        assertThat(Nil.empty().sorted()).isEqualTo(Nil.empty());
        assertThat(Nil.<Integer>empty().sorted(Comparator.naturalOrder())).isEqualTo(Nil.empty());
    }

    @Test
    void someReturnsFalse() {
        assertThat(Nil.<Integer>empty().some(x -> true)).isFalse();
    }

    @Test
    void everyReturnsTrue() {
        assertThat(Nil.<Integer>empty().every(x -> false)).isTrue();
    }

    @Test
    void notAnyReturnsTrue() {
        assertThat(Nil.<Integer>empty().every(x -> false)).isTrue();
    }

    @Test
    void isRealizedReturnsFalse() {
        assertThat(Nil.empty().isRealized()).isFalse();
    }

    @Test
    void maxReturnsEmptyOptional() {
        assertThat(Nil.<Integer>empty().max(Comparator.naturalOrder())).isEmpty();
    }

    @Test
    void minReturnsEmptyOptional() {
        assertThat(Nil.<Integer>empty().min(Comparator.naturalOrder())).isEmpty();
    }

    @Test
    void maxKeyReturnsEmptyOptional() {
        assertThat(Nil.<Integer>empty().maxKey(x -> Math.abs(x))).isEmpty();
    }

    @Test
    void minKeyReturnsEmptyOptional() {
        assertThat(Nil.<Integer>empty().minKey(x -> Math.abs(x))).isEmpty();
    }

    @Test
    void findReturnsEmptyOptional() {
        assertThat(Nil.empty().find(-1)).isEmpty();
        assertThat(Nil.empty().find(0)).isEmpty();
        assertThat(Nil.empty().find(1)).isEmpty();
    }

    @Test
    void findFirstReturnsEmptyOptional() {
        assertThat(Nil.empty().findFirst()).isEmpty();
        assertThat(Nil.empty().findFirst(x -> true)).isEmpty();
    }

    @Test
    void forEachDoesNothing() {
        var consumer = Mockito.<Consumer<Integer>>mock();

        Nil.<Integer>empty().forEach(consumer);

        verifyNoInteractions(consumer);
    }

    @Test
    void runDoesNothing() {
        var proc = Mockito.<Consumer<Integer>>mock();

        Nil.<Integer>empty().run(proc);

        verifyNoInteractions(proc);
    }

    @Test
    void iteratorReturnsEmptyIterator() {
        assertThat(Nil.empty().iterator().hasNext()).isFalse();
    }

    @Test
    void streamReturnsEmptyStream() {
        assertThat(Nil.empty().stream()).isEmpty();
    }

    @Test
    void parallelStreamReturnsEmptyStream() {
        assertThat(Nil.empty().parallelStream()).isEmpty();
    }

    @Nested
    class ToMap {

        @Test
        void throwsIfSeqIsNotOfTypeEntry() {
            assertThatExceptionOfType(UnsupportedOperationException.class)
                    .isThrownBy(() -> Nil.empty().toMap())
                    .withMessage("ISeq is not of type Map.Entry. Provide key- and value-mappers");
        }

        @Test
        void toMapReturnsEmptyMap() {
            assertThat(Nil.empty().toMap(k -> k, v -> v)).isEmpty();
            assertThat(Nil.empty().toMap(k -> k, v -> v, (a, b) -> a)).isEmpty();
        }
    }

    @Test
    void toListReturnsEmptyList() {
        assertThat(Nil.empty().toList())
                .isInstanceOf(List.class)
                .isEmpty();
    }

    @Test
    void toStringReturnsEmptyBrackets() {
        assertThat(Nil.empty()).hasToString("[]");
    }
}
