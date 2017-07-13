package fr.vergne.filter;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A {@link Filter} provides a filtering function (and as such is a
 * {@link FunctionalInterface}). This function tells whether an element is
 * accepted or rejected by returning respectively <code>true</code> or
 * <code>false</code>. It can also be undecided if the {@link Filter} does not
 * manage such element, in which case it returns <code>null</code>. This is the
 * main difference with the standard {@link Predicate}, which returns only
 * <code>true</code> or <code>false</code>. To remain compatible with this
 * standard, several methods are provided to transform a {@link Predicate} into
 * a {@link Filter} and vice-versa.
 * 
 * @author Matthieu Vergne <vergne@fbk.eu>
 * 
 * @param <Element>
 */
@FunctionalInterface
public interface Filter<Element> {
	/**
	 * 
	 * @param element
	 *            the element to consider
	 * @return <code>true</code> if the element should be kept, <code>false</code>
	 *         if it should not be kept, an <code>null</code> if the element is not
	 *         supported
	 */
	public Boolean accepts(Element element);

	/**
	 * This method instantiates a {@link Predicate} based on this {@link Filter}. To
	 * work properly as a {@link Predicate}, the {@link Filter} should support every
	 * elements we may provide (i.e. it should never return <code>null</code>). If
	 * an unsupported element is provided, an {@link IllegalArgumentException} is
	 * thrown when calling {@link Predicate#test(Object)}.
	 * 
	 * @return a {@link Predicate} based on the current {@link Filter}
	 */
	default Predicate<Element> toPredicate() {
		return (element) -> {
			Boolean accepted = accepts(element);
			if (accepted != null) {
				return accepted;
			} else {
				throw new IllegalArgumentException(
						"The predicate is based on a filter which does not support " + element);
			}
		};
	}

	/**
	 * This method provides a {@link Filter} which rejects currently accepted
	 * elements and accepts currently rejected elements. The elements which are not
	 * supported by the current {@link Filter} remain not supported by the new one.
	 * 
	 * @return a {@link Filter} which reverses the logics of the current
	 *         {@link Filter}
	 */
	default Filter<Element> reverse() {
		return (element) -> {
			Boolean isAccepted = accepts(element);
			if (isAccepted == null) {
				return null;
			} else {
				return !isAccepted;
			}
		};
	}

	/**
	 * 
	 * @param element
	 *            the element to accept with the new {@link Filter}
	 * @return a {@link Filter} which accepts the given element and relies on the
	 *         current {@link Filter} for the others
	 */
	default Filter<Element> plus(Element element) {
		return (e) -> {
			if (Objects.equals(e, element)) {
				return true;
			} else {
				return accepts(e);
			}
		};
	}

	/**
	 * 
	 * @param elements
	 *            the elements to accept with the new {@link Filter}
	 * @return a {@link Filter} which accepts the given elements and relies on the
	 *         current {@link Filter} for the others
	 */
	default Filter<Element> plusAll(Collection<? extends Element> elements) {
		Set<Element> accepted = new HashSet<>(elements);
		if (accepted.size() == 0) {
			return this;
		} else if (accepted.size() == 1) {
			Element element = accepted.iterator().next();
			return plus(element);
		} else {
			return (element) -> {
				if (accepted.contains(element)) {
					return true;
				} else {
					return accepts(element);
				}
			};
		}
	}

	/**
	 * This method makes a conservative {@link Filter}: all the elements which are
	 * not supported yet will be accepted by default with the new {@link Filter}.
	 * 
	 * @return a {@link Filter} which accepts all the elements not yet supported.
	 */
	default Filter<Element> plusNotSupported() {
		return (element) -> {
			Boolean isAccepted = accepts(element);
			if (isAccepted == null) {
				return true;
			} else {
				return isAccepted;
			}
		};
	}

	/**
	 * 
	 * @param element
	 *            the element to reject with the new {@link Filter}
	 * @return a {@link Filter} which rejects the given element and relies on the
	 *         current {@link Filter} for the others
	 */
	default Filter<Element> minus(Element element) {
		return (e) -> {
			if (Objects.equals(e, element)) {
				return false;
			} else {
				return accepts(e);
			}
		};
	}

	/**
	 * 
	 * @param elements
	 *            the elements to reject with the new {@link Filter}
	 * @return a {@link Filter} which rejects the given elements and relies on the
	 *         current {@link Filter} for the others
	 */
	default Filter<Element> minusAll(Collection<? extends Element> elements) {
		Set<Element> rejected = new HashSet<>(elements);
		if (rejected.size() == 0) {
			return this;
		} else if (rejected.size() == 1) {
			Element element = rejected.iterator().next();
			return minus(element);
		} else {
			return (element) -> {
				if (rejected.contains(element)) {
					return false;
				} else {
					return accepts(element);
				}
			};
		}
	}

	/**
	 * This method makes an expeditive {@link Filter}: all the elements which are
	 * not supported yet will be rejected by default with the new {@link Filter}.
	 * 
	 * @return a {@link Filter} which rejects all the elements not yet supported.
	 */
	default Filter<Element> minusNotSupported() {
		return (element) -> {
			Boolean isAccepted = accepts(element);
			if (isAccepted == null) {
				return false;
			} else {
				return isAccepted;
			}
		};
	}

	/**
	 * 
	 * @param element
	 *            the element to ignore with the new {@link Filter}
	 * @return a {@link Filter} which ignore the given element and relies on the
	 *         current {@link Filter} for the others
	 */
	default Filter<Element> ignore(Element element) {
		return (e) -> {
			if (Objects.equals(e, element)) {
				return null;
			} else {
				return accepts(e);
			}
		};
	}

	/**
	 * 
	 * @param elements
	 *            the elements to ignore with the new {@link Filter}
	 * @return a {@link Filter} which ignores the given elements and relies on the
	 *         current {@link Filter} for the others
	 */
	default Filter<Element> ignoreAll(Collection<? extends Element> elements) {
		Set<Element> ignored = new HashSet<>(elements);
		if (ignored.size() == 0) {
			return this;
		} else if (ignored.size() == 1) {
			Element element = ignored.iterator().next();
			return ignore(element);
		} else {
			return (element) -> {
				if (ignored.contains(element)) {
					return null;
				} else {
					return accepts(element);
				}
			};
		}
	}

	/**
	 * This method creates a {@link Filter} which uses the current {@link Filter}
	 * when possible and the {@link Filter} in argument when an element is not
	 * supported.
	 * 
	 * @param filter
	 *            the {@link Filter} to use when required
	 * @return a {@link Filter} which uses first the current {@link Filter} and then
	 *         the {@link Filter} in argument.
	 */
	default Filter<Element> before(Filter<? super Element> filter) {
		return (element) -> {
			Boolean isAccepted = accepts(element);
			if (isAccepted == null) {
				return filter.accepts(element);
			} else {
				return isAccepted;
			}
		};
	}

	/**
	 * This method creates a {@link Filter} which uses the {@link Filter} in
	 * argument when possible and the current {@link Filter} when an element is not
	 * supported.
	 * 
	 * @param filter
	 *            the {@link Filter} to use in priority
	 * @return a {@link Filter} which uses first the {@link Filter} in argument and
	 *         then the current {@link Filter}.
	 */
	default Filter<Element> after(Filter<? super Element> filter) {
		return (element) -> {
			Boolean isAccepted = filter.accepts(element);
			if (isAccepted == null) {
				return accepts(element);
			} else {
				return isAccepted;
			}
		};
	}

	/**
	 * This method is a convenient method to add a filtering step to a
	 * {@link Stream}. If some elements are not supported by this {@link Filter}, an
	 * {@link IllegalStateException} will be thrown when the {@link Stream} will be
	 * processed.
	 * 
	 * @param stream
	 *            the {@link Stream} to filter
	 * @return the filtered {@link Stream}
	 */
	default <T extends Element> Stream<T> applyToStream(Stream<T> stream) {
		Predicate<Element> predicate = toPredicate();
		return stream.filter((element) -> {
			try {
				return predicate.test(element);
			} catch (IllegalArgumentException cause) {
				throw new IllegalStateException("The filter " + Filter.this + " does not support element " + element
						+ " processed by stream " + stream, cause);
			}
		});
	}

	/**
	 * This method is a convenient method to build a {@link Collection} without the
	 * elements rejected by this {@link Filter}. If some elements are not supported
	 * by this {@link Filter}, an {@link IllegalArgumentException} will be thrown.
	 * 
	 * @param elements
	 *            the {@link Collection} of elements to filter
	 * @return a {@link Collection} with only the elements accepted by this
	 *         {@link Filter}
	 * @throws IllegalArgumentException
	 *             if some elements are not supported by this {@link Filter}
	 */
	default <T extends Element> Collection<T> applyToCollection(Collection<T> elements) {
		return elements.stream().filter(toPredicate()).collect(Collectors.toList());
	}

	/**
	 * This method is a convenient method to build a {@link Map} without the keys
	 * rejected by this {@link Filter}. If some keys are not supported by this
	 * {@link Filter}, an {@link IllegalArgumentException} will be thrown.
	 * 
	 * @param map
	 *            the {@link Map} to filter
	 * @return a {@link Map} with only the keys accepted by this {@link Filter}
	 * @throws IllegalArgumentException
	 *             if some keys are not supported by this {@link Filter}
	 */
	default <K extends Element, V> Map<K, V> applyToMapKeys(Map<K, V> map) {
		Predicate<Element> keyPredicate = toPredicate();
		return map.entrySet().stream().filter((entry) -> keyPredicate.test(entry.getKey()))
				.collect(Collectors.toMap((entry) -> entry.getKey(), (entry) -> entry.getValue()));
	}

	/**
	 * This method is a convenient method to build a {@link Map} without the keys
	 * rejected by this {@link Filter}. If some keys are not supported by this
	 * {@link Filter}, an {@link IllegalArgumentException} will be thrown.
	 * 
	 * @param map
	 *            the {@link Map} to filter
	 * @return a {@link Map} with only the keys accepted by this {@link Filter}
	 * @throws IllegalArgumentException
	 *             if some keys are not supported by this {@link Filter}
	 */
	default <K, V extends Element> Map<K, V> applyToMapValues(Map<K, V> map) {
		Predicate<Element> valuePredicate = toPredicate();
		return map.entrySet().stream().filter((entry) -> valuePredicate.test(entry.getValue()))
				.collect(Collectors.toMap((entry) -> entry.getKey(), (entry) -> entry.getValue()));
	}

	/**
	 * @return a {@link Filter} which accepts everything (always returns
	 *         <code>true</code>)
	 */
	public static <Element> Filter<Element> acceptsAll() {
		return (element) -> true;
	}

	/**
	 * @return a {@link Filter} which accepts nothing (always returns
	 *         <code>false</code>)
	 */
	public static <Element> Filter<Element> acceptsNone() {
		return (element) -> false;
	}

	/**
	 * @return a {@link Filter} which never knows (always returns <code>null</code>)
	 */
	public static <Element> Filter<Element> supportsNone() {
		return (element) -> null;
	}

	/**
	 * This method allows to instantiate a {@link Filter} to accept the same
	 * elements than a given {@link Predicate}. Because a {@link Predicate} returns
	 * either <code>true</code> or <code>false</code>, a {@link Filter} instantiated
	 * through this method never returns <code>null</code> (although it may throw
	 * {@link RuntimeException}s). If you need to specify undecided cases, you
	 * should use {@link #fromPredicates(Predicate, Predicate)} instead, which
	 * requires an additional {@link Predicate} to know whether or not an element is
	 * supported.
	 * 
	 * @param predicate
	 *            the {@link Predicate} to translate into a {@link Filter}
	 * @return a {@link Filter} acting lie the provided {@link Predicate}
	 * @throws NullPointerException
	 *             if the {@link Predicate} is <code>null</code>
	 */
	public static <Element> Filter<Element> fromPredicate(Predicate<Element> predicate) {
		Objects.requireNonNull(predicate, "No predicate provided");
		return (element) -> predicate.test(element);
	}

	/**
	 * This method transforms a {@link Predicate} into a {@link Filter}. Because a
	 * {@link Filter} does not necessarily support an element (i.e. can return
	 * <code>null</code>), an additional predicate is used to tell whether or not an
	 * element is supported by this {@link Filter}. If the element is not supported,
	 * <code>null</code> is returned, otherwise the result of the predicate choosing
	 * <code>true</code> or <code>false</code> is returned.
	 * 
	 * @param isSupported
	 *            tells whether an element is supported (<code>true</code> or
	 *            <code>false</code>) or not (<code>null</code>)
	 * @param isAccepted
	 *            if the element is supported, tells whether it is accepted
	 *            (<code>true</code>) or not (<code>false</code>)
	 * @return a {@link Filter} exploiting the provided {@link Predicate}s
	 * @throws NullPointerException
	 *             for each <code>null</code> {@link Predicate}
	 */
	public static <Element> Filter<Element> fromPredicates(Predicate<Element> isSupported,
			Predicate<Element> isAccepted) {
		Objects.requireNonNull(isSupported, "No support predicate provided");
		Objects.requireNonNull(isAccepted, "No acceptance predicate provided");
		return (element) -> isSupported.test(element) ? isAccepted.test(element) : null;
	}
}
