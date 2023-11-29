package com.github.nylle.javaseq;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class SeqTest {

    @Nested
    class NilTest {

        @Test
        void firstReturnsNull() {
            assertThat(Seq.Nil.of().first()).isNull();
        }

        @Test
        void restReturnsNil() {
            assertThat(Seq.Nil.of().rest()).isEqualTo(Seq.Nil.of());
        }

        @Test
        void sizeReturnsZero() {
            assertThat(Seq.Nil.of().size()).isZero();
        }

        @Test
        void isEmptyReturnsTrue() {
            assertThat(Seq.Nil.of().isEmpty()).isTrue();
        }

        @Test
        void getReturnsNull() {
            assertThat(Seq.Nil.of().get(0)).isNull();
        }

        @Nested
        class Take {

            @Test
            void returnsNilWithNegativeItems() {
                var sut = Seq.Nil.of();

                assertThat(sut.take(-1)).isEqualTo(Seq.Nil.of());
            }

            @Test
            void returnsNilWithZeroItems() {
                var sut = Seq.Nil.of();

                assertThat(sut.take(0)).isEqualTo(Seq.Nil.of());
            }

            @Test
            void returnsNilForMoreThanZeroItems() {
                var sut = Seq.Nil.of();

                assertThat(sut.take(3)).isEqualTo(Seq.Nil.of());
            }
        }

        @Nested
        class Drop {

            @Test
            void returnsNilWithNegativeItems() {
                assertThat(Seq.Nil.of().drop(-1)).isEqualTo(Seq.Nil.of());
            }

            @Test
            void returnsNilWithZeroItems() {
                assertThat(Seq.Nil.of().drop(0)).isEqualTo(Seq.Nil.of());
            }

            @Test
            void returnsNilForMoreThanZeroItems() {
                assertThat(Seq.Nil.of().drop(12)).isEqualTo(Seq.Nil.of());
            }
        }

        @Test
        void filterReturnsNil() {
            assertThat(Seq.Nil.<Integer>of().filter(x -> x != null)).isEqualTo(Seq.Nil.of());
        }

        @Nested
        class Map {

            @Test
            void returnsNil() {
                assertThat(Seq.Nil.<Integer>of().map(x -> x * 100)).isEqualTo(Seq.Nil.of());
            }

            @Test
            void returnsNilWhenMappingWithOtherSeq() {
                assertThat(Seq.Nil.<Integer>of().map(Seq.Nil.<Integer>of(), (a, b) -> a + b)).isEqualTo(Seq.Nil.of());
                assertThat(Seq.Nil.<Integer>of().map(Seq.of(1, 2), (a, b) -> a + b)).isEqualTo(Seq.Nil.of());
            }
        }

        @Test
        void mapcatReturnsNil() {
            assertThat(Seq.Nil.<Integer>of().mapcat(x -> List.of(x, x))).isEqualTo(Seq.Nil.of());
        }

        @Test
        void takeWhileReturnsNil() {
            assertThat(Seq.Nil.of().takeWhile(x -> true)).isEqualTo(Seq.Nil.of());
        }

        @Test
        void dropWhileReturnsNil() {
            assertThat(Seq.Nil.of().dropWhile(x -> true)).isEqualTo(Seq.Nil.of());
        }

        @Nested
        class Partition {

            @Test
            void returnsNilForZeroSizeN() {
                assertThat(Seq.Nil.of().partition(0)).isEqualTo(Seq.Nil.of());
                assertThat(Seq.Nil.of().partition(0, 2)).isEqualTo(Seq.Nil.of());
                assertThat(Seq.Nil.of().partition(0, 2, List.of(1))).isEqualTo(Seq.Nil.of());
            }

            @Test
            void returnsNilForNegativeSizeN() {
                assertThat(Seq.Nil.of().partition(-1)).isEqualTo(Seq.Nil.of());
                assertThat(Seq.Nil.of().partition(-1, 2)).isEqualTo(Seq.Nil.of());
                assertThat(Seq.Nil.of().partition(-1, 2, List.of(1))).isEqualTo(Seq.Nil.of());
            }

            @Test
            void returnsNil() {
                assertThat(Seq.Nil.of().partition(3)).isEqualTo(Seq.Nil.of());
                assertThat(Seq.Nil.of().partition(3, 2)).isEqualTo(Seq.Nil.of());
                assertThat(Seq.Nil.of().partition(3, 3, List.of(1, 2, 3))).isEqualTo(Seq.Nil.of());
            }
        }

        @Nested
        class PartitionAll {

            @Test
            void returnsNilForZeroSizeN() {
                assertThat(Seq.Nil.of().partitionAll(0)).isEqualTo(Seq.Nil.of());
                assertThat(Seq.Nil.of().partitionAll(0, 2)).isEqualTo(Seq.Nil.of());
            }

            @Test
            void returnsNilForNegativeSizeN() {
                assertThat(Seq.Nil.of().partitionAll(-1)).isEqualTo(Seq.Nil.of());
                assertThat(Seq.Nil.of().partitionAll(-1, 2)).isEqualTo(Seq.Nil.of());
            }

            @Test
            void returnsNil() {
                assertThat(Seq.Nil.of().partitionAll(3)).isEqualTo(Seq.Nil.of());
                assertThat(Seq.Nil.of().partitionAll(3, 2)).isEqualTo(Seq.Nil.of());
            }
        }

        @Nested
        class Reductions {

            @Test
            void returnsNil() {
                assertThat(Seq.Nil.<Integer>of().reductions((a, b) -> a + b)).isEqualTo(Seq.Nil.of());
            }

            @Test
            void returnsSeqOfInit() {
                assertThat(Seq.Nil.<String>of().reductions("a", (a, b) -> a + b)).containsExactly("a");
            }

        }

        @Test
        void consReturnsNewSeqWithItemPrepended() {
            var actual = Seq.Nil.<String>of().cons("x");

            assertThat(actual.isRealized()).isFalse();
            assertThat(actual.first()).isEqualTo("x");
            assertThat(actual.rest()).isEmpty();
        }

        @Nested
        class Reduce {

            @Test
            void returnsEmptyOptional() {
                assertThat(Seq.Nil.<Integer>of().reduce((a, b) -> a + b)).isEmpty();
            }

            @Test
            void returnsVal() {
                assertThat(Seq.Nil.<Integer>of().reduce(0, (a, b) -> a + b)).isEqualTo(0);
            }
        }

        @Test
        void distinctReturnsNil() {
            assertThat(Seq.Nil.of().distinct()).isEqualTo(Seq.Nil.of());
        }

        @Test
        void sortedReturnsNil() {
            assertThat(Seq.Nil.of().sorted()).isEqualTo(Seq.Nil.of());
            assertThat(Seq.Nil.<Integer>of().sorted(Comparator.naturalOrder())).isEqualTo(Seq.Nil.of());
        }

        @Test
        void someReturnsFalse() {
            assertThat(Seq.Nil.<Integer>of().some(x -> true)).isFalse();
        }

        @Test
        void everyReturnsTrue() {
            assertThat(Seq.Nil.<Integer>of().every(x -> false)).isTrue();
        }

        @Test
        void notAnyReturnsTrue() {
            assertThat(Seq.Nil.<Integer>of().every(x -> false)).isTrue();
        }

        @Test
        void isRealizedReturnsTrue() {
            assertThat(Seq.Nil.of().isRealized()).isTrue();
        }

        @Test
        void maxReturnsEmptyOptional() {
            assertThat(Seq.Nil.<Integer>of().max(Comparator.naturalOrder())).isEmpty();
        }

        @Test
        void minReturnsEmptyOptional() {
            assertThat(Seq.Nil.<Integer>of().min(Comparator.naturalOrder())).isEmpty();
        }

        @Test
        void maxKeyReturnsEmptyOptional() {
            assertThat(Seq.Nil.<Integer>of().maxKey(x -> Math.abs(x))).isEmpty();
        }

        @Test
        void minKeyReturnsEmptyOptional() {
            assertThat(Seq.Nil.<Integer>of().minKey(x -> Math.abs(x))).isEmpty();
        }

        @Test
        void findReturnsEmptyOptional() {
            assertThat(Seq.Nil.of().find(-1)).isEmpty();
            assertThat(Seq.Nil.of().find(0)).isEmpty();
            assertThat(Seq.Nil.of().find(1)).isEmpty();
        }

        @Test
        void findFirstReturnsEmptyOptional() {
            assertThat(Seq.Nil.of().findFirst()).isEmpty();
            assertThat(Seq.Nil.of().findFirst(x -> true)).isEmpty();
        }

        @Test
        void forEachDoesNothing() {
            var consumer = Mockito.<Consumer<Integer>>mock();

            Seq.Nil.<Integer>of().forEach(consumer);

            verifyNoInteractions(consumer);
        }

        @Test
        void iteratorReturnsEmptyIterator() {
            assertThat(Seq.Nil.of().iterator().hasNext()).isFalse();
        }

        @Test
        void streamReturnsEmptyStream() {
            assertThat(Seq.Nil.of().stream()).isEmpty();
        }

        @Test
        void parallelStreamReturnsEmptyStream() {
            assertThat(Seq.Nil.of().parallelStream()).isEmpty();
        }

        @Test
        void subListReturnsEmptyList() {
            assertThat(Seq.Nil.of().subList(0, 0)).isEmpty();
            assertThat(Seq.Nil.of().subList(-1, -1)).isEmpty();
            assertThat(Seq.Nil.of().subList(1, 5)).isEmpty();
            assertThat(Seq.Nil.of().subList(2, 1)).isEmpty();
        }

        @Test
        void toMapReturnsEmptyMap() {
            assertThat(Seq.Nil.of().toMap()).isEmpty();
            assertThat(Seq.Nil.of().toMap(k -> k, v -> v)).isEmpty();
        }

        @Test
        void toListReturnsEmptyList() {
            assertThat(Seq.Nil.of().toList())
                    .isInstanceOf(List.class)
                    .isEmpty();
        }

        @Test
        void toStringReturnsEmptyBrackets() {
            assertThat(Seq.Nil.of()).hasToString("[]");
        }
    }

    @Nested
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
            var sut = Seq.Cons.of(3, () -> Seq.Cons.of(-2, () -> Seq.Cons.of(8, () -> Seq.of(1))));

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
                        .isExactlyInstanceOf(Seq.Nil.class)
                        .isEmpty();
            }

            @Test
            void returnsNilWithZeroItems() {
                var sut = Seq.iterate(0, x -> x + 1);

                assertThat(sut.take(0))
                        .isExactlyInstanceOf(Seq.Nil.class)
                        .isEmpty();
            }

            @Test
            void returnsConsWithMoreThanZeroItems() {
                var sut = Seq.iterate(0, x -> x + 1);

                assertThat(sut.take(3))
                        .isExactlyInstanceOf(Seq.Cons.class)
                        .containsExactly(0, 1, 2);
            }
        }

        @Nested
        class Drop {

            @Test
            void returnsUnchangedSeqWithNegativeItemsToDrop() {
                assertThat(Seq.of(1, 2, 3, 4).drop(-1)).containsExactly(1, 2, 3, 4);
            }

            @Test
            void returnsUnchangedSeqWithZeroItemsToDrop() {
                assertThat(Seq.of(1, 2, 3, 4).drop(0)).containsExactly(1, 2, 3, 4);
            }

            @Test
            void returnsSeqOfAllButTheFirstNItems() {
                assertThat(Seq.of(1, 2, 3, 4).drop(2)).containsExactly(3, 4);
            }

            @Test
            void returnsEmptySeqIfMoreItemsAreDroppedThanPresent() {
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
            void returnsNilWhenNoItemsMatch() {
                var sut = Seq.iterate(0, x -> x + 1).take(10);

                assertThat(sut.filter(x -> x < 0)).isEmpty();
            }

            @Test
            void returnsMatchingItems() {
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

            @Nested
            class WithTwoOtherSeqs {

                @Test
                void returnsEmptySeqWhenProvidingEmptyOther1() {
                    assertThat(Seq.of(1, 2, 3).map(Seq.<Integer>of(), Seq.of(1, 2, 3), (a, b, c) -> a + b + c))
                            .isEmpty();
                }

                @Test
                void returnsEmptySeqWhenProvidingEmptyOther2() {
                    assertThat(Seq.of(1, 2, 3).map(Seq.of(1, 2, 3), Seq.<Integer>of(), (a, b, c) -> a + b + c))
                            .isEmpty();
                }

                @Test
                void returnsANewSeqWithTheItemsOfAllThreeSeqsCombinedUsingF() {
                    var sut = Seq.of(1, 2, 3);

                    assertThat(sut.map(Seq.of("a", "b", "c"), Seq.of("X", "Y", "Z"), (a, b, c) -> a + b + c))
                            .containsExactly("1aX", "2bY", "3cZ");
                }

                @Test
                void ignoresRemainingItemsIfOneOfTheSeqsIsExhausted() {
                    var sut = Seq.of(1, 2, 3);

                    Seq.TriFunction<Integer, String, String, String> f = (a, b, c) -> a + b + c;

                    assertThat(sut.map(Seq.of("a", "b"), Seq.of("X", "Y"), f)).containsExactly("1aX", "2bY");
                    assertThat(sut.map(Seq.of("a", "b"), Seq.of("X", "Y", "Z"), f)).containsExactly("1aX", "2bY");
                    assertThat(sut.map(Seq.of("a", "b", "c"), Seq.of("X", "Y"), f)).containsExactly("1aX", "2bY");

                    assertThat(sut.map(Seq.of("a", "b", "c", "d"), Seq.of("X", "Y", "Z", "0"), f)).containsExactly("1aX", "2bY", "3cZ");
                    assertThat(sut.map(Seq.of("a", "b", "c", "d"), Seq.of("X", "Y", "Z"), f)).containsExactly("1aX", "2bY", "3cZ");
                    assertThat(sut.map(Seq.of("a", "b", "c"), Seq.of("X", "Y", "Z", "0"), f)).containsExactly("1aX", "2bY", "3cZ");
                }

                @Test
                void isLazy() {
                    var sut = Seq.iterate(0, x -> x + 1);
                    var other1 = Seq.iterate(0, x -> x + 1);
                    var other2 = Seq.iterate(0, x -> x + 1);

                    assertThat(sut.map(other1, other2, (a, b, c) -> a + b + c).take(4)).containsExactly(0, 3, 6, 9);
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
        class Cons {

            @Test
            void returnsNewSeqWithItemPrepended() {
                var sut = Seq.range();

                var actual = sut.cons(-1);

                assertThat(actual.isRealized()).isFalse();
                assertThat(actual.first()).isEqualTo(-1);
                assertThat(actual.rest().take(3)).containsExactly(0, 1, 2);
                assertThat(actual.take(4)).containsExactly(-1, 0, 1, 2);
            }

            @Test
            void acceptsNullAsItem() {
                var sut = Seq.of("a", "b", "c");

                var actual = sut.cons(null);

                assertThat(actual.isRealized()).isFalse();
                assertThat(actual.first()).isNull();
                assertThat(actual.rest()).containsExactly("a", "b", "c");
                assertThat(actual).containsExactly(null, "a", "b", "c");
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
                var sut = Seq.Cons.of("a", () -> Seq.of("a"));

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
        class IsRealized {

            @Test
            void returnsFalseForUnrealisedLazySeq() {
                var sut = Seq.iterate(0, x -> x + 1);

                assertThat(sut.isRealized()).isFalse();
            }

            @Test
            void returnsFalseForPartiallyRealisedLazySeq() {
                var sut = Seq.iterate(0, x -> x + 1);

                sut.get(2);

                assertThat(sut.isRealized()).isTrue();
            }

            @Test
            void returnsTrueForFullyRealisedSeq() {
                var sut = Seq.of(1, 2, 3);

                sut.forEach(x -> {});

                assertThat(sut.isRealized()).isTrue();
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

            @Test
            void returnsTheLastOccurrenceOfLongestStringIfMoreThanOneItemFound() {
                var sut = Seq.of("x", "xx", "aaa", "x", "bbb");

                assertThat(sut.take(6).max(Comparator.comparingInt(x -> x.length()))).hasValue("bbb");
            }
        }

        @Nested
        class Min {

            @Test
            void returnsSingleItem() {
                assertThat(Seq.of(1).min(Comparator.naturalOrder())).hasValue(1);
            }

            @Test
            void returnsLowestNumber() {
                var sut = Seq.iterate(-1, x -> x - 1);

                assertThat(sut.take(100).min(Comparator.naturalOrder())).hasValue(-100);
            }

            @Test
            void returnsShortestString() {
                var sut = Seq.of("xxxxxx", "xxxxx", "xxxx", "x", "xx", "xxx");

                assertThat(sut.take(6).min(Comparator.comparingInt(x -> x.length()))).hasValue("x");
            }

            @Test
            void returnsTheLastOccurrenceOfShortestStringIfMoreThanOneItemFound() {
                var sut = Seq.of("a", "xx", "aaa", "x", "bbb", "b");

                assertThat(sut.take(6).min(Comparator.comparingInt(x -> x.length()))).hasValue("b");
            }
        }

        @Nested
        class MaxKey {

            @Test
            void returnsSingleItem() {
                assertThat(Seq.of(1).maxKey(x -> Math.abs(x))).hasValue(1);
            }

            @Test
            void returnsHighestNumber() {
                var sut = Seq.iterate(1, x -> x + 1);

                assertThat(sut.take(100).maxKey(x -> Math.abs(x))).hasValue(100);
            }

            @Test
            void returnsLongestString() {
                var sut = Seq.iterate("x", x -> x + "x");

                assertThat(sut.take(6).maxKey(x -> x.length())).hasValue("xxxxxx");
            }

            @Test
            void returnsTheLastOccurrenceOfLongestStringIfMoreThanOneItemFound() {
                var sut = Seq.of("x", "xx", "aaa", "x", "bbb");

                assertThat(sut.take(6).maxKey(x -> x.length())).hasValue("bbb");
            }
        }

        @Nested
        class MinKey {

            @Test
            void returnsSingleItem() {
                assertThat(Seq.of(1).minKey(x -> Math.abs(x))).hasValue(1);
            }

            @Test
            void returnsLowestNumber() {
                var sut = Seq.iterate(-1, x -> x - 1);

                assertThat(sut.take(100).minKey(x -> x)).hasValue(-100);
            }

            @Test
            void returnsShortestString() {
                var sut = Seq.of("xxxxxx", "xxxxx", "xxxx", "x", "xx", "xxx");

                assertThat(sut.take(6).minKey(x -> x.length())).hasValue("x");
            }

            @Test
            void returnsTheLastOccurrenceOfShortestStringIfMoreThanOneItemFound() {
                var sut = Seq.of("x", "xx", "aaa", "x", "bbb");

                assertThat(sut.take(6).minKey(x -> x.length())).hasValue("x");
            }
        }

        @Nested
        class Find {

            @Test
            void returnsOptionalOfValueAtIndex() {
                var sut = Seq.iterate("", x -> x + x.length());

                assertThat(sut.find(0)).hasValue("");
                assertThat(sut.find(1)).hasValue("0");
                assertThat(sut.find(2)).hasValue("01");
                assertThat(sut.find(3)).hasValue("012");
            }

            @Test
            void returnsEmptyOptionalForNegativeIndex() {
                assertThat(Seq.of(1).find(-1)).isEmpty();
            }

            @Test
            void returnsEmptyOptionalIfIndexNotPresent() {
                assertThat(Seq.of(1).find(1)).isEmpty();
            }
        }

        @Nested
        class FindFirst {

            @Test
            void returnsOptionalOfHead() {
                var sut = Seq.iterate(0, x -> x + 1);

                assertThat(sut.findFirst()).hasValue(0);
            }

            @Test
            void returnsEmptyOptionalWhenNoItemsMatchPred() {
                var sut = Seq.iterate(0, x -> x + 1).take(10);

                assertThat(sut.findFirst(x -> x < 0)).isEmpty();
            }

            @Test
            void returnsOptionalOfFirstMatchingItem() {
                var sut = Seq.iterate(0, x -> x + 1);

                assertThat(sut.findFirst(x -> x > 100)).hasValue(101);
            }
        }

        @Test
        void forEachCallsConsumerForEveryItemPresent() {
            var consumer = Mockito.<Consumer<Integer>>mock();

            var sut = Seq.iterate(0, x -> x + 1);

            sut.take(5).forEach(consumer);

            verify(consumer).accept(0);
            verify(consumer).accept(1);
            verify(consumer).accept(2);
            verify(consumer).accept(3);
            verify(consumer).accept(4);
            verifyNoMoreInteractions(consumer);
        }

        @Test
        void toListReturnsFullyRealizedList() {
            var sut = Seq.iterate(0, x -> x + 1);

            assertThat(sut.isRealized()).isFalse();
            assertThat(sut.take(4).toList())
                    .isInstanceOf(List.class)
                    .containsExactly(0, 1, 2, 3);
            assertThat(sut.isRealized()).isTrue();
        }

        @Nested
        class Iterator {

            @Test
            void returnsIterator() {
                var sut = Seq.iterate(0, x -> x + 1);

                var actual = sut.take(2).iterator();

                assertThat(actual.hasNext()).isTrue();
                assertThat(actual.next()).isEqualTo(0);
                assertThat(actual.hasNext()).isTrue();
                assertThat(actual.next()).isEqualTo(1);
                assertThat(actual.hasNext()).isFalse();
            }

            @Test
            void returnsInfiniteIterator() {
                var sut = Seq.iterate(0, x -> x + 1);

                var actual = sut.iterator();

                assertThat(actual.hasNext()).isTrue();
                assertThat(actual.next()).isEqualTo(0);
                assertThat(actual.hasNext()).isTrue();
                assertThat(actual.next()).isEqualTo(1);
                assertThat(actual.hasNext()).isTrue();
            }

            @Test
            void iteratorThrowsWhenTryingToAccessNextWhenThereIsNone() {
                var actual = Seq.iterate(0, x -> x + 1).take(2).iterator();

                assertThat(actual.next()).isEqualTo(0);
                assertThat(actual.next()).isEqualTo(1);
                assertThat(actual.hasNext()).isFalse();
                assertThatExceptionOfType(NoSuchElementException.class)
                        .isThrownBy(() -> actual.next());
            }
        }

        @Test
        void streamReturnsStream() {
            var sut = Seq.iterate(0, x -> x + 1);

            assertThat(sut.stream().limit(3).toList()).containsExactly(0, 1, 2);
        }

        @Test
        void parallelStreamReturnsStream() {
            var sut = Seq.iterate(0, x -> x + 1);

            assertThat(sut.parallelStream().limit(3).toList()).containsExactly(0, 1, 2);
        }

        @Nested
        class SubList {

            @Test
            void returnsListWithItemsBetweenFromIndexInclusiveAndToIndexExclusive() {
                var sut = Seq.iterate(0, x -> x + 1);

                assertThat(sut.subList(1, 4)).containsExactly(1, 2, 3);
            }

            @Test
            void returnsEmptyListForIndexOutOfBounds() {
                var sut = Seq.iterate(0, x -> x + 1);

                assertThat(sut.take(3).subList(-1, -1)).isEmpty();
                assertThat(sut.take(3).subList(0, -1)).isEmpty();
                assertThat(sut.take(3).subList(1, 0)).isEmpty();
                assertThat(sut.take(3).subList(3, 10)).isEmpty();
            }

            @Test
            void returnsListWithAsManyItemsPresentForIndexOutOfBounds() {
                var sut = Seq.iterate(0, x -> x + 1);

                assertThat(sut.take(3).subList(1, 10)).containsExactly(1, 2);
            }
        }

        @Nested
        class ToMap {

            @Test
            void returnsMapForSeqOfEntries() {
                var sut = Seq.iterate("x", x -> x + "x").map(x -> java.util.Map.entry(x.length(), x)).take(3);

                var actual = sut.toMap();

                assertThat(actual)
                        .containsEntry(1, "x")
                        .containsEntry(2, "xx")
                        .containsEntry(3, "xxx");
            }

            @Test
            void throwsIfSeqIsNotOfTypeEntry() {
                var sut = Seq.iterate("x", x -> x + "x").take(3);

                assertThatExceptionOfType(UnsupportedOperationException.class)
                        .isThrownBy(() -> sut.toMap())
                        .withMessage("Seq is not of type Map.Entry. Provide key- and value-mappers.");
            }

            @Test
            void returnsMapBasedOnKeyAndValueMapper() {
                var sut = Seq.iterate("x", x -> x + "x");

                var actual = sut.take(3).toMap(k -> k.length(), v -> v);

                assertThat(actual)
                        .containsEntry(1, "x")
                        .containsEntry(2, "xx")
                        .containsEntry(3, "xxx");
            }
        }

        @Nested
        class ToString {

            @Test
            void returnsFirstItemOnlyInSeq() {
                var sut = Seq.iterate(0, x -> x + 1);

                assertThat(sut).hasToString("[0, ?]");
            }

            @Test
            void returnsRealisedItemsInSeq() {
                var sut = Seq.iterate(0, x -> x + 1);

                sut.get(2);

                assertThat(sut).hasToString("[0, 1, 2, ?]");
            }

            @Test
            void returnsAllItemsInFullyRealisedSeq() {
                var sut = Seq.iterate(0, x -> x + 1).take(4);

                sut.forEach(x -> {});

                assertThat(sut).hasToString("[0, 1, 2, 3]");
            }
        }
    }

    @Nested
    class Static {

        @Nested
        class Of {

            @Test
            void returnsEmptySeq() {
                assertThat(Seq.of()).isEmpty();
            }

            @Test
            void returnsSeqOfSingleItem() {
                assertThat(Seq.of(1)).containsExactly(1);
            }

            @Test
            void returnsSeqOfSuppliedItems() {
                assertThat(Seq.of(1, 2, 3)).containsExactly(1, 2, 3);
            }

            @Test
            void returnsSeqOfSuppliedLists() {
                assertThat(Seq.of(List.of(1, 2), List.of("foo", "bar"), List.of(true, false)))
                        .containsExactly(List.of(1, 2), List.of("foo", "bar"), List.of(true, false));
            }
        }

        @Nested
        class Sequence {

            @Test
            void returnsSeqOfItemsInIterable() {
                var list = List.of(1, 2, 3);

                assertThat(Seq.sequence(list)).containsExactly(1, 2, 3);
            }

            @Test
            void returnsSeqOfItemsOfIterator() {
                var infiniteIterator = Stream.iterate(0, x -> x + 1).iterator();

                assertThat(Seq.sequence(infiniteIterator).take(4)).containsExactly(0, 1, 2, 3);
            }

            @Test
            void returnsSeqOfItemsInStream() {
                var infiniteStream = Stream.iterate(0, x -> x + 1);

                assertThat(Seq.sequence(infiniteStream).take(4)).containsExactly(0, 1, 2, 3);
            }

            @Test
            void returnsSeqOfItemsInArray() {
                var array = new Integer[] {1, 2, 3};

                assertThat(Seq.sequence(array)).containsExactly(1, 2, 3);
            }

            @Test
            void returnsSeqOfKeyValuePairsInMap() {
                var map = Map.of("a", 1, "b", 2, "c", 3);

                assertThat(Seq.sequence(map)).containsExactlyInAnyOrder(entry("a", 1), entry("b", 2), entry("c", 3));
            }

            @Test
            void returnsEmptySeqIfCollIsNull() {
                assertThat(Seq.sequence((Iterable<Integer>) null)).isEmpty();
                assertThat(Seq.sequence((Stream<Integer>) null)).isEmpty();
                assertThat(Seq.sequence((Iterator<Integer>) null)).isEmpty();
            }

            @Test
            void returnsCollIfAlreadyASeq() {
                var coll = Seq.of(1, 2, 3);
                assertThat(Seq.sequence(coll)).isSameAs(coll);
            }

            @Test
            void doesNotForceLazyColl() {
                assertThat(Seq.sequence(Stream.iterate(0, x -> x + 1)).take(3)).containsExactly(0, 1, 2);
                assertThat(Seq.sequence(Stream.iterate(0, x -> x + 1).iterator()).take(3)).containsExactly(0, 1, 2);
            }
        }

        @Nested
        class Cons {

            @Test
            void returnsSeqFromFirstElementAndSeqSupplier() {
                var first = "a";
                Supplier<Seq<String>> supplier = () -> Seq.Cons.of("b", () -> Seq.Cons.of("c", () -> Seq.Nil.of()));

                var actual = Seq.Cons.of(first, supplier);

                assertThat(actual).containsExactly("a", "b", "c");
            }
        }

        @Nested
        class Concat {

            @Test
            void returnsSeqFromConcatenatingMultipleSeqs() {
                var actual = Seq.concat(Seq.of("a", "b"), Seq.of("c", "d"), Seq.of("e", "f"));

                assertThat(actual).containsExactly("a", "b", "c", "d", "e", "f");
            }

            @Test
            void returnsSeqFromConcatenatingMultipleIterables() {
                var actual = Seq.concat(List.of("a", "b"), List.of("c", "d"), List.of("e", "f"));

                assertThat(actual).containsExactly("a", "b", "c", "d", "e", "f");
            }
        }
        
        @Nested
        class Iterate {
        
            @Test
            void returnsSeqOfInitialValueUsingFunction() {

                var actual = Seq.iterate(0, x -> x + 1);

                assertThat(actual.take(4)).containsExactly(0, 1, 2, 3);

            }
        }

        @Nested
        class Range {

            @Test
            void returnsInfiniteSeqOfIntegersStartingWithZero() {
                assertThat(Seq.range().take(3)).containsExactly(0, 1, 2);
            }

            @Test
            void returnsSeqOfIntegersStartingWithZeroUntilEnd() {
                assertThat(Seq.range(3)).containsExactly(0, 1, 2);
            }

            @Test
            void returnsSeqOfIntegersFromStartInclusiveUntilEndExclusive() {
                assertThat(Seq.range(1, 5)).containsExactly(1, 2, 3, 4);
                assertThat(Seq.range(-5, 5)).containsExactly(-5, -4, -3, -2, -1, 0, 1, 2, 3, 4);
            }

            @Test
            void returnsSeqOfIntegersFromStartInclusiveUntilEndExclusiveByStep() {
                assertThat(Seq.range(10, 25, 5)).containsExactly(10, 15, 20);
                assertThat(Seq.range(10, -25, -5)).containsExactly(10, 5, 0, -5, -10, -15, -20);
                assertThat(Seq.range(-10, 25, 5)).containsExactly(-10, -5, 0, 5, 10, 15, 20);
            }

            @Test
            void returnsInfiniteSeqOfStartWhenStepIsZero() {
                assertThat(Seq.range(10, 25, 0).take(10)).containsExactly(10, 10, 10, 10, 10, 10, 10, 10, 10, 10);
            }

            @Test
            void returnsEmptySeqWhenStartIsEqualToEnd() {
                assertThat(Seq.range(10, 10)).isEmpty();
                assertThat(Seq.range(-10, -10)).isEmpty();
                assertThat(Seq.range(1, 1, 1)).isEmpty();
                assertThat(Seq.range(-1, -1, 1)).isEmpty();
            }
        }
    }

    @Nested
    class Extensions {

        @Nested
        class FromStream {

            @Test
            void returnsEmptySeqIfStreamIsEmpty() {
                assertThat(Seq.Extensions.toSeq(Stream.of())).isEmpty();
            }

            @Test
            void returnsSeqOfItemsInStream() {
                assertThat(Seq.Extensions.toSeq(Stream.of(1, 2, 3))).containsExactly(1, 2, 3);
            }
        }

        @Nested
        class FromArray {

            @Test
            void returnsEmptySeqIfArrayIsEmpty() {
                assertThat(Seq.Extensions.toSeq(new String[0])).isEmpty();
            }

            @Test
            void returnsSeqOfItemsInArray() {
                assertThat(Seq.Extensions.toSeq("f o o".split(" "))).containsExactly("f", "o", "o");
            }
        }

        @Nested
        class FromIterable {

            @Test
            void returnsEmptySeqIfIterableIsEmpty() {
                assertThat(Seq.Extensions.toSeq(List.of())).isEmpty();
            }

            @Test
            void returnsSeqOfItemsInIterable() {
                var list = List.of(1, 2, 3);

                assertThat(Seq.Extensions.toSeq(list)).containsExactly(1, 2, 3);
            }
        }

        @Nested
        class FromIterator {

            @Test
            void returnsEmptySeqIfIterableIsEmpty() {
                assertThat(Seq.Extensions.toSeq(Stream.of().iterator())).isEmpty();
            }

            @Test
            void returnsSeqOfItemsInIterable() {
                var iterator = Stream.iterate(0, x -> x + 1).iterator();

                assertThat(Seq.Extensions.toSeq(iterator).take(4)).containsExactly(0, 1, 2, 3);
            }
        }

        @Nested
        class FromMap {

            @Test
            void returnsEmptySeqIfMapIsEmpty() {
                assertThat(Seq.Extensions.toSeq(Map.of())).isEmpty();
            }

            @Test
            void returnsSeqOfKeyValuePairsInMap() {
                var map = Map.of("a", 1, "b", 2, "c", 3);

                assertThat(Seq.Extensions.toSeq(map)).containsExactlyInAnyOrder(entry("a", 1), entry("b", 2), entry("c", 3));
            }
        }
    }
}