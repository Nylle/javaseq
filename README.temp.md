# Seq

## This Is An Exercise

The goal of this exercise was to build an alternative to Java's Stream API, heavily influenced by Rich Hickey's [Clojure](https://clojure.org/), to introduce all the features Stream should have.

I do not claim this to be enterprise-ready in any way, shape, or form, but I believe it is production-ready. I've made no measurements in regard to its performance.

Feel free to copy any of this code if it is of any help to you. If you find yourself using this class in your Java-code, chances are you're using the wrong programming-language. Take a look at [C#](https://en.wikipedia.org/wiki/C_Sharp_(programming_language)), [Kotlin](https://kotlinlang.org/) or [Clojure](https://clojure.org/).

## Contents
- [Background](#background)
- [Usage](#usage)
  - [Creation](#creation)
  - [Operations](#operations)
  - [Interoperability](#interoperability)
  - [Extensions](#extensions)


## Background

I've always had a difficult relationship with Java's Streams. After a decade working with C# (and [LINQ](https://en.wikipedia.org/wiki/Language_Integrated_Query)), Streams were the single feature that convinced me to try out Java -- and another decade later it's one of the features driving me away; towards languages like [Kotlin](https://kotlinlang.org/) or [Clojure](https://clojure.org/).

### Streams Are Cumbersome

- While functional features like **filter**, **map**, and **reduce** are built in for any collection (lazy or not) in other languages, in Java one only gets access if the collection at hand is turned into a Stream by calling `.stream()`.
- At least, this is built-in with Java's `Collection`-interface, but turning an array into a Stream requires the use of static helper-classes like `Arrays`.
- In the end, the contents of the Stream have to be turned back into a collection of your choosing, by calling `.collect()`.
- In order to do so, one has to deal with additional static helper-classes, so-called `Collectors` to be passed to the `.collect()`-method. While recent Java-versions provide a convenient shortcut `.toList()`, creating a `Set` or `Map` still requires a collector, not to mention creating an array -- which is difficult due to Java's broken generics.

### Streams Are Not Re-usable

- The Stream-API is just a thin wrapper around `Iterator`, which provides out-of-the-box laziness in Java, or any OOP language.
- This means that once a Stream is "realized" (fully consumed) it cannot be consumed again.

### Basic Features Are Missing

While it's entirely possible (and no rocket science) to get those features using your own `Iterator`-implementation, the lack of extension-methods in Java make it painful to use. Some of those features are:
- **zip** -- Lazily mapping two (or more) potentially infinite collections by _"applying f to
  the set of first items of each coll, followed by applying f to the
  set of second items in each coll, until any one of the colls is
  exhausted"_ ([Rich Hickey](https://clojuredocs.org/clojure.core/map))
- **partition** -- Lazily partitioning a potentially infinite collection into a collection of collections. This allows for batching or creating a sliding window.
- **head/tail** -- Lazily accessing the first and/or remaining items of a potentially infinite collection.

## Usage

**Seq** is always being evaluated lazily. It can be re-used and operated on as often as needed. Some operations will cause it to be partly or fully realised into memory. Consuming an infinite Seq entirely will run infinitely or until system resources are exhausted. Operations that may lead to that behaviour are marked accordingly.

### Creation

#### Seq.of()
- Returns a new empty Seq.

#### Seq.of(x1, x2, x3, ...)
- Returns a new Seq of all supplied xs.

#### Seq.of(coll)
- Returns a new Seq of all items in coll which can be a Stream, Iterable, Iterator, Array, or Map. If coll is a Stream it will be consumed lazily. If coll is a Map<K, V> the returned Seq will contain items of type Map.Entry<K, V>.

#### Seq.cons(first, f)
- //TODO

#### Seq.concat(coll, f)
- //TODO

#### Seq.iterate(init, f)
- //TODO

#### Seq.range()
- //TODO

#### Seq.range(end)
- //TODO

#### Seq.range(start, end)
- //TODO

#### Seq.range(start, end, step)
- //TODO

### Operations

#### first()
- Returns the first item in this Seq.
- Returns null if this Seq is empty.

#### rest()
- Returns a new Seq of the items in this Seq after the first.

#### take(n)
- Returns a new Seq with the first n items in this Seq.

#### drop(n)
- Returns a new Seq with all but the first n items in this Seq.

#### filter(pred)
- Returns a new Seq of the items in this Seq for which pred(item) returns true.

#### map(f)
- Returns a new Seq of the result of applying f to each item in this Seq.

#### map(other, f)
- Returns a new Seq of the result of applying f to the set of first items in both this Seq and other, followed by applying f to the set of second items in this Seq and other, until any one of the collections is exhausted. Any remaining items in either collection are ignored.
_(This is similar to zipping two collections.)_

#### mapcat(f)
- Returns a new Seq of the result of applying concat to the result of applying map
to f and the items in this Seq. Function f should return a collection. _(This is similar to Stream::flatMap.)_

#### takeWhile(pred)
- Returns a new Seq of successive items from this Seq while pred(item) returns true.

#### dropWhile(pred)
- Returns a new Seq of the items in this Seq starting from the first item for which pred(item) returns false.

#### partition(n)
- Returns a new Seq of lists of n items each.

#### partition(n, step)
- Returns a new Seq of lists of n items each, at offsets step apart.

#### partition(n, step, pad)
- Returns a new Seq of lists of n items each, at offsets step apart. The items in pad are used as necessary to complete the last partition up to n items. In case there are not enough padding items, returns a partition with less than n items.

#### partitionAll(n)
- Returns a new Seq of lists of n items each, like partition, but may include partitions with fewer than n items at the end.

#### partitionAll(n, step)
- Returns a new Seq of lists of n items each, at offsets step apart, like partition, but may include partitions with fewer than n items at the end.

#### reductions(f)
- Returns a new Seq of the intermediate values of the reduction (as per reduce) of this Seq by f.

#### reductions(init, f)
- Returns a new Seq of the intermediate values of the reduction (as per reduce) of this Seq by f, starting with init.

#### distinct()
- Returns a new Seq of the items of this Seq with duplicates removed.

#### sorted()
- Returns a new Seq of the items of this Seq sorted by using compare.

#### sorted(comp)
- Returns a new Seq of the items of this Seq sorted by using supplied comparator comp.

#### reduce(f)
- Returns an empty Optional if this Seq contains no items.
- Returns an Optional of the result of applying f to the first 2 items in this Seq, then applying f to that result and the 3rd item, etc. If this Seq has only 1 item, it is returned and f is not called. **Caution:** If the Seq is lazy, it will be fully realised. If this Seq is infinite, it will run infinitely or until system resources are exhausted.

#### reduce(val, f)
- Returns val if this Seq is empty. f is not called.
- Returns the result of applying f to val and the first item in this Seq, then applying f to that result and the 2nd item, etc. **Caution:** If the Seq is lazy, it will be fully realised. If this Seq is infinite, it will run infinitely or until system resources are exhausted.

#### some(pred)
- Returns the first true value of pred(x) for any x in this Seq. _(This is similar to Stream::anyMatch.)_
- Returns false if none of the items in this Seq return true for pred(item). **Caution:** If the Seq is lazy, it will be fully realised. If this Seq is infinite, it will run infinitely or until system resources are exhausted.

#### every(pred)
- Returns the first false value of pred(x) for any x in this Seq. _(This is similar to Stream::allMatch.)_
- Returns true if all the items in this Seq return true for pred(item). **Caution:** If the Seq is lazy, it will be fully realised. If this Seq is infinite, it will run infinitely or until system resources are exhausted. 

#### notAny(pred)
- Returns false if (pred x) is true for any x in this Seq. _(This is similar to Stream::noneMatch.)_
- Returns true if none of the items in this Seq return true for pred(item). **Caution:** If the Seq is lazy, it will be fully realised. If this Seq is infinite, it will run infinitely or until system resources are exhausted.

#### isRealized()
- Returns true if a value has been produced for this Seq.

#### max(comp)
- Returns the item in this Seq for which comp determines is greatest. If there are multiple such items, the last one is returned. **Caution:** If the Seq is lazy, it will be fully realised. If this Seq is infinite, it will run infinitely or until system resources are exhausted.

#### min(comp)
- Returns the item in this Seq for which comp determines is least. If there are multiple such items, the last one is returned. **Caution:** If the Seq is lazy, it will be fully realised. If this Seq is infinite, it will run infinitely or until system resources are exhausted.

#### maxKey(f)
- Returns the item x in this Seq for which f(x), a number, is greatest. If there are multiple such xs, the last one is returned. **Caution:** If the Seq is lazy, it will be fully realised. If this Seq is infinite, it will run infinitely or until system resources are exhausted.

#### minKey(f)
- Returns the item x in this Seq for which f(x), a number, is least. If there are multiple such xs, the last one is returned. **Caution:** If the Seq is lazy, it will be fully realised. If this Seq is infinite, it will run infinitely or until system resources are exhausted.

#### find(i)
- Returns an empty Optional if this Seq does not contain any item at index i.
- Returns an Optional of the item at index i in this Seq.

#### findFirst()
- Returns an empty Optional if this Seq contains no items.
- Returns an Optional of the first item in this Seq. _(See first().)_

#### findFirst(pred)
- Returns an Optional of the first item in this Seq for which pred(item) returns true.
- Returns an empty Optional if pred(item) returns false for all items in this Seq. **Caution:** If the Seq is lazy, it will be fully realised. If this Seq is infinite, it will run infinitely or until system resources are exhausted.

#### toMap()
- Returns a new Map with the keys and values of the items in this Seq<Map.Entry>. If the Seq is of any other type, an exception is thrown. **Caution:** If the Seq is lazy, it will be fully realised. If this Seq is infinite, it will run infinitely or until system resources are exhausted.

#### toMap(k, v)
- Returns a new Map with keys as a result of k(item) and values as a result of v(item) of all items in this Seq. **Caution:** If the Seq is lazy, it will be fully realised. If this Seq is infinite, it will run infinitely or until system resources are exhausted.

#### toList()
- Returns a List with all items in this Seq. **Caution:** If the Seq is lazy, it will be fully realised. If this Seq is infinite, it will run infinitely or until system resources are exhausted.

### Interoperability

#### Object::toString()
- Returns the string representation of this Seq, e.g. "[1, 2, 3]". Only the first and any other realised item will be included. Unrealized remaining items will be depicted with a single question mark, e.g. "[1, 2, ?]". If this Seq is empty, only brackets will be returned, e.g. "[]".

#### Iterable::forEach(f)
- Calls f(x) for each x in this Seq, f being a consumer function taking one argument and returning void. **Caution:** If the Seq is lazy, it will be fully realised. If this Seq is infinite, it will run infinitely or until system resources are exhausted.

#### Collection::stream()
- Returns a Stream of the items in this Seq.

#### Collection::parallelStream()
- Returns a Stream of the items in this Seq.

#### List::get(index)
- Returns the item at index in this Seq. All items up to that point will be realised.
- Returns null if index is out of bounds. **Caution:** If the Seq is lazy, it will be fully realised. If this Seq is infinite, it will run infinitely or until system resources are exhausted.

#### List::iterator()
- Returns an Iterator of the items in this Seq.

#### List::isEmpty()
- Returns true if this Seq is empty, otherwise false.

#### List::size()
- Returns the number of items in this Seq. **Caution:** If the Seq is lazy, it will be fully realised. If this Seq is infinite, it will run infinitely or until system resources are exhausted.

#### List::subList(fromIndex, toIndex)
- Returns a new List of items from this Seq between fromIndex (inclusive) and toIndex (exclusive).

### Lombok Extensions

If you're using [Lombok](https://projectlombok.org/) you can add `@ExtensionMethods({Seq.Extensions.class})` to your class in order to access the following extension methods.

#### Iterable::toSeq()
- Returns a new Seq with the items in extended Iterable.

#### Iterator::toSeq()
- Returns a new Seq with the items in extended Iterator.

#### Stream::toSeq()
- Returns a new Seq with the items in extended Stream.

#### Map::toSeq()
- Returns a new Seq<Map.Entry> with the items in extended Map.
