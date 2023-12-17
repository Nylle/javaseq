package com.github.nylle.javaseq;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class AListTest {

    static class TestList<T> extends AList<T> {
        private final List<T> list;

        TestList(List<T> list) {
            this.list = list;
        }

        static <T> TestList<T> from(T... items) {
            return new TestList<>(Arrays.asList(items));
        }

        @Override
        List<T> reify() {
            return list;
        }

        @Override
        public int size() {
            return list.size();
        }

        @Override
        public boolean isEmpty() {
            return list.isEmpty();
        }

        @Override
        public Iterator<T> iterator() {
            return list.iterator();
        }

        @Override
        public T get(int index) {
            return list.get(index);
        }
    }

    @Nested
    class ListTest {

        @Test
        void subListReturnsItemsFromIndexToIndex() {
            var sut = TestList.from(0, 1, 2, 3, 4, 5);

            assertThat(sut.subList(1, 4)).containsExactly(1, 2, 3);
        }

        @Nested
        class ToArray {

            @Test
            void returnsObjectArrayWithAllItemsInThisSeq() {
                var sut = TestList.from("0", "1", "2", "3");

                var actual = sut.toArray();

                assertThat(actual)
                        .isExactlyInstanceOf(Object[].class)
                        .containsExactly("0", "1", "2", "3");
            }

            @Test
            void returnsEmptyArrayForNil() {
                assertThat(Nil.empty().toArray()).isEmpty();
            }

            @Test
            void returnsSuppliedArrayFilledWithAllItemsInThisSeq() {
                var sut = TestList.from("0", "1", "2", "3");

                assertThat(sut.toArray(new String[4]))
                        .isExactlyInstanceOf(String[].class)
                        .containsExactly("0", "1", "2", "3");
            }

            @Test
            void returnsNewArrayFilledWithAllItemsInThisSeqIfSuppliedArrayIsTooSmall() {
                var sut = TestList.from("0", "1", "2", "3");

                assertThat(sut.toArray(new String[2]))
                        .isExactlyInstanceOf(String[].class)
                        .containsExactly("0", "1", "2", "3");
            }
        }

        @Test
        void containsAllReturnsTrueIfAllItemsAreContainedInThisSeq() {
            var sut = TestList.from("0", "1", "2", "3");

            assertThat(sut.containsAll(List.of("1", "2"))).isTrue();
            assertThat(sut.containsAll(List.of("1", "5"))).isFalse();
        }

        @Test
        void indexOfReturnsIndexOfSuppliedObjectInThisSeq() {
            var sut = TestList.from("0", "1", "2", "3");

            assertThat(sut.indexOf("1")).isEqualTo(1);
            assertThat(sut.indexOf("4")).isEqualTo(-1);
        }

        @Test
        void lastIndexOfReturnsIndexOfLastOccurrenceOfSuppliedObject() {
            var sut = TestList.from("a", "b", "c", "a", "d");

            assertThat(sut.lastIndexOf("a")).isEqualTo(3);
            assertThat(sut.lastIndexOf("e")).isEqualTo(-1);
        }

        @Test
        void listIteratorReturnsIteratorForTheListRepresentationOfThisSeq() {
            var sut = TestList.from("0", "1", "2");

            var all = sut.listIterator();
            assertThat(all.hasNext()).isTrue();
            assertThat(all.next()).isEqualTo("0");
            assertThat(all.hasNext()).isTrue();
            assertThat(all.next()).isEqualTo("1");
            assertThat(all.hasNext()).isTrue();
            assertThat(all.next()).isEqualTo("2");
            assertThat(all.hasNext()).isFalse();

            var fromIndex = sut.listIterator(2);
            assertThat(fromIndex.hasNext()).isTrue();
            assertThat(fromIndex.next()).isEqualTo("2");
            assertThat(fromIndex.hasNext()).isFalse();
        }

        @Test
        void setThrows() {
            var sut = TestList.from("0", "1", "2", "3");
            assertThatExceptionOfType(UnsupportedOperationException.class)
                    .isThrownBy(() -> sut.set(4, "4"));
        }

        @Test
        void addThrows() {
            var sut = TestList.from("0", "1", "2", "3");

            assertThatExceptionOfType(UnsupportedOperationException.class)
                    .isThrownBy(() -> sut.add("4"));
            assertThatExceptionOfType(UnsupportedOperationException.class)
                    .isThrownBy(() -> sut.add(4, "4"));
        }

        @Test
        void addAllThrows() {
            var sut = TestList.from("0", "1", "2", "3");

            assertThatExceptionOfType(UnsupportedOperationException.class)
                    .isThrownBy(() -> sut.addAll(List.of("4", "5")));
            assertThatExceptionOfType(UnsupportedOperationException.class)
                    .isThrownBy(() -> sut.addAll(1, List.of("4", "5")));
        }

        @Test
        void removeThrows() {
            var sut = TestList.from("0", "1", "2", "3");

            assertThatExceptionOfType(UnsupportedOperationException.class)
                    .isThrownBy(() -> sut.remove(2));
            assertThatExceptionOfType(UnsupportedOperationException.class)
                    .isThrownBy(() -> sut.remove("2"));
        }

        @Test
        void removeAllThrows() {
            var sut = TestList.from("0", "1", "2", "3");

            assertThatExceptionOfType(UnsupportedOperationException.class)
                    .isThrownBy(() -> sut.removeAll(List.of("1", "2")));
        }

        @Test
        void retainAllThrows() {
            var sut = TestList.from("0", "1", "2", "3");

            assertThatExceptionOfType(UnsupportedOperationException.class)
                    .isThrownBy(() -> sut.retainAll(List.of("1", "2")));
        }

        @Test
        void replaceAllThrows() {
            var sut = TestList.from("0", "1", "2", "3");

            assertThatExceptionOfType(UnsupportedOperationException.class)
                    .isThrownBy(() -> sut.replaceAll(x -> x.toUpperCase()));
        }

        @Test
        void sortThrows() {
            var sut = TestList.from("0", "1", "2", "3");

            assertThatExceptionOfType(UnsupportedOperationException.class)
                    .isThrownBy(() -> sut.sort(Comparator.reverseOrder()));
        }

        @Test
        void clearThrows() {
            var sut = TestList.from("0", "1", "2", "3");

            assertThatExceptionOfType(UnsupportedOperationException.class)
                    .isThrownBy(() -> sut.clear());
        }
    }

    @Nested
    class CollectionTest {

        @Test
        void containsReturnsTrueIfSuppliedObjectIsInThisSeq() {
            var sut = TestList.from("0", "1", "2", "3");

            assertThat(sut.contains("1")).isTrue();
            assertThat(sut.contains("4")).isFalse();
        }

        @Nested
        class ToArray {

            @Test
            void returnsTypedArrayWithAllItemsInThisSeq() {
                var sut = TestList.from("0", "1", "2", "3");

                var actual = sut.toArray(size -> new String[size]);

                assertThat(actual)
                        .isExactlyInstanceOf(String[].class)
                        .containsExactly("0", "1", "2", "3");
            }

            @Test
            void returnsEmptyArrayForNil() {
                var actual = Nil.<String>empty().toArray(size -> new String[size]);

                assertThat(actual)
                        .isExactlyInstanceOf(String[].class)
                        .isEmpty();
            }
        }

        @Test
        void removeIfThrows() {
            var sut = TestList.from("0", "1", "2", "3");

            assertThatExceptionOfType(UnsupportedOperationException.class)
                    .isThrownBy(() -> sut.removeIf(x -> x.equals("1")));
        }
    }
}