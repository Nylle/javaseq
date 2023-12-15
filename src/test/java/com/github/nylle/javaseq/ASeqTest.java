package com.github.nylle.javaseq;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class ASeqTest {

    static class TestASeq<T> extends ASeq<T> {
        private final ISeq<T> rest;

        public TestASeq(ISeq<T> rest) {
            this.rest = rest;
        }

        @Override
        public T first() {
            return null;
        }

        @Override
        public ISeq<T> rest() {
            return rest;
        }

        @Override
        public boolean isRealized() {
            return false;
        }
    }

    @Test
    void toListThrowsForNullValue() {
        var sut = new TestASeq<Integer>(ISeq.of(null, null));

        assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> sut.toList());
    }
}