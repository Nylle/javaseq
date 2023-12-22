package com.github.nylle.javaseq;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UtilTest {

    @Nested
    @DisplayName("nil")
    class NilTest {

        @Test
        void returnsEmptySeq() {
            var actual = ISeq.of();

            assertThat(actual)
                    .isExactlyInstanceOf(Nil.class)
                    .isEmpty();
        }
    }

    @Nested
    @DisplayName("cons")
    class ConsTest {

        @Test
        void returnsSeqFromFirstElementAndSeq() {
            var actual = ISeq.cons("a", ISeq.cons("b", ISeq.cons("c", Nil.empty())));

            assertThat(actual)
                    .isExactlyInstanceOf(Cons.class)
                    .containsExactly("a", "b", "c");
        }

        @Test
        void returnsSeqWithNulls() {
            var sut = ISeq.cons(null, ISeq.cons(null, ISeq.cons(null, Nil.empty())));

            assertThat(sut).containsExactly(null, null, null);
        }
    }

    @Nested
    @DisplayName("lazySeq")
    class LazySeqTest {

        @Test
        void returnsSeqFromSupplier() {
            var actual = ISeq.lazySeq(() ->
                    ISeq.cons("a", ISeq.lazySeq(() ->
                            ISeq.cons("b", ISeq.lazySeq(() ->
                                    ISeq.cons("c", ISeq.lazySeq(() ->
                                            ISeq.of())))))));

            assertThat(actual)
                    .isInstanceOf(LazySeq.class)
                    .containsExactly("a", "b", "c");
        }

        @Test
        void returnsSeqWithNulls() {
            var sut = ISeq.lazySeq(() ->
                    ISeq.cons(null, ISeq.lazySeq(() ->
                            ISeq.cons(null, ISeq.lazySeq(() ->
                                    ISeq.cons(null, ISeq.lazySeq(() ->
                                            ISeq.of())))))));

            assertThat(sut)
                    .isExactlyInstanceOf(LazySeq.class)
                    .containsExactly(null, null, null);
        }
    }

    @Nested
    @DisplayName("arraySeq")
    class ArraySeqTest {

        @Test
        void returnsSeqWithSuppliedItems() {
            var sut = Util.arraySeq(new Integer[] {0, 1, 2, 3});

            assertThat(sut)
                    .isExactlyInstanceOf(ArraySeq.class)
                    .containsExactly(0, 1, 2, 3);
        }

        @Test
        void returnsSeqWithNulls() {
            var sut = Util.arraySeq(new Integer[] {null, null, null});

            assertThat(sut)
                    .isExactlyInstanceOf(ArraySeq.class)
                    .containsExactly(null, null, null);
        }
    }

    @Nested
    @DisplayName("conj")
    class ConjTest {

        @Test
        void returnsNewListWithXAdded() {
            assertThat(Util.conj(List.of(1), 2)).isInstanceOf(List.class).containsExactly(1, 2);
        }

        @Test
        void returnsNewSetWithXAdded() {
            assertThat(Util.conj(Set.of(1), 2)).isInstanceOf(Set.class).containsExactlyInAnyOrder(1, 2);
        }
    }

    @Test
    void chunkInputStreamSeq() throws IOException {
        var tmpFile = File.createTempFile("test", ".tmp");
        var writer = new FileWriter(tmpFile);
        writer.write("Hello world!\nfoo\nbar");
        writer.close();

        try(var inputStream = new FileInputStream(tmpFile)) {

            var sut = Util.chunkInputStreamSeq(inputStream, StandardCharsets.UTF_8);

            assertThat(sut.first()).isEqualTo('H');
            assertThat(sut.nth(11)).isEqualTo('!');
            assertThat(sut.drop(12).first()).isEqualTo('\n');
            assertThat(sut.drop(13).take(3)).containsExactly('f', 'o', 'o');
            assertThat(sut.str()).isEqualTo("Hello world!\nfoo\nbar");
        }
    }
}