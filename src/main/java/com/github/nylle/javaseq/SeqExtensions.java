package com.github.nylle.javaseq;

import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;

public class SeqExtensions {

    private SeqExtensions() {
    }

    public static <T> ISeq<T> toSeq(Stream<T> stream) {
        return ISeq.seq(stream);
    }

    public static <K, V> ISeq<Map.Entry<K, V>> toSeq(Map<K, V> map) {
        return ISeq.seq(map);
    }

    public static <T> ISeq<T> toSeq(Iterable<T> coll) {
        return ISeq.seq(coll);
    }

    public static <T> ISeq<T> toSeq(Iterator<T> coll) {
        return ISeq.seq(coll);
    }

    public static <T> ISeq<T> toSeq(T[] coll) {
        return ISeq.seq(coll);
    }

    public static ISeq<Character> toSeq(CharSequence coll) {
        return ISeq.seq(coll);
    }

    public static ISeq<Character> toSeq(Character[] coll) {
        return ISeq.seq(coll);
    }

    public static ISeq<Character> toSeq(char[] coll) {
        return ISeq.seq(coll);
    }
}
