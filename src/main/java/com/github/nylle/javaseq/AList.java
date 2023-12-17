package com.github.nylle.javaseq;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public abstract class AList<T> implements List<T> {

    private volatile List<T> cached;

    private List<T> reify() {
        if (cached == null) {
            synchronized (this) {
                if (cached == null) {
                    cached = toList();
                }
            }
        }
        return cached;
    }

    abstract List<T> toList();

    public abstract int size();

    public abstract boolean isEmpty();

    public abstract Iterator<T> iterator();

    public abstract T get(int index);


    // java.util.List

    public T set(int index, T element){
        throw new UnsupportedOperationException();
    }

    public boolean add(T t) {
        throw new UnsupportedOperationException();
    }

    public void add(int index, T element) {
        throw new UnsupportedOperationException();
    }

    public boolean addAll(Collection<? extends T> c) {
        throw new UnsupportedOperationException();
    }

    public boolean addAll(int index, Collection<? extends T> c) {
        throw new UnsupportedOperationException();
    }

    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    public T remove(int index){
        throw new UnsupportedOperationException();
    }

    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    public void replaceAll(UnaryOperator<T> operator) {
        throw new UnsupportedOperationException();
    }

    public void sort(Comparator<? super T> c) {
        throw new UnsupportedOperationException();
    }

    public void clear() {
        throw new UnsupportedOperationException();
    }

    public List<T> subList(int fromIndex, int toIndex){
        return reify().subList(fromIndex, toIndex);
    }

    public boolean containsAll(Collection<?> c) {
        return reify().containsAll(c);
    }

    public int indexOf(Object o){
        return reify().indexOf(o);
    }

    public int lastIndexOf(Object o) {
        return reify().lastIndexOf(o);
    }

    public ListIterator<T> listIterator() {
        return reify().listIterator();
    }

    public ListIterator<T> listIterator(int index) {
        return reify().listIterator(index);
    }

    public Object[] toArray() {
        Object[] r = new Object[size()];
        Iterator<T> it = iterator();
        for (int i = 0; i < r.length; i++) {
            if (! it.hasNext())
                return Arrays.copyOf(r, i);
            r[i] = it.next();
        }
        return it.hasNext() ? finishToArray(r, it) : r;
    }

    public <U> U[] toArray(U[] a) {
        int size = size();
        U[] r = a.length >= size ? a : (U[])java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size);
        Iterator<T> it = iterator();

        for (int i = 0; i < r.length; i++) {
            if (! it.hasNext()) {
                if (a == r) {
                    r[i] = null;
                } else if (a.length < i) {
                    return Arrays.copyOf(r, i);
                } else {
                    System.arraycopy(r, 0, a, 0, i);
                    if (a.length > i) {
                        a[i] = null;
                    }
                }
                return a;
            }
            r[i] = (U)it.next();
        }
        return it.hasNext() ? finishToArray(r, it) : r;
    }

    private static final int SOFT_MAX_ARRAY_LENGTH = Integer.MAX_VALUE - 8;

    @SuppressWarnings("unchecked")
    private static <T> T[] finishToArray(T[] r, Iterator<?> it) {
        int len = r.length;
        int i = len;
        while (it.hasNext()) {
            if (i == len) {
                len = newLength(len, 1, (len >> 1) + 1);
                r = Arrays.copyOf(r, len);
            }
            r[i++] = (T)it.next();
        }
        return (i == len) ? r : Arrays.copyOf(r, i);
    }

    private static int newLength(int oldLength, int minGrowth, int prefGrowth) {
        int prefLength = oldLength + Math.max(minGrowth, prefGrowth);
        if (0 < prefLength && prefLength <= SOFT_MAX_ARRAY_LENGTH) {
            return prefLength;
        } else {
            return hugeLength(oldLength, minGrowth);
        }
    }

    private static int hugeLength(int oldLength, int minGrowth) {
        int minLength = oldLength + minGrowth;
        if (minLength < 0) {
            throw new OutOfMemoryError("Required array length " + oldLength + " + " + minGrowth + " is too large");
        } else if (minLength <= SOFT_MAX_ARRAY_LENGTH) {
            return SOFT_MAX_ARRAY_LENGTH;
        } else {
            return minLength;
        }
    }


    // java.util.Collection

    public boolean contains(Object o){
        return reify().contains(o);
    }

    public boolean removeIf(Predicate<? super T> filter) {
        throw new UnsupportedOperationException();
    }

    public <U> U[] toArray(IntFunction<U[]> generator) {
        return toArray(generator.apply(0));
    }
}
