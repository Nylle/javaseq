[![Tests](https://github.com/Nylle/javaseq/workflows/test/badge.svg)](https://github.com/Nylle/JavaFixture/actions?query=workflow%3ATest)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.nylle/javaseq.svg?label=maven-central)](https://maven-badges.herokuapp.com/maven-central/com.github.nylle/javaseq)
[![MIT license](http://img.shields.io/badge/license-MIT-brightgreen.svg?style=flat)](http://opensource.org/licenses/MIT)

# JavaSeq

A lazy, immutable, persistent sequence for Java

## This Is An Exercise

The goal was to build an alternative to Java's Stream API and provide missing features. However, it turned out to be a truly persistent, immutable data-structure with all its advantages and disadvantages. API, usage, and implementation are heavily influenced by Clojure. 

The performance is not comparable to Streams, but depending on the usage (source, size, manipulations), it performs between ArrayList and LinkedList, especially larger collections (> 1000 items) perform surprisingly better than LinkedList.

Feel free to copy any of this code. If you find yourself using this in your Java-project a lot, chances are you're using the wrong programming-language. Take a look at [Clojure](https://clojure.org/) if you want fast, immutable, persistent data structures.

<details>
<summary>Getting started</summary>

```xml
<dependency>
    <groupId>com.github.nylle</groupId>
    <artifactId>javaseq</artifactId>
    <version>0.0.2</version>
</dependency>
```

</details>

<details>
<summary>Usage</summary>

- [Creation](#creation)
- [Operations](#operations)
- [Interoperability](#interoperability)
- [Extensions](#extensions)

</details>

## Background

I've always had a difficult relationship with Java's Streams. After a decade working with C# (and [LINQ](https://en.wikipedia.org/wiki/Language_Integrated_Query)), Streams were the single feature that convinced me to try out Java -- and another decade later it's one of the features driving me away; towards languages like Kotlin or Clojure.

### Streams Are Cumbersome

- While functional features like **filter**, **map**, and **reduce** are built in for any collection (lazy or not) in other languages, in Java one only gets access if the collection at hand is turned into a Stream by calling `.stream()`.
- At least, this is built-in with Java's `Collection`-interface, but turning an array into a Stream requires the use of static helper-classes like `Arrays`.
- In the end, the contents of the Stream have to be turned back into a collection of your choosing, by calling `.collect()`.
- In order to do so, one has to deal with additional static helper-classes, so-called `Collectors` to be passed to the `.collect()`-method. While recent Java-versions provide a convenient shortcut `.toList()`, creating a `Set` or `Map` still requires a collector, not to mention creating an array -- which is difficult due to Java's broken generics.

### Streams Are Not Re-usable

- Stream is **not** a data structure. The Stream-API is just a thin wrapper around `Iterator`, which provides out-of-the-box laziness.
- This means that once a Stream is consumed/reduced it cannot be read again, because the underlying iterator cannot be rewound.
- Instead, a full copy of the original data is created while both, the source and the result are mutable. 
- Anyone holding a reference to either can never rely on what they get.

### Features Are Missing

While Streams help to work with lazy collections by providing the aforementioned filter-map-reduce interface, features beyond those are not to be found, including but not limited to: 
- **zip** -- Map two potentially infinite collections by applying a bi-function f to the set of first items of each collection, followed by applying f to the set of second items in each collection and so forth, until any one of the collections is exhausted (see Clojure's [map](https://clojuredocs.org/clojure.core/map)-function).
- **partition** -- Split a potentially infinite collection into a collection of collections of fixed size. This allows for batching or creating a sliding window.
- **head/tail** -- Access the first and/or remaining items of a potentially infinite collection.
- caching in general

## Usage

**ISeq** is immutable, which means anyone holding a reference to it, will always get the same result. It is also persistent, which means dropping or adding an item will not return a full copy of the collection; instead, the previous, immutable state will be re-used in combination with the added item. Seqs are always being evaluated lazily, which allows for infinite sequences and handling very large amounts of data. Their content is being cached, for it to be re-used and operated on as often as needed. Consuming an infinite Seq entirely, of course will run infinitely or until system resources are exhausted.

### Creation

#### ISeq.of()
- Returns an empty seq.

#### ISeq.of(xs...)
- Returns a fully realized seq of all supplied xs.

#### ISeq.cons(x, coll)
- Returns a seq where x is the first element and coll is the rest.

#### ISeq.lazySeq(body)
- Takes a body of expressions (supplier) that returns an ISeq or Nil that will invoke the body only the first time it is accessed, and will cache the result and return it on all subsequent calls (see [isRealized](#isrealized)).

#### ISeq.seq(coll)
- Coerces coll to a (possibly empty) seq, if it is not already one. Will not force a lazy Stream or Iterator. If coll is a String, the returned seq will contain items of type Character. If coll is a Map<K, V> the returned seq will contain items of type Map.Entry<K, V> (see [toMap](#tomap)). Yields empty seq if coll is null or empty.

#### ISeq.iterate(x, f)
- Returns a seq of x, f(x), f(f(x)) etc. f must be free of side effects.

#### ISeq.repeat(x)
- Returns a lazy (infinite!) seq of xs.

#### ISeq.repeat(n, x)
- Returns a lazy seq of xs with length n.

#### ISeq.range()
- Returns a seq of numbers from 0 (inclusive) to infinity, by step 1.

#### ISeq.range(end)
- Returns a seq of numbers from 0 (inclusive) to end (exclusive), by step 1.
- Returns empty seq when end is equal to 0.

#### ISeq.range(start, end)
- Returns a seq of numbers from start (inclusive) to end (exclusive), by step 1. 
- Returns empty seq when start is equal to end.

#### ISeq.range(start, end, step)
- Returns a seq of numbers from start (inclusive) to end (exclusive), by step. 
- Returns infinite seq of start when step is equal to 0.
- Returns empty seq when start is equal to end.

#### ISeq.concat(coll, x)
- Returns a lazy seq representing the concatenation of the items in coll and x.

#### ISeq.concat(colls...)
- Returns a lazy seq representing the concatenation of the items in colls.

### Operations

#### first()
- Returns the first item in this seq.
- Returns null if this seq is empty. _(See [findFirst](#findfirst).)_

#### second()
- Returns the second item in this seq.
- Returns null if this seq has less than two items.

#### rest()
- Returns a seq of the items in this seq after the first.

#### take(n)
- Returns a seq with the first n items in this seq.

#### drop(n)
- Returns a seq with all but the first n items in this seq.

#### filter(pred)
- Returns a seq of the items in this seq for which pred(item) returns true.

#### map(f)
- Returns a seq of the result of applying f to each item in this seq.

#### map(coll, f)
- Returns a seq of the result of applying f to the set of first items in both this seq and coll, followed by applying f to the set of second items in this seq and coll, until any one of the collections is exhausted. Any remaining items in either collection are ignored.
_(This is similar to zipping two collections.)_

#### mapcat(f)
- Returns a seq of the result of applying concat to the result of applying map to f and the items in this seq. Function f should return a collection. _(This is similar to [Stream::flatMap](https://docs.oracle.com/javase/8/docs/api/java/util/stream/Stream.html#flatMap-java.util.function.Function-).)_

#### mapcat(coll, f)
- Returns a seq of the result of applying concat to the result of applying map to f and coll and the items in this seq. Function f should return a collection. 

#### takeWhile(pred)
- Returns a seq of successive items from this seq while pred of item returns true.

#### dropWhile(pred)
- Returns a seq of the items in this seq starting from the first item for which pred of item returns false.

#### partition(n)
- Returns a seq of seqs of n items each.

#### partition(n, step)
- Returns a seq of seqs of n items each, at offsets step apart.

#### partition(n, step, pad)
- Returns a seq of seqs of n items each, at offsets step apart. The items in pad are used as necessary to complete the last partition up to n items. In case there are not enough padding items, returns a partition with less than n items.

#### partitionAll(n)
- Returns a seq of seqs of n items each, like [partition](#partitionn), but may include partitions with fewer than n items at the end.

#### partitionAll(n, step)
- Returns a seq of seqs of n items each, at offsets step apart, like [partition](#partitionn-step), but may include partitions with fewer than n items at the end.

### partitionBy(f)
- Applies f to each value in coll, splitting it each time f returns a new value. Returns a lazy seq of partitions.

#### reductions(f)
- Returns a seq of the intermediate values of the reduction (as per [reduce](#reducef)) of this seq by f.

#### reductions(init, f)
- Returns a seq of the intermediate values of the reduction (as per [reduce](#reduceval-f)) of this seq by f, starting with init.

#### cons(x)
- Returns a new seq where x is the first item and this seq is the rest.

#### concat(xs)
- Returns a seq representing the concatenation of the items in this seq and xs.

#### distinct()
- Returns a seq of the items of this seq with duplicates removed. **Caution:** The seq will be fully realized. If this seq is infinite, it will run infinitely or until system resources are exhausted.

#### sorted()
- Returns a seq of the items of this seq sorted by using compare. **Caution:** The seq will be fully realized. If this seq is infinite, it will run infinitely or until system resources are exhausted.

#### sorted(comp)
- Returns a seq of the items of this seq sorted by using supplied comparator comp. **Caution:** The seq will be fully realized. If this seq is infinite, it will run infinitely or until system resources are exhausted.

#### reverse()
- Returns a seq of the items of this seq in reversed order. **Caution:** The seq will be fully realized. If this seq is infinite, it will run infinitely or until system resources are exhausted.

#### reduce(f)
- Returns an empty Optional if this seq contains no items.
- Returns an Optional of the result of applying f to the first 2 items in this seq, then applying f to that result and the 3rd item, etc. If this seq has only 1 item, it is returned and f is not called. **Caution:** The seq will be fully realized. If this seq is infinite, it will run infinitely or until system resources are exhausted.

#### reduce(val, f)
- Returns val if this seq is empty. f is not called.
- Returns the result of applying f to val and the first item in this seq, then applying f to that result and the 2nd item, etc. **Caution:** The seq will be fully realized. If this seq is infinite, it will run infinitely or until system resources are exhausted.

#### run(proc)
- Calls proc(x) for each x in this seq, proc being a consumer function taking one argument and returning void. **Caution:** The seq will be fully realized. If this seq is infinite, it will run infinitely or until system resources are exhausted.

#### some(pred)
- Returns the first true value of pred(x) for any x in this seq. _(This is similar to Stream::anyMatch.)_
- Returns false if none of the items in this seq return true for pred(item). **Caution:** The seq will be fully realized. If this seq is infinite, it will run infinitely or until system resources are exhausted.

#### every(pred)
- Returns the first false value of pred(x) for any x in this seq. _(This is similar to Stream::allMatch.)_
- Returns true if all the items in this seq return true for pred(item). **Caution:** The seq will be fully realized. If this seq is infinite, it will run infinitely or until system resources are exhausted. 

#### notAny(pred)
- Returns false if (pred x) is true for any x in this seq. _(This is similar to Stream::noneMatch.)_
- Returns true if none of the items in this seq return true for pred(item). **Caution:** The seq will be fully realized. If this seq is infinite, it will run infinitely or until system resources are exhausted.

#### isRealized()
- Returns true if a value has been produced for this seq.

#### max(comp)
- Returns the item in this seq for which comp determines is greatest. If there are multiple such items, the last one is returned. **Caution:** The seq will be fully realized. If this seq is infinite, it will run infinitely or until system resources are exhausted.

#### min(comp)
- Returns the item in this seq for which comp determines is least. If there are multiple such items, the last one is returned. **Caution:** The seq will be fully realized. If this seq is infinite, it will run infinitely or until system resources are exhausted.

#### maxKey(f)
- Returns the item x in this seq for which f(x), a number, is greatest. If there are multiple such xs, the last one is returned. **Caution:** The seq will be fully realized. If this seq is infinite, it will run infinitely or until system resources are exhausted.

#### minKey(f)
- Returns the item x in this seq for which f(x), a number, is least. If there are multiple such xs, the last one is returned. **Caution:** The seq will be fully realized. If this seq is infinite, it will run infinitely or until system resources are exhausted.

#### nth(index)
- Returns the item at index in this seq. All items up to that point will be realized. _(See [find](#findi).)_
- Throws if index is out of bounds. **Caution:** The seq will be fully realized. If this seq is infinite, it will run infinitely or until system resources are exhausted.

#### nth(index, notFound)
- Returns the item at index in this seq. All items up to that point will be realized. _(See [find](#findi).)_
- Returns notFound if index is out of bounds. **Caution:** The seq will be fully realized. If this seq is infinite, it will run infinitely or until system resources are exhausted.

#### find(index)
- Returns an Optional of the item at index in this seq. All items up to that point will be realized. _(See [get](#listgetindex).)_
- Returns an empty Optional if this seq does not contain any item at index. **Caution:** The seq will be fully realized. If this seq is infinite, it will run infinitely or until system resources are exhausted.

#### findFirst()
- Returns an empty Optional if this seq contains no items.
- Returns an Optional of the first item in this seq. _(See [first](#first).)_

#### findFirst(pred)
- Returns an Optional of the first item in this seq for which pred(item) returns true.
- Returns an empty Optional if pred(item) returns false for all items in this seq. **Caution:** The seq will be fully realized. If this seq is infinite, it will run infinitely or until system resources are exhausted.

#### frequencies()
- Returns a seq of Map.Entry from distinct items in this seq to the number of times they appear. **Caution:** The seq will be fully realized. If this seq is infinite, it will run infinitely or until system resources are exhausted.

#### toMap()
- Returns a new Map with the keys and values of the items in this seq<Map.Entry>. Keeps last value on key-collision. If the seq is of any other type, an exception is thrown. **Caution:** The seq will be fully realized. If this seq is infinite, it will run infinitely or until system resources are exhausted.

#### toMap(k, v)
- Returns a new Map with keys as a result of k(x) and values as a result of v(x) of all xs in this seq. Throws on key-collision. **Caution:** The seq will be fully realized. If this seq is infinite, it will run infinitely or until system resources are exhausted.

#### toMap(k, v, m)
- Returns a new Map with keys as a result of k(x) and values as a result of v(x) of all xs in this seq, using m(v(x1), v(x2)) to resolve key-collision. **Caution:** The seq will be fully realized. If this seq is infinite, it will run infinitely or until system resources are exhausted.

#### reify()
- Returns a List with all items in this seq. **Caution:** The seq will be fully realized. If this seq is infinite, it will run infinitely or until system resources are exhausted.

#### str()
- Returns an empty string if the seq is empty.
- Returns the concatenation of x.toString() of all items x in this seq. **Caution:** The seq will be fully realized. If this seq is infinite, it will run infinitely or until system resources are exhausted.

### Interoperability

#### Object::toString()
- Returns the string representation of this seq, e.g. "[1, 2, 3]". Only the first and any other realized item will be included. Unrealized remaining items will be depicted with a single question mark, e.g. "[1, 2, ?]". If this seq is empty, only brackets will be returned, e.g. "[]".

#### Iterable::forEach(action)
- Calls action(x) for each x in this seq, action being a consumer function taking one argument and returning void. **Caution:** The seq will be fully realized. If this seq is infinite, it will run infinitely or until system resources are exhausted.

#### Collection::stream()
- Returns a Stream of the items in this seq.

#### Collection::parallelStream()
- Returns a Stream of the items in this seq.

#### List::get(index)
- Returns the item at index in this seq. All items up to that point will be realized. _(See [find](#findi).)_
- Throws if index is out of bounds. **Caution:** The seq will be fully realized. If this seq is infinite, it will run infinitely or until system resources are exhausted.

#### List::iterator()
- Returns an Iterator of the items in this seq.

#### List::isEmpty()
- Returns true if this seq is empty, otherwise false.

#### List::size()
- Returns the number of items in this seq. **Caution:** The seq will be fully realized. If this seq is infinite, it will run infinitely or until system resources are exhausted.

#### List::subList(fromIndex, toIndex)
- Returns a new List of items from this seq between fromIndex (inclusive) and toIndex (exclusive).

### Lombok Extensions

If you're using [Lombok](https://projectlombok.org/) you can add `@ExtensionMethods({SeqExtensions.class})` to your class in order to access the extension methods below.

#### Iterable::toSeq()
- Returns a seq of the items in Iterable.

#### Iterator::toSeq()
- Returns a seq of the items in Iterator.

#### Object[]::toSeq()
- Returns a seq of the items in array.

#### Stream::toSeq()
- Returns a seq of the items in Stream.

#### Map::toSeq()
- Returns a seq of the key-value-pairs in Map.

#### String::toSeq()
- Returns a seq of the characters in String.
