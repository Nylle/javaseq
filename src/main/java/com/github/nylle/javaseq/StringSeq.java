package com.github.nylle.javaseq;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

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
        return Fn.nil();
    }

    @Override
    public boolean isRealized() {
        return true;
    }

    @Override
    public ISeq<Character> filter(Predicate<? super Character> pred) {
        return Fn.lazySeq(() -> {
            var acc = Fn.<Character>nil();
            for (int i = end - 1; i >= index; i--) {
                var next = str.charAt(i);
                if (pred.test(next)) {
                    acc = Fn.cons(next, acc);
                }
            }
            return acc;
        });
    }

    @Override
    public <R> ISeq<R> map(Function<? super Character, ? extends R> f) {
        return Fn.lazySeq(() -> {
            var acc = Fn.<R>nil();
            for (int i = end - 1; i >= index; i--) {
                acc = Fn.cons(f.apply(str.charAt(i)), acc);
            }
            return acc;
        });
    }

    @Override
    public ISeq<Character> take(long n) {
        if(n >= count) {
            return this;
        }
        if (n > 0) {
            return new StringSeq(str, index, (int) n + index);
        }
        return Fn.nil();
    }

    @Override
    public ISeq<Character> drop(long n) {
        if (n >= count) {
            return Fn.nil();
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
        return Fn.nil();
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
        return Fn.nil();
    }

    @Override
    public <U> ISeq<U> reductions(U init, BiFunction<U, ? super Character, U> f) {
        return Fn.lazySeq(() -> {
            var acc = ISeq.of(init);
            var result = init;
            for (int i = index; i < end; i++) {
                result = f.apply(result, str.charAt(i));
                acc = Fn.cons(result, acc);
            }
            return acc.reverse();
        });
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
    public void run(Consumer<? super Character> proc) {
        for (int i = index; i < end; i++) {
            proc.accept(str.charAt(i));
        }
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
    public Optional<Character> max(Comparator<? super Character> comp) {
        var max = str.charAt(index);
        for (int i = index + 1; i < end; i++) {
            var next = str.charAt(i);
            max = comp.compare(max, next) > 0 ? max : next;
        }
        return Optional.of(max);
    }

    @Override
    public Character nth(int i) {
        if (i < 0 || i >= count) {
            throw new IndexOutOfBoundsException(i);
        }
        return str.charAt(i + index);
    }

    @Override
    public Character nth(int i, Character notFound) {
        if (i < 0 || i >= count) {
            return notFound;
        }
        return str.charAt(i + index);
    }

    @Override
    public int count() {
        return count;
    }

    @Override
    public ISeq<Character> reverse() {
        return new StringSeq(new StringBuilder(str.subSequence(index, end)).reverse().toString(), 0, end);
    }

    @Override
    public List<Character> toList() {
        var acc = new ArrayList<Character>();
        for (int i = index; i < end; i++) {
            acc.add(str.charAt(i));
        }
        return List.copyOf(acc);
    }

    @Override
    public Set<Character> toSet() {
        return Set.copyOf(toList());
    }

    @Override
    public String toString() {
        return toList().toString();
    }
}
