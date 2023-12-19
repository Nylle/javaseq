package com.github.nylle.javaseq;

import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;

public class SeqExtensions {

    private SeqExtensions() {
    }

    public static <T> ISeq<T> toSeq(Stream<T> stream) {
        return Fn.seq(stream);
    }

    public static <K, V> ISeq<Map.Entry<K, V>> toSeq(Map<K, V> map) {
        return Fn.seq(map);
    }

    public static <T> ISeq<T> toSeq(Iterable<T> coll) {
        return Fn.seq(coll);
    }

    public static <T> ISeq<T> toSeq(Iterator<T> coll) {
        return Fn.seq(coll);
    }

    public static <T> ISeq<T> toSeq(T[] coll) {
        return Fn.seq(coll);
    }

    public static ISeq<Character> toSeq(CharSequence coll) {
        return Fn.seq(coll);
    }

    public static ISeq<Character> toSeq(Character[] coll) {
        return Fn.seq(coll);
    }

    public static ISeq<Character> toSeq(char[] coll) {
        return Fn.seq(coll);
    }
}
