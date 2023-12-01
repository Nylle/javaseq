package com.github.nylle.javaseq.prototype;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface Seq<T> extends List<T> {

    static <T> Seq<T> of() {
        return Nil.of();
    }

    @SafeVarargs
    static <T> Seq<T> of(T... xs) {
        return Seq.sequence(Arrays.asList(xs).iterator());
    }

    static <T> Seq<T> sequence(T[] coll) {
        if (coll == null) {
            return Nil.of();
        }
        return Seq.sequence(Arrays.asList(coll).iterator());
    }

    static <T> Seq<T> sequence(Iterable<T> coll) {
        if (coll == null) {
            return Nil.of();
        }
        if (coll instanceof Seq<T>) {
            return (Seq<T>) coll;
        }
        return Seq.sequence(coll.iterator());
    }

    static <T> Seq<T> sequence(Stream<T> coll) {
        if (coll == null) {
            return Nil.of();
        }
        return Seq.sequence(coll.iterator());
    }

    static <T> Seq<T> sequence(Iterator<T> coll) {
        if (coll == null) {
            return Nil.of();
        }
        return coll.hasNext() ? Cons.of(coll.next(), () -> Seq.sequence(coll)) : Seq.of();
    }

    static <K, V> Seq<Map.Entry<K, V>> sequence(Map<K, V> coll) {
        return Seq.sequence(coll.entrySet().iterator());
    }

    static <T> Seq<T> iterate(T x, UnaryOperator<T> f) {
        return Cons.of(x, () -> iterate(f.apply(x), f));
    }

    @SafeVarargs
    static <T> Seq<T> concat(Iterable<T>... colls) {
        return Seq.of(colls).mapcat(x -> x);
    }

    static Seq<Integer> range() {
        return iterate(0, x -> x + 1);
    }

    static Seq<Integer> range(int end) {
        return range(0, end);
    }

    static Seq<Integer> range(int start, int end) {
        return range(start, end, 1);
    }

    static Seq<Integer> range(int start, int end, int step) {
        return iterate(start, x -> x + step).takeWhile(x -> step >= 0 ? (x < end) : (x > end));
    }

    T first();

    Seq<T> rest();

    boolean isRealized();

    Seq<T> take(long n);

    Seq<T> drop(long n);

    Seq<T> filter(Predicate<? super T> pred);

    <R> Seq<R> map(Function<? super T, ? extends R> f);

    <S, R> Seq<R> map(Seq<? extends S> other, BiFunction<? super T, ? super S, ? extends R> f);

    <S, U, R> Seq<R> map(Seq<? extends S> other1, Seq<? extends U> other2, TriFunction<? super T, ? super S, ? super U, ? extends R> f);

    <R> Seq<R> mapcat(Function<? super T, ? extends Iterable<? extends R>> f);

    Seq<T> takeWhile(Predicate<? super T> pred);

    Seq<T> dropWhile(Predicate<? super T> pred);

    Seq<List<T>> partition(int n);

    Seq<List<T>> partition(int n, int step);

    Seq<List<T>> partition(int n, int step, Iterable<T> pad);

    Seq<List<T>> partitionAll(int n);

    Seq<List<T>> partitionAll(int n, int step);

    Seq<T> reductions(BinaryOperator<T> f);

    Seq<T> reductions(T init, BinaryOperator<T> f);

    Seq<T> cons(T x);

    Optional<T> reduce(BinaryOperator<T> f);

    <R> R reduce(R val, BiFunction<R, ? super T, R> f);

    Seq<T> distinct();

    Seq<T> sorted();

    Seq<T> sorted(Comparator<? super T> comp);

    boolean some(Predicate<? super T> pred);

    boolean every(Predicate<? super T> pred);

    boolean notAny(Predicate<? super T> pred);

    Optional<T> max(Comparator<? super T> comp);

    Optional<T> min(Comparator<? super T> comp);

    <C extends Comparable<? super C>> Optional<T> maxKey(Function<T, C> f);

    <C extends Comparable<? super C>> Optional<T> minKey(Function<T, C> f);

    Optional<T> find(int i);

    Optional<T> findFirst();

    Optional<T> findFirst(Predicate<? super T> pred);

    <K, V> Map<K, V> toMap(Function<T, K> k, Function<T, V> v);

    <K, V> Map<K, V> toMap(Function<T, K> k, Function<T, V> v, BinaryOperator<V> m);

    <K, V> Map<K, V> toMap();

    List<T> toList();

    @SuppressWarnings("java:S1700")
    class Nil<T> extends AbstractList<T> implements Seq<T> {
        private static final Nil<?> nil = new Nil<>();

        @SuppressWarnings("unchecked")
        public static <T> Nil<T> of() {
            return (Nil<T>) nil;
        }

        @Override
        public T first() {
            return null;
        }

        @Override
        public Seq<T> rest() {
            return of();
        }

        @Override
        public boolean isRealized() {
            return true;
        }

        @Override
        public Seq<T> take(long n) {
            return of();
        }

        @Override
        public Seq<T> drop(long n) {
            return of();
        }

        @Override
        public Seq<T> filter(Predicate<? super T> pred) {
            return of();
        }

        @Override
        public <R> Seq<R> map(Function<? super T, ? extends R> f) {
            return of();
        }

        @Override
        public <U, R> Seq<R> map(Seq<? extends U> other, BiFunction<? super T, ? super U, ? extends R> f) {
            return of();
        }

        @Override
        public <U, V, R> Seq<R> map(Seq<? extends U> other1, Seq<? extends V> other2, TriFunction<? super T, ? super U, ? super V, ? extends R> f) {
            return of();
        }

        @Override
        public <R> Seq<R> mapcat(Function<? super T, ? extends Iterable<? extends R>> f) {
            return of();
        }

        @Override
        public Seq<T> takeWhile(Predicate<? super T> pred) {
            return of();
        }

        @Override
        public Seq<T> dropWhile(Predicate<? super T> pred) {
            return of();
        }

        @Override
        public Seq<List<T>> partition(int n) {
            return partition(n, n);
        }

        @Override
        public Seq<List<T>> partition(int n, int step) {
            return of();
        }

        @Override
        public Seq<List<T>> partition(int n, int step, Iterable<T> pad) {
            return of();
        }

        @Override
        public Seq<List<T>> partitionAll(int n) {
            return of();
        }

        @Override
        public Seq<List<T>> partitionAll(int n, int step) {
            return of();
        }

        @Override
        public Seq<T> reductions(BinaryOperator<T> f) {
            return of();
        }

        @Override
        public Seq<T> reductions(T init, BinaryOperator<T> f) {
            return Seq.of(init);
        }

        @Override
        public Seq<T> cons(T x) {
            return Cons.of(x, () -> this);
        }

        @Override
        public Optional<T> reduce(BinaryOperator<T> f) {
            return Optional.empty();
        }

        @Override
        public <R> R reduce(R val, BiFunction<R, ? super T, R> f) {
            return val;
        }

        @Override
        public Seq<T> distinct() {
            return of();
        }

        @Override
        public Seq<T> sorted() {
            return of();
        }

        @Override
        public Seq<T> sorted(Comparator<? super T> comp) {
            return of();
        }

        @Override
        public boolean some(Predicate<? super T> pred) {
            return false;
        }

        @Override
        public boolean every(Predicate<? super T> pred) {
            return true;
        }

        @Override
        public boolean notAny(Predicate<? super T> pred) {
            return true;
        }

        @Override
        public Optional<T> max(Comparator<? super T> comp) {
            return Optional.empty();
        }

        @Override
        public Optional<T> min(Comparator<? super T> comp) {
            return Optional.empty();
        }

        @Override
        public <C extends Comparable<? super C>> Optional<T> maxKey(Function<T, C> f) {
            return Optional.empty();
        }

        @Override
        public <C extends Comparable<? super C>> Optional<T> minKey(Function<T, C> f) {
            return Optional.empty();
        }

        @Override
        public Optional<T> find(int i) {
            return Optional.empty();
        }

        @Override
        public Optional<T> findFirst() {
            return Optional.empty();
        }

        @Override
        public Optional<T> findFirst(Predicate<? super T> pred) {
            return Optional.empty();
        }

        @Override
        public <K, V> Map<K, V> toMap(Function<T, K> k, Function<T, V> v) {
            return Map.of();
        }

        @Override
        public <K, V> Map<K, V> toMap(Function<T, K> k, Function<T, V> v, BinaryOperator<V> m) {
            return Map.of();
        }

        @Override
        public <K, V> Map<K, V> toMap() {
            return Map.of();
        }

        @Override
        public List<T> toList() {
            return List.of();
        }

        @Override
        public T get(int index) {
            return null;
        }

        @Override
        public List<T> subList(int fromIndex, int toIndex) {
            return List.of();
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Seq.Nil;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public String toString() {
            return "[]";
        }
    }

    @SuppressWarnings("java:S3077")
    class Cons<T> extends AbstractList<T> implements Seq<T> {
        private final T first;
        private volatile Seq<T> rest;
        private final Supplier<Seq<T>> f;

        private Cons(T first, Supplier<Seq<T>> f) {
            this.first = first;
            this.f = f;
        }

        public static <T> Cons<T> of(T first, Supplier<Seq<T>> f) {
            return new Cons<>(first, f);
        }

        @Override
        public T first() {
            return first;
        }

        @Override
        public Seq<T> rest() {
            if (!isRealized()) {
                synchronized (this) {
                    if (!isRealized()) {
                        rest = f.get();
                    }
                }
            }
            return rest;
        }

        @Override
        public boolean isRealized() {
            return rest != null;
        }

        @Override
        public Seq<T> take(long n) {
            return n <= 0 ? Nil.of() : Cons.of(first, () -> rest().take(n - 1));
        }

        @Override
        public Seq<T> drop(long n) {
            return n > 0 ? rest().drop(n - 1) : this;
        }

        @Override
        public Seq<T> filter(Predicate<? super T> pred) {
            return pred.test(first) ? Cons.of(first, () -> rest().filter(pred)) : rest().filter(pred);
        }

        @Override
        public <R> Seq<R> map(Function<? super T, ? extends R> f) {
            return Cons.of(f.apply(first()), () -> rest().map(f));
        }

        @Override
        public <U, R> Seq<R> map(Seq<? extends U> other, BiFunction<? super T, ? super U, ? extends R> f) {
            return other.isEmpty()
                    ? Nil.of()
                    : Cons.of(f.apply(first(), other.first()), () -> rest().map(other.rest(), f));
        }

        @Override
        public <U, V, R> Seq<R> map(Seq<? extends U> other1, Seq<? extends V> other2, TriFunction<? super T, ? super U, ? super V, ? extends R> f) {
            return other1.isEmpty() || other2.isEmpty()
                    ? Nil.of()
                    : Cons.of(f.apply(first(), other1.first(), other2.first()), () -> rest().map(other1.rest(), other2.rest(), f));
        }

        @Override
        public <R> Seq<R> mapcat(Function<? super T, ? extends Iterable<? extends R>> f) {
            return concat(Seq.sequence(f.apply(first)).map(x -> x), () -> rest().mapcat(f));
        }

        @Override
        public Seq<T> takeWhile(Predicate<? super T> pred) {
            return pred.test(first()) ? Cons.of(first(), () -> rest().takeWhile(pred)) : Nil.of();
        }

        @Override
        public Seq<T> dropWhile(Predicate<? super T> pred) {
            return pred.test(first()) ? rest().dropWhile(pred) : this;
        }

        @Override
        public Seq<List<T>> partition(int n) {
            return partition(n, n);
        }

        @Override
        public Seq<List<T>> partition(int n, int step) {
            return partition(n, step, null);
        }

        @Override
        public Seq<List<T>> partition(int n, int step, Iterable<T> pad) {
            if (n < 0) {
                return Nil.of();
            }
            var partition = take(n).toList();
            if (partition.size() < n) {
                return pad == null
                        ? Nil.of()
                        : Cons.of(pad(partition, pad, n), () -> drop(step).partition(n, step, pad));
            }
            return Cons.of(partition, () -> drop(step).partition(n, step, pad));
        }

        @Override
        public Seq<List<T>> partitionAll(int n) {
            return partition(n, n, List.of());
        }

        @Override
        public Seq<List<T>> partitionAll(int n, int step) {
            return partition(n, step, List.of());
        }

        @Override
        public Seq<T> reductions(BinaryOperator<T> f) {
            return rest().reductions(first(), f);
        }

        @Override
        public Seq<T> reductions(T init, BinaryOperator<T> f) {
            return Cons.of(init, () -> rest().reductions(f.apply(init, first()), f));
        }

        @Override
        public Seq<T> cons(T x) {
            return Cons.of(x, () -> this);
        }

        @Override
        public Optional<T> reduce(BinaryOperator<T> f) {
            return Optional.of(rest().reduce(first(), f));
        }

        @Override
        public <R> R reduce(R val, BiFunction<R, ? super T, R> f) {
            var acc = val;
            Seq<T> seq = this;
            while (!seq.isEmpty()) {
                acc = f.apply(acc, seq.first());
                seq = seq.rest();
            }
            return acc;
        }

        @Override
        public Seq<T> distinct() {
            return distinct(this, new HashSet<>());
        }

        @Override
        @SuppressWarnings("unchecked")
        public Seq<T> sorted() {
            return sorted((a, b) -> ((Comparable<T>) a).compareTo(b));
        }

        @Override
        public Seq<T> sorted(Comparator<? super T> comp) {
            var list = new ArrayList<>(this);
            list.sort(comp);
            return Seq.sequence(list);
        }

        @Override
        public boolean some(Predicate<? super T> pred) {
            return pred.test(first()) || rest().some(pred);
        }

        @Override
        public boolean every(Predicate<? super T> pred) {
            return pred.test(first()) && rest().every(pred);
        }

        @Override
        public boolean notAny(Predicate<? super T> pred) {
            return every(pred.negate());
        }

        @Override
        public Optional<T> max(Comparator<? super T> comp) {
            if (rest().isEmpty()) {
                return Optional.of(first());
            }
            var result = first();
            var current = this.rest();
            while (!current.isEmpty()) {
                var next = current.first();
                result = comp.compare(result, next) > 0 ? result : next;
                current = current.rest();
            }
            return Optional.of(result);
        }

        @Override
        public Optional<T> min(Comparator<? super T> comp) {
            return max(comp.reversed());
        }

        @Override
        public <C extends Comparable<? super C>> Optional<T> maxKey(Function<T, C> f) {
            return max(Comparator.comparing(t -> f.apply(t)));
        }

        @Override
        public <C extends Comparable<? super C>> Optional<T> minKey(Function<T, C> f) {
            return min(Comparator.comparing(t -> f.apply(t)));
        }

        @Override
        public Optional<T> find(int i) {
            return Optional.ofNullable(get(i));
        }

        @Override
        public Optional<T> findFirst() {
            return Optional.ofNullable(first());
        }

        @Override
        public Optional<T> findFirst(Predicate<? super T> pred) {
            return filter(pred).findFirst();
        }

        @Override
        public <K, V> Map<K, V> toMap(Function<T, K> k, Function<T, V> v) {
            return Map.ofEntries(map(x -> Map.entry(k.apply(x), v.apply(x))).toArray(new Map.Entry[0]));
        }

        @Override
        public <K, V> Map<K, V> toMap(Function<T, K> k, Function<T, V> v, BinaryOperator<V> m) {
            var entries = this
                    .map(x -> Map.entry(k.apply(x), v.apply(x)))
                    .reduce(
                            Seq.<Map.Entry<K, V>>of(),
                            (acc, x) -> acc
                                    .filter(y -> !x.getKey().equals(y.getKey()))
                                    .cons(acc.findFirst(y -> y.getKey().equals(x.getKey())).map(y -> Map.entry(x.getKey(), m.apply(y.getValue(), x.getValue()))).orElse(x)));

            return Map.ofEntries(entries.toArray(new Map.Entry[0]));
        }

        @Override
        public <K, V> Map<K, V> toMap() {
            if (first instanceof Map.Entry<?, ?>) {
                return toMap(k -> ((Map.Entry<K, V>) k).getKey(), v -> ((Map.Entry<K, V>) v).getValue(), (a, b) -> b);
            }
            throw new UnsupportedOperationException("Seq is not of type Map.Entry. Provide key- and value-mappers.");
        }

        @Override
        public void forEach(Consumer<? super T> f) {
            f.accept(first());
            rest().forEach(f);
        }

        @Override
        public List<T> toList() {
            rest().toList();
            return List.copyOf(this);
        }

        @Override
        public T get(int index) {
            if (index < 0) {
                return null;
            }
            Seq<T> current = this;
            for (int i = index; i > 0; --i) {
                if (current.rest().isEmpty()) {
                    return null;
                }
                current = current.rest();
            }
            return current.first();
        }

        @Override
        public int size() {
            return 1 + rest().size();
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public Iterator<T> iterator() {
            var seq = this;
            return new Iterator<>() {
                private Seq<T> source = seq;

                @Override
                public boolean hasNext() {
                    return !source.isEmpty();
                }

                @Override
                public T next() {
                    var next = source.first();
                    source = source.rest();
                    if (next == null) {
                        throw new NoSuchElementException();
                    }
                    return next;
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException("remove");
                }

                @Override
                public void forEachRemaining(Consumer<? super T> f) {
                    source.forEach(f);
                }
            };
        }

        @Override
        public Stream<T> stream() {
            return StreamSupport.stream(((Iterable<T>) () -> iterator()).spliterator(), false);
        }

        @Override
        public Stream<T> parallelStream() {
            return stream();
        }

        @Override
        public List<T> subList(int fromIndex, int toIndex) {
            return drop(fromIndex).take((long) toIndex - fromIndex);
        }

        @Override
        @SuppressWarnings("java:S3740")
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Seq other)) return false;
            return !other.isEmpty() && first().equals(other.first()) && rest().equals(other.rest());
        }

        @Override
        public int hashCode() {
            return first().hashCode() + rest().hashCode() * 31;
        }

        @Override
        public String toString() {
            StringBuilder s = new StringBuilder("[");
            Seq<T> seq = this;
            while (!seq.isEmpty()) {
                s.append(seq.first());
                if (seq.isRealized()) {
                    if (!seq.rest().isEmpty()) {
                        s.append(", ");
                    }
                    seq = seq.rest();
                } else {
                    s.append(", ").append("?");
                    break;
                }
            }
            return s.append("]").toString();
        }

        private static <T> List<T> pad(List<T> partition, Iterable<T> pad, int n) {
            return concat(partition, () -> Seq.sequence(pad).take(n - (long) partition.size())).toList();
        }

        private static <T> Seq<T> distinct(Seq<T> seq, Set<T> exclude) {
            var additionalItems = seq.filter(t -> !exclude.contains(t));
            if (additionalItems.isEmpty()) {
                return Seq.of();
            }
            exclude.add(additionalItems.first());
            return Cons.of(additionalItems.first(), () -> distinct(additionalItems.rest(), exclude));
        }

        private static <T> Seq<T> concat(Iterable<T> coll, Supplier<Seq<T>> f) {
            return Cons.concat(coll.iterator(), f);
        }

        private static <T> Seq<T> concat(Iterator<T> coll, Supplier<Seq<T>> f) {
            if (coll.hasNext()) {
                T next = coll.next();
                return coll.hasNext() ? Cons.of(next, () -> Cons.concat(coll, f)) : Cons.of(next, f);
            }
            return f.get();
        }
    }

    class Extensions {

        private Extensions() {
        }

        public static <T> Seq<T> toSeq(Stream<T> stream) {
            return Seq.sequence(stream);
        }

        public static <K, V> Seq<Map.Entry<K, V>> toSeq(Map<K, V> map) {
            return Seq.sequence(map);
        }

        public static <T> Seq<T> toSeq(Iterable<T> coll) {
            return Seq.sequence(coll);
        }

        public static <T> Seq<T> toSeq(Iterator<T> coll) {
            return Seq.sequence(coll);
        }

        public static <T> Seq<T> toSeq(T[] coll) {
            return Seq.sequence(coll);
        }
    }

    @FunctionalInterface
    interface TriFunction<A, B, C, R> {
        R apply(A a, B b, C c);
    }
}
