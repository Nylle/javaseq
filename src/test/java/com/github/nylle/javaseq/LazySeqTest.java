package com.github.nylle.javaseq;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class LazySeqTest {

    private static <T> ISeq<T> iterate(T x, UnaryOperator<T> f) {
        return new LazySeq<>(() -> new Cons<>(x, iterate(f.apply(x), f)));
    }

    private static ISeq<Integer> range() {
        return iterate(0, x -> x + 1);
    }

    private static ISeq<Integer> range(int end) {
        return range(0, end);
    }

    private static ISeq<Integer> range(int start, int end) {
        return iterate(start, x -> x + 1).takeWhile(x -> x < end);
    }

    private static <T> ISeq<T> sequence(Iterator<T> iterator) {
        if (iterator.hasNext()) {
            return new LazySeq<>(() -> new Cons<>(iterator.next(), sequence(iterator)));
        }
        return ISeq.of();
    }

    @Nested
    class CanBeEmpty {

        private static <T> ISeq<T> createEmpty() {
            return new LazySeq<>(() -> Nil.empty());
        }

        @Test
        void firstReturnsNull() {
            assertThat(createEmpty().first()).isNull();
        }

        @Test
        void restReturnsNil() {
            assertThat(createEmpty().rest()).isEqualTo(Nil.empty());
        }

        @Test
        void sizeReturnsZero() {
            assertThat(createEmpty().size()).isZero();
        }

        @Test
        void isEmptyReturnsTrue() {
            assertThat(createEmpty().isEmpty()).isTrue();
        }

        @Test
        void getThrows() {
            assertThatExceptionOfType(IndexOutOfBoundsException.class)
                    .isThrownBy(() -> createEmpty().get(0))
                    .withMessage("Index out of range: 0");
        }

        @Nested
        class Nth {

            @Test
            void throwsIndexOutOfBoundsException() {
                assertThatExceptionOfType(IndexOutOfBoundsException.class)
                        .isThrownBy(() -> createEmpty().nth(0))
                        .withMessage("Index out of range: 0");
            }

            @Test
            void returnsDefault() {
                assertThat(createEmpty().nth(0, "x")).isEqualTo("x");
            }
        }

        @Nested
        class Take {

            @Test
            void returnsNilWithNegativeItems() {
                assertThat(createEmpty().take(-1)).isEqualTo(Nil.empty());
            }

            @Test
            void returnsNilWithZeroItems() {
                assertThat(createEmpty().take(0)).isEqualTo(Nil.empty());
            }

            @Test
            void returnsNilForMoreThanZeroItems() {
                assertThat(createEmpty().take(3)).isEqualTo(Nil.empty());
            }
        }

        @Nested
        class Drop {

            @Test
            void returnsNilWithNegativeItems() {
                assertThat(createEmpty().drop(-1)).isEqualTo(Nil.empty());
            }

            @Test
            void returnsNilWithZeroItems() {
                assertThat(createEmpty().drop(0)).isEqualTo(Nil.empty());
            }

            @Test
            void returnsNilForMoreThanZeroItems() {
                assertThat(createEmpty().drop(12)).isEqualTo(Nil.empty());
            }
        }

        @Test
        void filterReturnsNil() {
            assertThat(createEmpty().filter(x -> x != null)).isEqualTo(Nil.empty());
        }

        @Nested
        @DisplayName("map")
        class MapTest {

            @Test
            void returnsNil() {
                assertThat(CanBeEmpty.<Integer>createEmpty().map(x -> x * 100)).isEqualTo(Nil.empty());
            }

            @Test
            void returnsNilWhenMappingWithOtherColl() {
                assertThat(CanBeEmpty.<Integer>createEmpty().map(ISeq.<Integer>of(), (a, b) -> a + b)).isEqualTo(Nil.empty());
                assertThat(CanBeEmpty.<Integer>createEmpty().map(ISeq.of(1, 2), (a, b) -> a + b)).isEqualTo(Nil.empty());
                assertThat(CanBeEmpty.<Integer>createEmpty().map(Stream.of(1, 2), (a, b) -> a + b)).isEqualTo(Nil.empty());
                assertThat(CanBeEmpty.<Integer>createEmpty().map(ISeq.of(1, 2).iterator(), (a, b) -> a + b)).isEqualTo(Nil.empty());
                assertThat(CanBeEmpty.<Integer>createEmpty().map(List.of(1, 2), (a, b) -> a + b)).isEqualTo(Nil.empty());
                assertThat(CanBeEmpty.<Integer>createEmpty().map(new Integer[]{1, 2}, (a, b) -> a + b)).isEqualTo(Nil.empty());
                assertThat(CanBeEmpty.<String>createEmpty().map("", (a, b) -> a + b)).isEqualTo(Nil.empty());
            }
        }

        @Nested
        class MapCat {

            @Test
            void returnsNil() {
                assertThat(createEmpty().mapcat(x -> ISeq.of(x, x))).isEqualTo(Nil.empty());
            }

            @Test
            void returnsNilWhenMappingWithOtherColl() {
                assertThat(createEmpty().mapcat(Nil.<Integer>empty(), (a, b) -> List.of(a, b))).isEqualTo(Nil.empty());
                assertThat(createEmpty().mapcat(ISeq.of(1, 2), (a, b) -> List.of(a, b))).isEqualTo(Nil.empty());
            }
        }

        @Test
        void takeWhileReturnsNil() {
            assertThat(createEmpty().takeWhile(x -> true)).isEqualTo(Nil.empty());
        }

        @Test
        void dropWhileReturnsNil() {
            assertThat(createEmpty().dropWhile(x -> true)).isEqualTo(Nil.empty());
        }

        @Nested
        class Partition {

            @Test
            void returnsNilForZeroSizeN() {
                assertThat(createEmpty().partition(0)).isEqualTo(Nil.empty());
                assertThat(createEmpty().partition(0, 2)).isEqualTo(Nil.empty());
                assertThat(createEmpty().partition(0, 2, List.of(1))).isEqualTo(Nil.empty());
            }

            @Test
            void returnsNilForNegativeSizeN() {
                assertThat(createEmpty().partition(-1)).isEqualTo(Nil.empty());
                assertThat(createEmpty().partition(-1, 2)).isEqualTo(Nil.empty());
                assertThat(createEmpty().partition(-1, 2, List.of(1))).isEqualTo(Nil.empty());
            }

            @Test
            void returnsNil() {
                assertThat(createEmpty().partition(3)).isEqualTo(Nil.empty());
                assertThat(createEmpty().partition(3, 2)).isEqualTo(Nil.empty());
                assertThat(createEmpty().partition(3, 3, List.of(1, 2, 3))).isEqualTo(Nil.empty());
            }
        }

        @Nested
        class PartitionAll {

            @Test
            void returnsNilForZeroSizeN() {
                assertThat(createEmpty().partitionAll(0)).isEqualTo(Nil.empty());
                assertThat(createEmpty().partitionAll(0, 2)).isEqualTo(Nil.empty());
            }

            @Test
            void returnsNilForNegativeSizeN() {
                assertThat(createEmpty().partitionAll(-1)).isEqualTo(Nil.empty());
                assertThat(createEmpty().partitionAll(-1, 2)).isEqualTo(Nil.empty());
            }

            @Test
            void returnsNil() {
                assertThat(createEmpty().partitionAll(3)).isEqualTo(Nil.empty());
                assertThat(createEmpty().partitionAll(3, 2)).isEqualTo(Nil.empty());
            }
        }

        @Nested
        class Reductions {

            @Test
            void returnsNil() {
                assertThat(CanBeEmpty.<Integer>createEmpty().reductions((a, b) -> a + b)).isEqualTo(Nil.empty());
            }

            @Test
            void returnsSeqOfInit() {
                assertThat(CanBeEmpty.<String>createEmpty().reductions("a", (a, b) -> a + b)).containsExactly("a");
            }
        }

        @Test
        void consReturnsNewSeqWithItemPrepended() {
            var actual = CanBeEmpty.<String>createEmpty().cons("x");

            assertThat(actual.first()).isEqualTo("x");
            assertThat(actual.rest()).isEmpty();
        }

        @Nested
        class Reduce {

            @Test
            void returnsEmptyOptional() {
                assertThat(CanBeEmpty.<Integer>createEmpty().reduce((a, b) -> a + b)).isEmpty();
            }

            @Test
            void returnsVal() {
                assertThat(CanBeEmpty.<Integer>createEmpty().reduce(0, (a, b) -> a + b)).isEqualTo(0);
            }
        }

        @Test
        void distinctReturnsNil() {
            assertThat(createEmpty().distinct()).isEqualTo(Nil.empty());
        }

        @Test
        void sortedReturnsNil() {
            assertThat(createEmpty().sorted()).isEqualTo(Nil.empty());
            assertThat(CanBeEmpty.<Integer>createEmpty().sorted(Comparator.naturalOrder())).isEqualTo(Nil.empty());
        }

        @Test
        void someReturnsFalse() {
            assertThat(CanBeEmpty.<Integer>createEmpty().some(x -> true)).isFalse();
        }

        @Test
        void everyReturnsTrue() {
            assertThat(CanBeEmpty.<Integer>createEmpty().every(x -> false)).isTrue();
        }

        @Test
        void notAnyReturnsTrue() {
            assertThat(CanBeEmpty.<Integer>createEmpty().every(x -> false)).isTrue();
        }

        @Test
        void isRealizedReturnsFalse() {
            assertThat(createEmpty().isRealized()).isFalse();
        }

        @Test
        void maxReturnsEmptyOptional() {
            assertThat(CanBeEmpty.<Integer>createEmpty().max(Comparator.naturalOrder())).isEmpty();
        }

        @Test
        void minReturnsEmptyOptional() {
            assertThat(CanBeEmpty.<Integer>createEmpty().min(Comparator.naturalOrder())).isEmpty();
        }

        @Test
        void maxKeyReturnsEmptyOptional() {
            assertThat(CanBeEmpty.<Integer>createEmpty().maxKey(x -> Math.abs(x))).isEmpty();
        }

        @Test
        void minKeyReturnsEmptyOptional() {
            assertThat(CanBeEmpty.<Integer>createEmpty().minKey(x -> Math.abs(x))).isEmpty();
        }

        @Test
        void findReturnsEmptyOptional() {
            assertThat(createEmpty().find(-1)).isEmpty();
            assertThat(createEmpty().find(0)).isEmpty();
            assertThat(createEmpty().find(1)).isEmpty();
        }

        @Test
        void findFirstReturnsEmptyOptional() {
            assertThat(createEmpty().findFirst()).isEmpty();
            assertThat(createEmpty().findFirst(x -> true)).isEmpty();
        }

        @Test
        void forEachDoesNothing() {
            var consumer = Mockito.<Consumer<Integer>>mock();

            CanBeEmpty.<Integer>createEmpty().forEach(consumer);

            verifyNoInteractions(consumer);
        }

        @Test
        void runDoesNothing() {
            var proc = Mockito.<Consumer<Integer>>mock();

            CanBeEmpty.<Integer>createEmpty().run(proc);

            verifyNoInteractions(proc);
        }

        @Test
        void realizeReturnsEmpty() {
            var sut = CanBeEmpty.<String>createEmpty();

            assertThat(sut.isRealized()).isFalse();

            var forced = sut.realize();

            assertThat(sut).isEmpty();
            assertThat(sut.isRealized()).isTrue();
            assertThat(forced.isRealized()).isTrue();
            assertThat(sut).isSameAs(forced);
        }

        @Test
        void iteratorReturnsEmptyIterator() {
            assertThat(createEmpty().iterator().hasNext()).isFalse();
        }

        @Test
        void streamReturnsEmptyStream() {
            assertThat(createEmpty().stream()).isEmpty();
        }

        @Test
        void parallelStreamReturnsEmptyStream() {
            assertThat(createEmpty().parallelStream()).isEmpty();
        }

        @Test
        void toMapReturnsEmptyMap() {
            assertThat(createEmpty().toMap()).isEmpty();
            assertThat(createEmpty().toMap(k -> k, v -> v)).isEmpty();
            assertThat(createEmpty().toMap(k -> k, v -> v, (a, b) -> a)).isEmpty();
        }

        @Test
        void toListReturnsEmptyList() {
            assertThat(createEmpty().toList())
                    .isInstanceOf(List.class)
                    .isEmpty();
        }

        @Test
        void toSetReturnsEmptySet() {
            assertThat(createEmpty().toSet())
                    .isInstanceOf(Set.class)
                    .isEmpty();
        }

        @Test
        void toStringReturnsEmptyBrackets() {
            assertThat(createEmpty()).hasToString("[]");
        }
    }

    @Test
    void firstReturnsFirstItem() {
        var sut = iterate(0, x -> x + 1);

        assertThat(sut.first()).isEqualTo(0);
    }

    @Test
    void restReturnsSeqWithItemsExceptFirst() {
        var sut = iterate(0, x -> x + 1);

        var rest = sut.rest();

        assertThat(rest.first()).isEqualTo(1);
        assertThat(rest.rest().first()).isEqualTo(2);
        assertThat(rest.rest().rest().first()).isEqualTo(3);
        assertThat(rest.rest().rest().rest().first()).isEqualTo(4);
    }

    @Test
    void isEmptyReturnsFalse() {
        var sut = iterate("", x -> x + x.length());

        assertThat(sut.isEmpty()).isFalse();
    }

    @Nested
    class IsRealized {

        @Test
        void returnsFalseForUnrealisedLazySeq() {
            var sut = iterate(0, x -> x + 1);

            assertThat(sut.isRealized()).isFalse();
        }

        @Test
        void returnsTrueIfFirstItemWasAccessed() {
            var sut = iterate(0, x -> x + 1);

            assertThat(sut.isRealized()).isFalse();

            sut.first();

            assertThat(sut.isRealized()).isTrue();
        }

        @Test
        void returnsTrueIfRestWasAccessed() {
            var sut = iterate(0, x -> x + 1);

            assertThat(sut.isRealized()).isFalse();

            sut.rest();

            assertThat(sut.isRealized()).isTrue();
        }

        @Test
        void returnsTrueWhenAllItemsWereAccessed() {
            var sut = range(1, 4);

            sut.forEach(x -> {
            });

            assertThat(sut.isRealized()).isTrue();
        }
    }

    @Nested
    class Filter {

        @Test
        void doesNotRealizeSeqUnlessAccessed() {
            var sut = iterate(0, x -> x + 1);

            assertThat(sut.filter(x -> x > 0).isRealized()).isFalse();
        }

        @Test
        void returnsNilWhenNoItemsMatch() {
            var sut = iterate(0, x -> x + 1).take(10);

            assertThat(sut.filter(x -> x < 0)).isEmpty();
        }

        @Test
        void returnsMatchingItems() {
            var sut = iterate(0, x -> x + 1);

            assertThat(sut.filter(x -> x > 100).take(3)).containsExactly(101, 102, 103);
        }
    }

    @Nested
    @DisplayName("map")
    class MapTest {

        @Test
        void doesNotRealizeSeqUnlessAccessed() {
            var sut = iterate("x", x -> x + "x");

            assertThat(sut.map(x -> x.length()).isRealized()).isFalse();
        }

        @Test
        void returnsSingleMapResult() {
            var sut = iterate("x", x -> x + "x");

            assertThat(sut.map(x -> x.length()).take(1)).isEqualTo(ISeq.of(1));
        }

        @Test
        void returnsAllMapResults() {
            var sut = iterate("x", x -> x + "x");

            assertThat(sut.map(x -> x.length()).take(3)).isEqualTo(ISeq.of(1, 2, 3));
        }

        @Test
        void returnsInfiniteMapResults() {
            var sut = iterate(0, x -> x + 1);

            assertThat(sut.map(x -> x * 100).take(3)).containsExactly(0, 100, 200);
        }

        @Test
        void returnsInfiniteSeqWithInfiniteSeqs() {
            var sut = iterate(0, i -> i + 1);

            var actual = sut.map(x -> iterate(x, i -> i + x)).take(4);

            assertThat(actual).hasSize(4);
            assertThat(actual.nth(0).take(3)).containsExactly(0, 0, 0);
            assertThat(actual.nth(1).take(3)).containsExactly(1, 2, 3);
            assertThat(actual.nth(2).take(3)).containsExactly(2, 4, 6);
            assertThat(actual.nth(3).take(3)).containsExactly(3, 6, 9);
        }

        @Nested
        class WithOtherSeq {

            @Test
            void doesNotRealizeSeqUnlessAccessed() {
                var sut = iterate("x", x -> x + "x");

                assertThat(sut.map(ISeq.of("a", "b", "c"), (a, b) -> (a + b).length()).isRealized()).isFalse();
            }

            @Test
            void returnsEmptySeqWhenProvidingEmptyOther() {
                assertThat(range(1, 4).map(ISeq.<Integer>of(), (a, b) -> a + b)).isEmpty();
                assertThat(range(1, 4).map(ISeq.<Integer>of().iterator(), (a, b) -> a + b)).isEmpty();
                assertThat(range(1, 4).map(List.<Integer>of(), (a, b) -> a + b)).isEmpty();
                assertThat(range(1, 4).map(Stream.<Integer>of(), (a, b) -> a + b)).isEmpty();
                assertThat(range(1, 4).map(new Integer[0], (a, b) -> a + b)).isEmpty();
                assertThat(range(1, 4).map("", (a, b) -> "" + a + b)).isEmpty();
            }

            @Test
            void returnsANewSeqWithTheItemsOfBothInitialSeqsAreCombinedUsingF() {
                var sut = range(1, 4);

                assertThat(sut.map(ISeq.of("a", "b", "c"), (a, b) -> a + b)).containsExactly("1a", "2b", "3c");
                assertThat(sut.map(ISeq.of("a", "b", "c").iterator(), (a, b) -> a + b)).containsExactly("1a", "2b", "3c");
                assertThat(sut.map(List.of("a", "b", "c"), (a, b) -> a + b)).containsExactly("1a", "2b", "3c");
                assertThat(sut.map(Stream.of("a", "b", "c"), (a, b) -> a + b)).containsExactly("1a", "2b", "3c");
                assertThat(sut.map(new String[]{"a", "b", "c"}, (a, b) -> a + b)).containsExactly("1a", "2b", "3c");
                assertThat(sut.map("abc", (a, b) -> "" + a + b)).containsExactly("1a", "2b", "3c");
            }

            @Test
            void ignoresRemainingItemsIfOneOfTheSeqsIsExhausted() {
                var sut = range(1, 4);

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
            }

            @Test
            void returnsInfiniteLazySeqIfOtherIsInfinite() {
                var sut = iterate(0, x -> x + 1);
                var other = iterate(0, x -> x + 1);

                assertThat(sut.map(other, (a, b) -> a + b).take(4)).containsExactly(0, 2, 4, 6);
            }
        }
    }

    @Nested
    class Mapcat {

        @Test
        void doesNotRealizeSeqUnlessAccessed() {
            var sut = ISeq.of(range(0, 3), range(3, 6));

            assertThat(sut.mapcat(x -> x).isRealized()).isFalse();
        }

        @Test
        void returnsFlattenedSeq() {
            var sut = ISeq.of(range(0, 3), range(3, 6));

            assertThat(sut.mapcat(x -> x)).containsExactly(0, 1, 2, 3, 4, 5);
        }

        @Test
        void returnsLazySeqWithMappingResultsConcatenated() {
            var sut = iterate(8, x -> x + 1).map(x -> x.toString());

            assertThat(sut.mapcat(x -> ISeq.sequence(x.split(""))).take(8)).containsExactly("8", "9", "1", "0", "1", "1", "1", "2");
        }

        @Test
        void ignoresEmptyResults() {
            var sut = iterate(0, x -> x + 1);

            assertThat(sut.mapcat(x -> x == 0 ? ISeq.of() : ISeq.of(x, x)).take(6)).containsExactly(1, 1, 2, 2, 3, 3);
        }

        @Test
        void isLazy() {
            var sut = iterate(0, x -> x + 1);

            assertThat(sut.mapcat(x -> ISeq.of(x, x)).take(6)).containsExactly(0, 0, 1, 1, 2, 2);
        }

        @Test
        void returnsInfiniteLazySeqIfMappingResultIsInfinite() {
            var sut = iterate("x", x -> x + "x");

            assertThat(sut.mapcat(x -> iterate("Y", y -> y + "Y")).take(4)).containsExactly("Y", "YY", "YYY", "YYYY");
        }

        @Nested
        class WithOtherColl {

            @Test
            void doesNotRealizeSeqUnlessAccessed() {
                var sut = range(1, 4);

                assertThat(sut.mapcat(ISeq.of("a", "b", "c"), (a, b) -> ISeq.of(a + b, a + b)).isRealized()).isFalse();
            }

            @Test
            void returnsEmptySeqWhenProvidingEmptyOther() {
                var sut = range(1, 4);

                assertThat(sut.mapcat(ISeq.<Integer>of(), (a, b) -> ISeq.of(a + b, a + b))).isEmpty();
            }

            @Test
            void returnsNewSeqWithTheItemsOfBothSeqsCombinedUsingF() {
                var sut = range(1, 4);

                assertThat(sut.mapcat(ISeq.of("a", "b", "c"), (a, b) -> ISeq.of(a + b, a + b)))
                        .containsExactly("1a", "1a", "2b", "2b", "3c", "3c");
            }

            @Test
            void ignoresRemainingItemsIfOneOfTheSeqsIsExhausted() {
                var sut = range(1, 4);

                assertThat(sut.mapcat(ISeq.of("a", "b"), (a, b) -> ISeq.of(a + b, a + b)))
                        .containsExactly("1a", "1a", "2b", "2b");
                assertThat(sut.mapcat(ISeq.of("a", "b", "c", "d"), (a, b) -> ISeq.of(a + b, a + b)))
                        .containsExactly("1a", "1a", "2b", "2b", "3c", "3c");
            }

            @Test
            void returnsInfiniteLazySeqIfOtherIsInfinite() {
                var sut = iterate(0, x -> x + 1);
                var infiniteOther = iterate(0, x -> x + 1);

                assertThat(sut.mapcat(infiniteOther, (a, b) -> ISeq.of(a + b, a + b)).take(8))
                        .containsExactly(0, 0, 2, 2, 4, 4, 6, 6);
            }

            @Test
            void returnsInfiniteLazySeqIfMappingResultIsInfinite() {
                var sut = iterate("x", x -> x + "x");
                var other = iterate("a", x -> x + "b");

                assertThat(sut.mapcat(other, (a, b) -> iterate("Y", y -> y + a + b)).take(4)).containsExactly("Y", "Yxa", "Yxaxa", "Yxaxaxa");
            }
        }
    }

    @Test
    void sizeReturnsNumberOfItemsInFiniteLazySeq() {
        var sut = iterate(0, x -> x + 1).take(4);

        assertThat(sut.size()).isEqualTo(4);
    }

    @Nested
    class Get {

        @Test
        void returnsValueAtIndex() {
            var sut = iterate("", x -> x + x.length());

            assertThat(sut.get(0)).isEqualTo("");
            assertThat(sut.get(1)).isEqualTo("0");
            assertThat(sut.get(2)).isEqualTo("01");
            assertThat(sut.get(3)).isEqualTo("012");
        }

        @Test
        void throwsForNegativeIndex() {
            var sut = iterate(0, x -> x + 1).take(1);

            assertThatExceptionOfType(IndexOutOfBoundsException.class)
                    .isThrownBy(() -> sut.get(-1))
                    .withMessage("Index out of range: -1");
        }

        @Test
        void throwsIfIndexNotPresent() {
            var sut = iterate(0, x -> x + 1).take(1);

            assertThatExceptionOfType(IndexOutOfBoundsException.class)
                    .isThrownBy(() -> sut.get(1))
                    .withMessage("Index out of range: 1");
        }
    }

    @Nested
    class Nth {

        @Test
        void returnsValueAtIndex() {
            var sut = iterate("", x -> x + x.length());

            assertThat(sut.nth(0)).isEqualTo("");
            assertThat(sut.nth(1)).isEqualTo("0");
            assertThat(sut.nth(2)).isEqualTo("01");
            assertThat(sut.nth(3)).isEqualTo("012");
        }

        @Test
        void returnsDefaultValueIfIndexNotPresent() {
            var sut = iterate("", x -> x + x.length()).take(1);

            assertThat(sut.nth(0, "x")).isEqualTo("");
            assertThat(sut.nth(1, "x")).isEqualTo("x");
            assertThat(sut.nth(2, "x")).isEqualTo("x");
            assertThat(sut.nth(3, "x")).isEqualTo("x");
        }

        @Test
        void throwsForNegativeIndex() {
            var sut = iterate("", x -> x + x.length()).take(1);

            assertThatExceptionOfType(IndexOutOfBoundsException.class)
                    .isThrownBy(() -> sut.nth(-1))
                    .withMessage("Index out of range: -1");
        }

        @Test
        void throwsIfIndexNotPresent() {
            var sut = iterate("", x -> x + x.length()).take(1);

            assertThatExceptionOfType(IndexOutOfBoundsException.class)
                    .isThrownBy(() -> sut.nth(1))
                    .withMessage("Index out of range: 1");
        }
    }

    @Nested
    class Take {

        @Test
        void doesNotRealizeSeqUnlessAccessed() {
            var sut = iterate(0, x -> x + 1);

            assertThat(sut.take(3).isRealized()).isFalse();
        }

        @Test
        void returnsNilForNegativeItems() {
            var sut = iterate(0, x -> x + 1);

            assertThat(sut.take(-1)).isEqualTo(Nil.empty());
        }

        @Test
        void returnsNilForZeroItems() {
            var sut = iterate(0, x -> x + 1);

            assertThat(sut.take(0)).isEqualTo(Nil.empty());
        }

        @Test
        void returnsConsForMoreThanZeroItems() {
            var sut = iterate(0, x -> x + 1);

            assertThat(sut.take(3))
                    .isExactlyInstanceOf(LazySeq.class)
                    .containsExactly(0, 1, 2);
        }
    }

    @Nested
    class Drop {

        @Test
        void doesNotRealizeSeqUnlessAccessed() {
            var sut = iterate(0, x -> x + 1);

            assertThat(sut.drop(3).isRealized()).isFalse();
        }

        @Test
        void returnsUnchangedSeqWithNegativeItemsToDrop() {
            var sut = iterate(1, x -> x + 1).take(4);

            assertThat(sut.drop(-1)).containsExactly(1, 2, 3, 4);
        }

        @Test
        void returnsUnchangedSeqWithZeroItemsToDrop() {
            var sut = iterate(1, x -> x + 1).take(4);

            assertThat(sut.drop(0)).containsExactly(1, 2, 3, 4);
        }

        @Test
        void returnsSeqOfAllButTheFirstNItems() {
            var sut = iterate(1, x -> x + 1).take(4);

            assertThat(sut.drop(2)).containsExactly(3, 4);
        }

        @Test
        void returnsEmptySeqIfMoreItemsAreDroppedThanPresent() {
            var sut = iterate(1, x -> x + 1).take(4);

            assertThat(sut.drop(5)).isEmpty();
        }

        @Test
        void isLazy() {
            var sut = iterate(0, x -> x + 1);

            assertThat(sut.drop(100).take(2)).containsExactly(100, 101);
        }
    }

    @Nested
    class TakeWhile {

        @Test
        void doesNotRealizeSeqUnlessAccessed() {
            var sut = iterate(0, x -> x + 1);

            assertThat(sut.takeWhile(x -> x < 3).isRealized()).isFalse();
        }

        @Test
        void returnsEmptySeqWhenFirstItemDoesNotMatch() {
            assertThat(iterate(0, x -> x + 1).takeWhile(x -> x > 0)).isEmpty();
        }

        @Test
        void returnsSeqWithFirstItemMatching() {
            assertThat(iterate(0, x -> x + 1).takeWhile(x -> x < 1)).containsExactly(0);
        }

        @Test
        void returnsSeqWithFirstAndSubsequentItemsMatching() {
            assertThat(iterate(0, x -> x + 1).takeWhile(x -> x < 3)).containsExactly(0, 1, 2);
        }

        @Test
        void returnsSeqWithAllItemsMatching() {
            assertThat(iterate(0, x -> x + 1).takeWhile(x -> true).take(4)).containsExactly(0, 1, 2, 3);
        }
    }

    @Nested
    class DropWhile {

        @Test
        void doesNotRealizeSeqUnlessAccessed() {
            var sut = iterate(0, x -> x + 1);

            assertThat(sut.dropWhile(x -> x < 2).isRealized()).isFalse();
        }

        @Test
        void returnsEmptySeqWhenAllItemsMatch() {
            assertThat(range(1, 5).dropWhile(x -> x > 0)).isEmpty();
        }

        @Test
        void returnsSeqWithItemsThatDoNotMatch() {
            assertThat(iterate(0, x -> x + 1).dropWhile(x -> x < 2).take(4)).containsExactly(2, 3, 4, 5);
        }

        @Test
        void returnsEntireSeqWhenFirstItemDoesNotMatch() {
            assertThat(iterate(0, x -> x + 1).dropWhile(x -> x > 2).take(4)).containsExactly(0, 1, 2, 3);
        }
    }

    @Nested
    class Partition {

        @Test
        void doesNotRealizeSeqUnlessAccessed() {
            var sut = iterate(0, x -> x + 1);

            assertThat(sut.partition(3).isRealized()).isFalse();
        }

        @Test
        void returnsEmptySeqForNegativeSizeN() {
            var sut = iterate(0, x -> x + 1);

            assertThat(sut.partition(-1)).isEmpty();
            assertThat(sut.partition(-1, 10)).isEmpty();
            assertThat(sut.partition(-1, 3)).isEmpty();
            assertThat(sut.partition(-1, 1)).isEmpty();
            assertThat(sut.partition(-1, 0)).isEmpty();
            assertThat(sut.partition(-1, -1)).isEmpty();
        }

        @Test
        void returnsInfiniteSeqOfEmptyListsForZeroSizeN() {
            var sut = iterate(0, x -> x + 1);

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
            var sut = iterate(0, x -> x + 1);

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
            var sut = iterate(0, x -> x + 1);

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
            var sut = iterate(0, x -> x + 1).take(14);

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
            var sut = range(1, 4);

            assertThat(sut.partition(0, 3)).containsExactly(List.of());
            assertThat(sut.partition(0, 4)).containsExactly(List.of());
        }

        @Test
        void returnsASlidingWindowIfStepIsLowerThanSizeN() {
            var sut = iterate(0, x -> x + 1);

            assertThat(sut.partition(3, 2).take(3)).containsExactly(
                    List.of(0, 1, 2),
                    List.of(2, 3, 4),
                    List.of(4, 5, 6));
        }

        @Nested
        class WhenPadIsProvidedAndNotNull {

            @Test
            void fillsIncompleteLastPartitionWithItemsFromPad() {
                var sut = iterate(0, x -> x + 1).take(14);

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
                var sut = iterate(0, x -> x + 1).take(14);

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
        void doesNotRealizeSeqUnlessAccessed() {
            var sut = iterate(0, x -> x + 1);

            assertThat(sut.partitionAll(3).isRealized()).isFalse();
        }

        @Test
        void returnsEmptySeqForNegativeSizeN() {
            var sut = iterate(0, x -> x + 1);

            assertThat(sut.partitionAll(-1)).isEmpty();
            assertThat(sut.partitionAll(-1, 10)).isEmpty();
            assertThat(sut.partitionAll(-1, 3)).isEmpty();
            assertThat(sut.partitionAll(-1, 1)).isEmpty();
            assertThat(sut.partitionAll(-1, 0)).isEmpty();
            assertThat(sut.partitionAll(-1, -1)).isEmpty();
        }

        @Test
        void returnsInfiniteSeqOfEmptyListsForZeroSizeN() {
            var sut = iterate(0, x -> x + 1);

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
            var sut = iterate(0, x -> x + 1);

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
            var sut = iterate(0, x -> x + 1);

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
            var sut = range(1, 4);

            assertThat(sut.partitionAll(0, 3)).containsExactly(List.of());
            assertThat(sut.partitionAll(0, 4)).containsExactly(List.of());
        }

        @Test
        void returnsASlidingWindowIfStepIsLowerThanSizeN() {
            var sut = iterate(0, x -> x + 1);

            assertThat(sut.partitionAll(3, 2).take(3)).containsExactly(
                    List.of(0, 1, 2),
                    List.of(2, 3, 4),
                    List.of(4, 5, 6));
        }

        @Test
        void returnsAnIncompleteLastPartition() {
            var sut = iterate(0, x -> x + 1).take(14);

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
        void doesNotRealizeSeqUnlessAccessed() {
            var sut = iterate(0, x -> x + 1);

            assertThat(sut.reductions((a, b) -> a + b).isRealized()).isFalse();

            assertThat(sut.reductions(0, (a, b) -> a + b).isRealized()).isFalse();
            assertThat(sut.reductions(0, (a, b) -> a + b).rest().isRealized()).isFalse();
        }

        @Test
        void returnsASeqWithTheIntermediateValuesOfTheReduction() {
            var sut = iterate(1, x -> x + 1);

            assertThat(sut.reductions((a, b) -> a + b).take(3)).containsExactly(1, 3, 6);
        }

        @Test
        void returnsASeqWithTheIntermediateValuesOfTheReductionStartingWithInit() {
            var sut = iterate(1, x -> x + 1);

            assertThat(sut.reductions(0, (a, b) -> a + b).take(4)).containsExactly(0, 1, 3, 6);
        }
    }

    @Nested
    @DisplayName("cons")
    class ConsTest {

        @Test
        void returnsNewSeqWithItemPrepended() {
            var sut = range();

            var actual = sut.cons(-1);

            assertThat(actual.first()).isEqualTo(-1);
            assertThat(actual.rest().take(3)).containsExactly(0, 1, 2);
            assertThat(actual.take(4)).containsExactly(-1, 0, 1, 2);
        }

        @Test
        void acceptsNullAsItem() {
            var sut = range();

            var actual = sut.cons(null);

            assertThat(actual.first()).isNull();
            assertThat(actual.rest().take(3)).containsExactly(0, 1, 2);
            assertThat(actual.take(4)).containsExactly(null, 0, 1, 2);
        }
    }

    @Nested
    class Reduce {

        @Test
        void returnsEmptyOptionalForLessThanTwoItemsWhenValIsNotSupplied() {
            var sut = iterate(0, x -> x + 1);

            assertThat(sut.take(0).reduce((a, b) -> a + b)).isEmpty();
            assertThat(sut.take(1).reduce((a, b) -> a + b)).isEmpty();
        }

        @Test
        void returnsOptionalResultWhenValIsNotSupplied() {
            var sut = iterate(0, x -> x + 1);

            assertThat(sut.take(0).reduce((a, b) -> a + b)).isEmpty();
            assertThat(sut.take(1).reduce((a, b) -> a + b)).isEmpty();
            assertThat(sut.take(2).reduce((a, b) -> a + b)).hasValue(1);
            assertThat(sut.take(4).reduce((a, b) -> a + b)).hasValue(6);
        }

        @Test
        void returnsResultWhenValIsSupplied() {
            var sut = iterate(1, x -> x + 1);

            assertThat(sut.take(1).reduce(0, (a, b) -> a + b)).isEqualTo(1);
            assertThat(sut.take(2).reduce(0, (a, b) -> a + b)).isEqualTo(3);
            assertThat(sut.take(3).reduce(0, (a, b) -> a + b)).isEqualTo(6);
        }

        @Test
        void returnsResultOfDifferentTypeThanSeq() {
            var sut = iterate(0, x -> x + 1);

            assertThat(sut.take(1).reduce("", (acc, x) -> acc + x.toString())).isEqualTo("0");
            assertThat(sut.take(2).reduce("", (acc, x) -> acc + x.toString())).isEqualTo("01");
            assertThat(sut.take(3).reduce("", (acc, x) -> acc + x.toString())).isEqualTo("012");
            assertThat(sut.take(4).reduce("", (acc, x) -> acc + x.toString())).isEqualTo("0123");
        }
    }

    @Nested
    class Distinct {

        @Test
        void doesNotRealizeSeqUnlessAccessed() {
            var sut = iterate(0, x -> x + 1);

            assertThat(sut.distinct().isRealized()).isFalse();
        }

        @Test
        void returnsSeqWithSingleItem() {
            assertThat(range(1).distinct()).containsExactly(0);
        }

        @Test
        void returnsSeqThatAlreadyIsDistinct() {
            var sut = iterate(0, x -> x + 1).take(4);

            assertThat(sut.distinct().take(4)).containsExactly(0, 1, 2, 3);
        }

        @Test
        void returnsSeqWithSingleItemForSeqWithIdenticalItems() {
            var sut = iterate("a", x -> x);

            assertThat(sut.take(10).distinct()).containsExactly("a");
            assertThat(sut.distinct().take(1)).containsExactly("a");
        }

        @Test
        void returnsDistinctItemsInSameOrderAsEncounteredFirst() {
            var sut = sequence(List.of("a", "c", "a", "b", "b", "d", "f", "e", "g", "e").iterator());

            assertThat(sut.distinct().toList()).containsExactly("a", "c", "b", "d", "f", "e", "g");
        }
    }

    @Nested
    class Sorted {

        @Test
        void returnsSeqWithSingleItem() {
            assertThat(range(1).sorted()).isEqualTo(ISeq.of(0));
        }

        @Test
        void returnsSeqWithAllItemsSortedUsingDefaultComparator() {
            var sut = iterate(10, x -> x - 1);

            assertThat(sut.take(4).sorted()).containsExactly(7, 8, 9, 10);
        }

        @Test
        void returnsSeqWithAllItemsSortedUsingSuppliedComparator() {
            var sut = iterate(0, x -> x + 1);

            assertThat(sut.take(4).sorted(Comparator.reverseOrder())).containsExactly(3, 2, 1, 0);
        }
    }

    @Nested
    class Some {

        @Test
        void returnsFalseIfNoneOfTheItemsMatchPred() {
            var sut = iterate(0, x -> x + 1);

            assertThat(sut.take(10).some(x -> x < 0)).isFalse();
        }

        @Test
        void returnsTrueIfAllItemsMatchPred() {
            var sut = iterate(0, x -> x + 1);

            assertThat(sut.take(10).some(x -> x >= 0)).isTrue();
        }

        @Test
        void returnsTrueIfFirstItemInInfiniteSeqMatchesPred() {
            var sut = iterate(0, x -> x + 1);

            assertThat(sut.some(x -> x == 0)).isTrue();
        }

        @Test
        void returnsTrueIfSomeItemInInfiniteSeqMatchesPred() {
            var sut = iterate(0, x -> x + 1);

            assertThat(sut.some(x -> x == 5)).isTrue();
        }

        @Test
        void returnsTrueIfLastItemMatchesPred() {
            var sut = iterate(0, x -> x + 1);

            assertThat(sut.take(10).some(x -> x == 9)).isTrue();
        }
    }

    @Nested
    class Every {

        @Test
        void returnsTrueIfAllItemsInSeqMatchPred() {
            var sut = iterate(1, x -> x + 1);

            assertThat(sut.take(100).every(x -> x > 0)).isTrue();
        }

        @Test
        void returnsFalseIfFirstItemInInfiniteSeqDoesNotMatchPred() {
            var sut = iterate(0, x -> x + 1);

            assertThat(sut.every(x -> x > 0)).isFalse();
        }

        @Test
        void returnsFalseIfAnyItemInInfiniteSeqDoesNotMatchPred() {
            var sut = iterate(0, x -> x + 1);

            assertThat(sut.every(x -> x < 100)).isFalse();
        }

        @Test
        void returnsFalseIfLastItemInInfiniteSeqDoesNotMatchPred() {
            var sut = iterate(1, x -> x + 1);

            assertThat(sut.take(100).every(x -> x < 100)).isFalse();
        }
    }

    @Nested
    class NotAny {

        @Test
        void returnsFalseIfFirstItemMatchesPred() {
            var sut = iterate(0, x -> x + 1);

            assertThat(sut.notAny(x -> x == 0)).isFalse();
        }

        @Test
        void returnsFalseIfAnyItemMatchesPred() {
            var sut = iterate(0, x -> x + 1);

            assertThat(sut.notAny(x -> x == 100)).isFalse();
        }

        @Test
        void returnsFalseIfAllItemsMatchPred() {
            var sut = iterate(1, x -> x + 1);

            assertThat(sut.notAny(x -> x > 0)).isFalse();
        }

        @Test
        void returnsTrueIfNoItemMatchesPred() {
            var sut = iterate(0, x -> x + 1);

            assertThat(sut.take(100).notAny(x -> x < 0)).isTrue();
        }
    }

    @Nested
    class Max {

        @Test
        void returnsSingleItem() {
            assertThat(range(1).max(Comparator.naturalOrder())).hasValue(0);
        }

        @Test
        void returnsHighestNumber() {
            var sut = iterate(1, x -> x + 1);

            assertThat(sut.take(100).max(Comparator.naturalOrder())).hasValue(100);
        }

        @Test
        void returnsLongestString() {
            var sut = iterate("x", x -> x + "x");

            assertThat(sut.take(6).max(Comparator.comparingInt(x -> x.length()))).hasValue("xxxxxx");
        }

        @Test
        void returnsTheLastOccurrenceOfLongestStringIfMoreThanOneItemFound() {
            var sut = sequence(List.of("x", "xx", "aaa", "x", "bbb").iterator());

            assertThat(sut.max(Comparator.comparingInt(x -> x.length()))).hasValue("bbb");
        }
    }

    @Nested
    class Min {

        @Test
        void returnsSingleItem() {
            assertThat(range(1).min(Comparator.naturalOrder())).hasValue(0);
        }

        @Test
        void returnsLowestNumber() {
            var sut = iterate(-1, x -> x - 1);

            assertThat(sut.take(100).min(Comparator.naturalOrder())).hasValue(-100);
        }

        @Test
        void returnsShortestString() {
            var sut = sequence(List.of("xxxxxx", "xxxxx", "xxxx", "x", "xx", "xxx").iterator());

            assertThat(sut.min(Comparator.comparingInt(x -> x.length()))).hasValue("x");
        }

        @Test
        void returnsTheLastOccurrenceOfShortestStringIfMoreThanOneItemFound() {
            var sut = sequence(List.of("a", "xx", "aaa", "x", "bbb", "b").iterator());

            assertThat(sut.min(Comparator.comparingInt(x -> x.length()))).hasValue("b");
        }
    }

    @Nested
    class MaxKey {

        @Test
        void returnsSingleItem() {
            assertThat(range(1).maxKey(x -> Math.abs(x))).hasValue(0);
        }

        @Test
        void returnsHighestNumber() {
            var sut = iterate(1, x -> x + 1);

            assertThat(sut.take(100).maxKey(x -> Math.abs(x))).hasValue(100);
        }

        @Test
        void returnsLongestString() {
            var sut = iterate("x", x -> x + "x");

            assertThat(sut.take(6).maxKey(x -> x.length())).hasValue("xxxxxx");
        }

        @Test
        void returnsTheLastOccurrenceOfLongestStringIfMoreThanOneItemFound() {
            var sut = sequence(List.of("x", "xx", "aaa", "x", "bbb").iterator());

            assertThat(sut.maxKey(x -> x.length())).hasValue("bbb");
        }
    }

    @Nested
    class MinKey {

        @Test
        void returnsSingleItem() {
            assertThat(range(1).minKey(x -> Math.abs(x))).hasValue(0);
        }

        @Test
        void returnsLowestNumber() {
            var sut = iterate(-1, x -> x - 1);

            assertThat(sut.take(100).minKey(x -> x)).hasValue(-100);
        }

        @Test
        void returnsShortestString() {
            var sut = sequence(List.of("xxxxxx", "xxxxx", "xxxx", "x", "xx", "xxx").iterator());

            assertThat(sut.minKey(x -> x.length())).hasValue("x");
        }

        @Test
        void returnsTheLastOccurrenceOfShortestStringIfMoreThanOneItemFound() {
            var sut = sequence(List.of("x", "xx", "aaa", "x", "bbb").iterator());

            assertThat(sut.minKey(x -> x.length())).hasValue("x");
        }
    }

    @Nested
    class Find {

        @Test
        void returnsOptionalOfValueAtIndex() {
            var sut = iterate("", x -> x + x.length());

            assertThat(sut.find(0)).hasValue("");
            assertThat(sut.find(1)).hasValue("0");
            assertThat(sut.find(2)).hasValue("01");
            assertThat(sut.find(3)).hasValue("012");
        }

        @Test
        void returnsEmptyOptionalForNegativeIndex() {
            assertThat(range(1).find(-1)).isEmpty();
        }

        @Test
        void returnsEmptyOptionalIfIndexNotPresent() {
            assertThat(range(1).find(1)).isEmpty();
        }
    }

    @Nested
    class FindFirst {

        @Test
        void returnsOptionalOfHead() {
            var sut = iterate(0, x -> x + 1);

            assertThat(sut.findFirst()).hasValue(0);
        }

        @Test
        void returnsEmptyOptionalWhenNoItemsMatchPred() {
            var sut = iterate(0, x -> x + 1).take(10);

            assertThat(sut.findFirst(x -> x < 0)).isEmpty();
        }

        @Test
        void returnsOptionalOfFirstMatchingItem() {
            var sut = iterate(0, x -> x + 1);

            assertThat(sut.findFirst(x -> x > 100)).hasValue(101);
        }
    }

    @Nested
    class Realize {

        @Test
        void realizesThisSeqAndReturnsIt() {
            var sut = range(4).map(x -> x.toString());
            assertThat(sut.isRealized()).isFalse();

            var forced = sut.realize();
            assertThat(sut.isRealized()).isTrue();
            assertThat(forced).isSameAs(sut);
            assertThat(forced.isRealized()).isTrue();
        }
    }

    @Nested
    class ToList {

        @Test
        void returnsFullyRealizedList() {
            var sut = iterate(0, x -> x + 1);

            assertThat(sut.isRealized()).isFalse();
            assertThat(sut.take(4).toList())
                    .isInstanceOf(List.class)
                    .containsExactly(0, 1, 2, 3);
            assertThat(sut.isRealized()).isTrue();
        }
    }

    @Test
    void forEachCallsConsumerForEveryItemPresent() {
        var consumer = Mockito.<Consumer<Integer>>mock();

        var sut = iterate(0, x -> x + 1);

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

        var sut = iterate(0, x -> x + 1);

        sut.take(5).run(proc);

        verify(proc).accept(0);
        verify(proc).accept(1);
        verify(proc).accept(2);
        verify(proc).accept(3);
        verify(proc).accept(4);
        verifyNoMoreInteractions(proc);
    }

    @Nested
    @DisplayName("iterator")
    class IteratorTest {

        @Test
        void returnsIterator() {
            var sut = iterate(0, x -> x + 1);

            var actual = sut.take(2).iterator();

            assertThat(actual.hasNext()).isTrue();
            assertThat(actual.next()).isEqualTo(0);
            assertThat(actual.hasNext()).isTrue();
            assertThat(actual.next()).isEqualTo(1);
            assertThat(actual.hasNext()).isFalse();
        }

        @Test
        void returnsInfiniteIterator() {
            var sut = iterate(0, x -> x + 1);

            var actual = sut.iterator();

            assertThat(actual.hasNext()).isTrue();
            assertThat(actual.next()).isEqualTo(0);
            assertThat(actual.hasNext()).isTrue();
            assertThat(actual.next()).isEqualTo(1);
            assertThat(actual.hasNext()).isTrue();
        }
    }

    @Test
    void streamReturnsStream() {
        var sut = iterate(0, x -> x + 1);

        assertThat(sut.stream().limit(3)).containsExactly(0, 1, 2);
    }

    @Test
    void parallelStreamReturnsStream() {
        var sut = iterate(0, x -> x + 1);

        assertThat(sut.parallelStream().limit(3)).containsExactly(0, 1, 2);
    }

    @Nested
    class ToMap {

        @Test
        void returnsMapForSeqOfEntries() {
            var sut = iterate("x", x -> x + "x").map(x -> java.util.Map.entry(x.length(), x)).take(3);

            var actual = sut.toMap();

            assertThat(actual)
                    .containsEntry(1, "x")
                    .containsEntry(2, "xx")
                    .containsEntry(3, "xxx");
        }

        @Test
        void returnsMapForSeqOfEntriesWithLastValueWinningOnCollision() {
            var sut = sequence(List.of("a", "aa", "b", "bb").iterator()).map(x -> java.util.Map.entry(x.length(), x));

            var actual = sut.toMap();

            assertThat(actual).hasSize(2)
                    .containsEntry(1, "b")
                    .containsEntry(2, "bb");
        }

        @Test
        void throwsIfSeqIsNotOfTypeEntry() {
            var sut = iterate("x", x -> x + "x").take(3);

            assertThatExceptionOfType(UnsupportedOperationException.class)
                    .isThrownBy(() -> sut.toMap())
                    .withMessage("ISeq is not of type Map.Entry. Provide key- and value-mappers");
        }

        @Test
        void returnsMapBasedOnKeyAndValueMapper() {
            var sut = iterate("x", x -> x + "x");

            var actual = sut.take(3).toMap(k -> k.length(), v -> v);

            assertThat(actual)
                    .containsEntry(1, "x")
                    .containsEntry(2, "xx")
                    .containsEntry(3, "xxx");
        }

        @Test
        void throwsOnCollision() {
            var sut = sequence(List.of("a", "b").iterator());

            assertThatExceptionOfType(IllegalArgumentException.class)
                    .isThrownBy(() -> sut.toMap(k -> k.length(), v -> v))
                    .withMessage("duplicate key: 1");
        }

        @Test
        void returnsMapBasedOnKeyAndValueMapperWithApplyingMergerOnCollision() {
            var sut = sequence(List.of("a", "b", "aa", "bb").iterator());

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
            var sut = iterate(0, x -> x + 1);

            assertThat(sut).hasToString("[0, ?]");
        }

        @Test
        void returnsRealisedItemsInSeq() {
            var sut = iterate(0, x -> x + 1);

            sut.get(2);

            assertThat(sut).hasToString("[0, 1, 2, ?]");
        }

        @Test
        void returnsAllItemsInFullyRealisedSeq() {
            var sut = iterate(0, x -> x + 1).take(4);

            sut.forEach(x -> {
            });

            assertThat(sut).hasToString("[0, 1, 2, 3]");
        }
    }
}