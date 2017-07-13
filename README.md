# Summary

This project provides a rich set of filtering functions for Java 8. As opposed to the standard functional interface `java.util.function.Predicate`, which can only return `true` or `false`, the `Filter` functional interface can also return `null`, which makes it able to explicit when a case is not managed. The interface also acts as a factory, with static methods providing trivial filters (e.g. accept all, reject all), as well as a builder, with default methods to create new filters from existing ones. These two kinds of features offer to the programmer a fluent interface which allows to build filters progressively through method chaining. Despite the fundamental difference with the standard `Predicate`, methods are provided to switch between the two representations, allowing to exploit the richness of the `Filter` features for `Predicate` as well. This ability supports the use of filters on standard structures like `Stream`, `Collection`, or `Map`, for which some methods are provided.

This project was first a module of a `Collection` project. It has now its own project to avoid making a lib with many, loosely related components.

# Manual

## `Filter` Implementation

The `Filter` interface is a functional interface, and as such requires only to implement a single method:
```java
public interface Filter<Element> {
	public Boolean accepts(Element element);
}
```

This method allows to tell whether an element is accepted, by returning `true`, or not, by returning `false`. It can also return `null`, which should be interpreted as the *inability* to tell whether or not the element is accepted. Such element is considered as *not supported* or *ignored* by the filter.

One can implement a filter in the usual way by specifying the whole (anonymous) class:
```java
Filter<String> emptyStringFilter = new Filter<String>() {
	public Boolean accepts(String string) {
		return !string.isEmpty();
	}
};
```

or through lambda expressions:
```java
Filter<String> emptyStringFilter = (s) -> !s.isEmpty();
```

## Trivial Filters through Static Methods

Several trivial filters are already implemented. They can be instantiated through static methods provided directly by the `Filter` interface:
- `Filter.acceptsAll()` provides a `Filter` which accepts everything ;
- `Filter.acceptsNone()` provides a `Filter` which rejects everything ;
- `Filter.supportsNone()` provides a `Filter` which support nothing.

The two first return respectively only `true` and only `false`. The third one is a `Filter` which always returns `null` (i.e. no element is supported). Although these filters might seem particularly useless, their utility come from combining them with the next features: they provides starters from which more complex filters can be built.

## Complex Filters through Method Chaining

People who would like to build complex filters can take two paths: one is to build a `Filter` from scratch, and another is to combine filters progressively through simple steps. The `Filter` interface provides many methods for the latter. Indeed, despite being a functional interface, it provides many default methods which exploit the current instance to build a richer `Filter`. Thus, from a simple filter presented above, one can easily adapt it to accept, reject, or ignore some elements.

For example, one can build a `Filter` which accepts only some elements and rejects all the others:
```java
Filter<Integer> acceptsSome = Filter.<Integer>acceptsNone()
                                    .plus(1)
                                    .plus(2)
                                    .plus(3);
```
or in a more compact way:
```java
Filter<Integer> acceptsSome = Filter.<Integer>acceptsNone()
                                    .plusAll(Arrays.asList(1, 2, 3));
```

A reversed `Filter`, which rejects only some elements and accepts all the others, can be built by calling:
```java
Filter<Integer> acceptsMost = Filter.<Integer>acceptsAll()
                                    .minus(1)
                                    .minus(2)
                                    .minus(3);
```

Or again in a more compact way:
```java
Filter<Integer> acceptsMost = Filter.<Integer>acceptsAll()
                                    .minusAll(Arrays.asList(1, 2, 3));
```

Reversal of an existing filter can also be done in one call:
```java
Filter<Integer> acceptsMost = acceptsSome.reverse();
```

If some elements should not be supported by the `Filter`, one can ignore them explicitly:
```java
Filter<Integer> ignoreNull = Filter.<Integer>acceptsAll()
                                   .ignore(null);
```
Again, both individual and grouped versions are provided.

In a more natural manner, one may start from an empty filter and accept/reject the relevant nodes:
```java
Filter<Integer> fromScratch = Filter.<Integer>supportsNone()
                                    .plus(1).plus(3).plus(5)
                                    .minus(2).minus(4).minus(6);
```

If one starts from an empty `Filter`, but want to support every possible values, two methods allow to accept or reject all the values not yet supported:
```java
Filter<Integer> acceptsSome = Filter.<Integer>supportsNone()
                                    .plus(1).plus(3).plus(5)
                                    .minusNotSupported();

Filter<Integer> acceptsMost = Filter.<Integer>supportsNone()
                                    .minus(1).minus(3).minus(5)
                                    .plusNotSupported();
```

A `Filter` can also be combined with another one. Because each of them may accept, reject, and ignore its own elements, it is important to know which one should be used before the other. To support at best the developer, both directions are supported:
```java
Filter<Integer> filter1 = ...;
Filter<Integer> filter2 = ...;
Filter<Integer> filterCombined1 = filter1.before(filter2);
Filter<Integer> filterCombined2 = filter2.after(filter1);
```
In this example, both combined filters are equivalent: `filter1` will be processed first, and if the element is not supported, only then `filter2` will be used.

**Note**

Of course, each call potentially increases the depth of nested filters, which means that performance might be impacted with many calls. A particular care should be given to overridden calls, like `filter.plus(1).minus(1)` which makes the call `plus(1)` useless (although it might still consume additional resources). These methods help to simplify the building of new filters but, once a valid filter is built, it is recommended to reduce the calls (e.g. merge all the `plus()` into a single `plusAll()`). Because the order of the calls is important, such a reduction should be done carefully as well. For the most efficient implementation, one will need to create a whole instance from scratch.

## Utility Methods

With the previous features, one can build simple or complex filters as needed. However, it is useless if it cannot be used easily. Several methods has been implemented to cover standard cases:
- `filter.applyToCollection(Collection)` applies the filter on a `Collection` of elements, such that it produces a new `Collection` with the accepted elements only ;
- `filter.applyToMapKeys(Map)` applies the filter on a `Map`, such that it produces a new `Map` with the accepted keys only (and their values) ;
- `filter.applyToMapValues(Map)` applies the filter on a `Map` of elements, such that it produces a new `Map` with the accepted values only (and their keys) ;
- `filter.applyToStream(Stream)` applies the filter on a `Stream` of elements, and more particularly by calling `Stream.filter()`.

The last case is a bit different to the previous ones, because it returns a new `Stream` on which the filtering is configured, but which will be effective only when the returned `Stream` will be processed. The other methods return directly a new, filtered instance.

## Link with the Standard `java.util.function.Predicate`

`Filter` is highly similar to `Predicate`, introduced in Java 8. However, a `Predicate` only returns `true` or `false`, while a `Filter` can also return `null` to say that the element is not supported. However, such a case is not common and, although it might help, it is probable that the main use of a `Filter` will remain equivalent to the one of a `Predicate`. In other words, many features implemented for `Predicate` would be relevant for `Filter`, and vice-versa. This motivates the ability to swap representations. Again, some methods have been implemented to support this swap.

To get a `Predicate` from a `Filter`, one can just call the dedicated method on the `Filter` instance:
```java
Filter<String> emptyStringFilter = (s) -> !s.isEmpty();
Predicate<String> emptyStringPredicate = emptyStringFilter.toPredicate();
```

To get a `Filter` from a `Predicate`, one can use the static method of the `Filter` interface:
```java
Predicate<String> emptyStringPredicate = (s) -> !s.isEmpty();
Filter<String> emptyStringFilter = Filter.fromPredicate(emptyStringPredicate);
```
If the `Filter` does not support an element, but a `Predicate` is built on it and applied to this element, an exception will be thrown.

In the case where some cases should not be supported by the filter, this can be specified through an additional `Predicate`:
```java
Predicate<String> isSupported = (s) -> s != null;
Predicate<String> isAccepted = (s) -> !s.isEmpty();
Filter<String> upperCaseFilter = Filter.fromPredicates(isSupported, isAccepted);
```

The first to be checked is the support, and only if the element is supported the filter will check the acceptance, otherwise it will directly return `null`.

With these methods, one can easily swap between a `Predicate` and a `Filter` instance, making the features developed in this library usable with `Predicate` instances as well. Similarly, features developed for `Predicate` are also usable with `Filter` instances. The utility methods presented above are examples exploiting this ability.