package com.github.nylle.javaseq;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class StringSeq extends ASeq<Character> implements ISeq<Character> {

    private final CharSequence str;
    private final int index;
    private final int end;
    private final int count;

    StringSeq(CharSequence str, int index, int end) {
        if (str == null || str.isEmpty()) {
            throw new IllegalArgumentException("string is null or empty");
        }
        if (index >= str.length()) {
            throw new IllegalArgumentException("index " + index + " is out of range for string " + str);
        }
        if (end > str.length()) {
            throw new IllegalArgumentException("end " + end + " is out of range for string " + str);
        }
        if (end <= index) {
            throw new IllegalArgumentException("end " + end + " must be greater than index " + index);
        }
        this.str = str;
        this.index = index;
        this.end = end;
        this.count = end - index;
    }

    @Override
    public Character first() {
        return str.charAt(index);
    }

    @Override
    public ISeq<Character> rest() {
        if (count > 1) {
            return new StringSeq(str, index + 1, end);
        }
        return ISeq.of();
    }

    @Override
    public boolean isRealized() {
        return true;
    }

    @Override
    public ISeq<Character> take(long n) {
        if(n >= count) {
            return this;
        }
        if (n > 0) {
            return new StringSeq(str, index, (int) n + index);
        }
        return ISeq.of();
    }

    @Override
    public ISeq<Character> drop(long n) {
        if (n >= count) {
            return ISeq.of();
        }
        if (n > 0) {
            return new StringSeq(str, (int) n + index, end);
        }
        return this;
    }

    @Override
    public ISeq<Character> takeWhile(Predicate<? super Character> pred) {
        var newEnd = index;
        for (int i = index; i < end; i++) {
            if (!pred.test(str.charAt(i))) {
                break;
            }
            newEnd++;
        }
        if (newEnd > index) {
            return new StringSeq(str, index, newEnd);
        }
        return ISeq.of();
    }

    @Override
    public ISeq<Character> dropWhile(Predicate<? super Character> pred) {
        var newIndex = index;
        for (int i = index; i < end; i++) {
            if (!pred.test(str.charAt(i))) {
                break;
            }
            newIndex++;
        }
        if (newIndex == index) {
            return this;
        }
        if (newIndex < end) {
            return new StringSeq(str, newIndex, end);
        }
        return ISeq.of();
    }

    @Override
    public <U> U reduce(U val, BiFunction<U, ? super Character, U> f) {
        var result = val;
        for (int i = index; i < end; i++) {
            result = f.apply(result, str.charAt(i));
        }
        return result;
    }

    @Override
    public boolean some(Predicate<? super Character> pred) {
        for (int i = index; i < end; i++) {
            if (pred.test(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean every(Predicate<? super Character> pred) {
        for (int i = index; i < end; i++) {
            if (!pred.test(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected Character nth(int i, Supplier<Character> notFound) {
        if (i < 0 || i >= count) {
            return notFound.get();
        }
        return str.charAt(i + index);
    }

    @Override
    public int count() {
        return count;
    }

    @Override
    public ISeq<Character> reverse() {
        return new StringSeq(new StringBuilder(str.subSequence(index, end)).reverse().toString(), 0, end-index);
    }

    @Override
    public List<Character> reify() {
        var acc = new ArrayList<Character>();
        for (int i = index; i < end; i++) {
            acc.add(str.charAt(i));
        }
        return List.copyOf(acc);
    }
}
