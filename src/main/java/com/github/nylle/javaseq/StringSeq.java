package com.github.nylle.javaseq;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class StringSeq extends ASeq<Character> implements ISeq<Character> {

    private final CharSequence str;
    private final int index;
    private final int length;

    StringSeq(CharSequence str, int index) {
        if (str == null || str.isEmpty()) {
            throw new IllegalArgumentException("string is null or empty");
        }
        if (index >= str.length()) {
            throw new IllegalArgumentException("index '" + index + "' is out of range for string '" + str + "'");
        }
        this.str = str;
        this.index = index;
        this.length = str.length();
    }

    @Override
    public Character first() {
        return str.charAt(index);
    }

    @Override
    public ISeq<Character> rest() {
        return index + 1 < length ? new StringSeq(str, index + 1) : ISeq.of();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean isRealized() {
        return true;
    }

    @Override
    public ISeq<Character> filter(Predicate<? super Character> pred) {
        return walk(pred, false);
    }

    @Override
    public <R> ISeq<R> map(Function<? super Character, ? extends R> f) {
        return Fn.lazySeq(() -> {
            var acc = ISeq.<R>of();
            var start = length - 1;
            for (int i = start; i >= index; i--) {
                acc = Fn.cons(f.apply(str.charAt(i)), acc);
            }
            return acc;
        });
    }

    @Override
    public ISeq<Character> cons(Character x) {
        return new StringSeq(x.toString() + str.subSequence(index, length), 0);
    }

    @Override
    public ISeq<Character> take(long n) {
        return Fn.lazySeq(() -> {
            if (n <= 0) {
                return ISeq.of();
            }
            int end = (int) n + index;
            if (end >= length) {
                return this;
            }
            return new StringSeq(str.subSequence(index, end), 0);
        });
    }

    @Override
    public ISeq<Character> drop(long n) {
        return Fn.lazySeq(() -> {
            if (n <= 0) {
                return this;
            }
            int end = (int) n + index;
            if (end >= length) {
                return ISeq.of();
            }
            return new StringSeq(str, end);
        });
    }

    @Override
    public ISeq<Character> takeWhile(Predicate<? super Character> pred) {
        return walk(pred, true);
    }

    private ISeq<Character> walk(Predicate<? super Character> pred, boolean stop) {
        return Fn.lazySeq(() -> {
            var acc = new StringBuilder();
            for (int i = index; i < length; i++) {
                var next = str.charAt(i);
                if (pred.test(next)) {
                    acc.append(next);
                } else if (stop) {
                    break;
                }
            }
            if (!acc.isEmpty()) {
                return new StringSeq(acc.toString(), 0);
            }
            return ISeq.of();
        });
    }

    @Override
    public ISeq<Character> dropWhile(Predicate<? super Character> pred) {
        return Fn.lazySeq(() -> {
            var end = length-1;
            for (int i = index; i < length; i++) {
                if (pred.test(str.charAt(i))) {
                    continue;
                }
                end = i;
                break;
            }
            if (end < length - 1) {
                return new StringSeq(str, end);
            }
            return ISeq.of();
        });
    }

    @Override
    public <U> ISeq<U> reductions(U init, BiFunction<U, ? super Character, U> f) {
        return Fn.lazySeq(() -> {
            var acc = ISeq.of(init);
            var result = init;
            for (int i = index; i < length; i++) {
                result = f.apply(result, str.charAt(i));
                acc = Fn.cons(result, acc);
            }
            return acc.reverse();
        });
    }

    @Override
    public <U> U reduce(U val, BiFunction<U, ? super Character, U> f) {
        var result = val;
        for (int i = index; i < length; i++) {
            result = f.apply(result, str.charAt(i));
        }
        return result;
    }

    @Override
    public void run(Consumer<? super Character> proc) {
        for (int i = index; i < length; i++) {
            proc.accept(str.charAt(i));
        }
    }

    @Override
    public ISeq<Character> distinct() {
        var seen = Set.of();
        var acc = new StringBuilder();
        for (int i = index; i < length; i++) {
            var step = str.charAt(i);
            if(seen.contains(step)) {
                continue;
            }
            acc.append(step);
            seen = Fn.conj(seen, step);
        }
        if (!acc.isEmpty()) {
            return new StringSeq(acc.toString(), 0);
        }
        return ISeq.of();
    }

    @Override
    public boolean some(Predicate<? super Character> pred) {
        for(int i = index; i < length; i++) {
            if(pred.test(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean every(Predicate<? super Character> pred) {
        for(int i = index; i < length; i++) {
            if(!pred.test(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Optional<Character> max(Comparator<? super Character> comp) {
        var max = str.charAt(index);
        for(int i = index + 1; i < length; i++) {
            var next = str.charAt(i);
            max = comp.compare(max, next) > 0 ? max : next;
        }
        return Optional.of(max);
    }

    @Override
    public Character nth(int index, Character notFound) {
        if(index >= this.index && index < length) {
            return str.charAt(index);
        }
        return notFound;
    }

    @Override
    public ISeq<Character> reverse() {
        return new StringSeq(new StringBuilder(str.subSequence(index, length)).reverse().toString(), 0);
    }

    @Override
    public List<Character> toList() {
        var acc = new ArrayList<Character>();
        for(int i = index; i < length; i++) {
            acc.add(str.charAt(i));
        }
        return List.copyOf(acc);
    }

    @Override
    public Set<Character> toSet() {
        return Set.copyOf(toList());
    }

    @Override
    public int size() {
        return length;
    }

    @Override
    public Iterator<Character> iterator() {
        return toList().iterator();
    }

    @Override
    public String toString() {
        return toList().toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        StringSeq that = (StringSeq) o;
        return index == that.index && length == that.length && Objects.equals(str, that.str);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), str, index, length);
    }
}
