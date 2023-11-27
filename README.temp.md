# Seq

## This Is An Exercise

The goal of this exercise was to build an alternative to Java's Stream API, heavily influenced by Rich Hickey's [Clojure](https://clojure.org/), to introduce all the features Stream should have.

I do not claim this to be enterprise-ready in any way, shape, or form, but I believe it is production-ready. I've made no measurements in regard to its performance.

Feel free to copy any of this code if it is of any help to you. If you find yourself using this class in your Java-code chances are, you're working with the wrong programming-language. Take a look at [C#](https://en.wikipedia.org/wiki/C_Sharp_(programming_language)), [Kotlin](https://kotlinlang.org/) or [Clojure](https://clojure.org/).


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

//TODO

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

#### max(comp)
- //TODO

#### min(comp)
- //TODO

#### maxKey(f)
- //TODO

#### minKey(f)
- //TODO

#### toList()
- //TODO

### Interoperability

#### Iterable::forEach(f)
- //TODO

#### Collection::stream()
- //TODO

#### Collection::parallelStream()
- //TODO

#### List::get(index)
- //TODO

#### List::iterator()
- //TODO

#### List::isEmpty()
- //TODO

#### List::size()
- //TODO

#### List::subList(fromIndex, toIndex)
- //TODO
