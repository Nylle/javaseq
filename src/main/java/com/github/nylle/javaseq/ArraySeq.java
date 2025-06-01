package com.github.nylle.javaseq;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ArraySeq<T> extends ASeq<T> implements ISeq<T> {

    private final T[] array;
    private final int index;
    private final int end;
    private final int count;

    @SafeVarargs
    public ArraySeq(T... items) {
        if (items == null || items.length == 0) {
            throw new IllegalArgumentException("items is null or empty");
        }
        this.array = items;
        this.index = 0;
        this.end = items.length;
        this.count = items.length;
    }

    ArraySeq(T[] array, int index, int end) {
        if (array == null || array.length == 0) {
            throw new IllegalArgumentException("array is null or empty");
        }
        if (index >= array.length) {
            throw new IllegalArgumentException("index " + index + " is out of range for array " + Arrays.toString(array));
        }
        if (end > array.length) {
            throw new IllegalArgumentException("end " + end + " is out of range for array " + Arrays.toString(array));
        }
        if (end <= index) {
            throw new IllegalArgumentException("end " + end + " must be greater than index " + index);
        }
        this.array = array;
        this.index = index;
        this.end = end;
        this.count = end - index;
    }

    @Override
    public T first() {
        return array[index];
    }

    public T last() {
        if (count < 1) {
            return null;
        }
        return array[end-1];
    }

    @Override
    public ISeq<T> rest() {
        if (count > 1) {
            return new ArraySeq<>(array, index + 1, end);
        }
        return ISeq.of();
    }

    @Override
    public boolean isRealized() {
        return true;
    }

    @Override
    public ISeq<T> take(long n) {
        if(n >= count) {
            return this;
        }
        if (n > 0) {
            return new ArraySeq<T>(array, index, (int) n + index);
        }
        return ISeq.of();
    }

    @Override
    public ISeq<T> drop(long n) {
        if (n >= count) {
            return ISeq.of();
        }
        if (n > 0) {
            return new ArraySeq<T>(array, (int) n + index, end);
        }
        return this;
    }

    @Override
    public ISeq<T> takeWhile(Predicate<? super T> pred) {
        var newEnd = index;
        for (int i = index; i < end; i++) {
            if (!pred.test(array[i])) {
                break;
            }
            newEnd++;
        }
        if (newEnd > index) {
            return new ArraySeq<>(array, index, newEnd);
        }
        return ISeq.of();
    }

    @Override
    public ISeq<T> dropWhile(Predicate<? super T> pred) {
        var newIndex = index;
        for (int i = index; i < end; i++) {
            if (!pred.test(array[i])) {
                break;
            }
            newIndex++;
        }
        if (newIndex == index) {
            return this;
        }
        if (newIndex < end) {
            return new ArraySeq<>(array, newIndex, end);
        }
        return ISeq.of();
    }

    @Override
    public <U> U reduce(U val, BiFunction<U, ? super T, U> f) {
        var result = val;
        for (int i = index; i < end; i++) {
            result = f.apply(result, array[i]);
        }
        return result;
    }

    @Override
    public boolean some(Predicate<? super T> pred) {
        for (int i = index; i < end; i++) {
            if (pred.test(array[i])) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean every(Predicate<? super T> pred) {
        for (int i = index; i < end; i++) {
            if (!pred.test(array[i])) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected T nth(int index, Supplier<T> notFound) {
        if (index < 0 || index >= count) {
            return notFound.get();
        }
        return array[index + this.index];
    }

    @Override
    public int count() {
        return count;
    }

    @Override
    public Object[] toArray() {
        var result = new Object[count];
        System.arraycopy(this.array, index, result, 0, count);
        return result;
    }

    @Override
    public int indexOf(Object o) {
        for (int i = index; i < end; i++) {
            if (Objects.equals(o, array[i])) {
                return i - index;
            }
        }
        return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        if (o == null) {
            for (int i = end - 1; i >= index; i--) {
                if (array[i] == null) {
                    return i - index;
                }
            }
        } else {
            for (int i = end - 1; i >= index; i--) {
                if (o.equals(array[i])) {
                    return i - index;
                }
            }
        }
        return -1;
    }
}
