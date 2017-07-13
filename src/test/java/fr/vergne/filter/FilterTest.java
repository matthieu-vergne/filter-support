package fr.vergne.filter;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

public class FilterTest {

	@Test
	public void testAcceptsAllFilterAcceptsNull() {
		Filter<Object> filter = Filter.acceptsAll();
		assertTrue(filter.accepts(null));
	}

	@Test
	public void testAcceptsAllFilterAcceptsArbitraryValues() {
		Filter<Object> filter = Filter.acceptsAll();
		assertTrue(filter.accepts(new Object()));
		assertTrue(filter.accepts("test"));
		assertTrue(filter.accepts(42));
	}

	@Test
	public void testAcceptsNoneFilterRejectsNull() {
		Filter<Object> filter = Filter.acceptsNone();
		assertFalse(filter.accepts(null));
	}

	@Test
	public void testAcceptsNoneFilterRejectsArbitraryValues() {
		Filter<Object> filter = Filter.acceptsNone();
		assertFalse(filter.accepts(new Object()));
		assertFalse(filter.accepts("test"));
		assertFalse(filter.accepts(42));
	}

	@Test
	public void testSupportsNoneFilterDoesNotSupportNull() {
		Filter<Object> filter = Filter.supportsNone();
		assertNull(filter.accepts(null));
	}

	@Test
	public void testSupportsNoneFilterDoesNotSupportArbitraryValues() {
		Filter<Object> filter = Filter.supportsNone();
		assertNull(filter.accepts(new Object()));
		assertNull(filter.accepts("test"));
		assertNull(filter.accepts(42));
	}

	@Test
	public void testSinglePredicateFilterReturnsTrueWhenPredicateReturnsTrue() {
		Predicate<Object> predicate = (e) -> true;
		Filter<Object> filter = Filter.fromPredicate(predicate);
		assertTrue(filter.accepts(null));
		assertTrue(filter.accepts(42));
		assertTrue(filter.accepts(new Object()));
	}

	@Test
	public void testSinglePredicateFilterReturnsFalseWhenPredicateReturnsFalse() {
		Predicate<Object> predicate = (e) -> false;
		Filter<Object> filter = Filter.fromPredicate(predicate);
		assertFalse(filter.accepts(null));
		assertFalse(filter.accepts(42));
		assertFalse(filter.accepts(new Object()));
	}

	@Test
	public void testSinglePredicateFilterThrowsExceptionOnNullPredicate() {
		try {
			Filter.fromPredicate(null);
			fail("No exception thrown");
		} catch (NullPointerException cause) {
			// OK
		}
	}

	@Test
	public void testDoublePredicateFilterReturnsNullWhenNotSupported() {
		Predicate<Object> isSupported = (e) -> false;
		Predicate<Object> isAccepted = (e) -> {
			throw new RuntimeException("Should not be executed");
		};
		Filter<Object> filter = Filter.fromPredicates(isSupported, isAccepted);
		assertNull(filter.accepts(null));
		assertNull(filter.accepts(42));
		assertNull(filter.accepts(new Object()));
	}

	@Test
	public void testDoublePredicateFilterReturnsTrueWhenSupportedAndAccepted() {
		Predicate<Object> isSupported = (e) -> true;
		Predicate<Object> isAccepted = (e) -> true;
		Filter<Object> filter = Filter.fromPredicates(isSupported, isAccepted);
		assertTrue(filter.accepts(null));
		assertTrue(filter.accepts(42));
		assertTrue(filter.accepts(new Object()));
	}

	@Test
	public void testDoublePredicateFilterReturnsFalseWhenSupportedAndNotAccepted() {
		Predicate<Object> isSupported = (e) -> true;
		Predicate<Object> isAccepted = (e) -> false;
		Filter<Object> filter = Filter.fromPredicates(isSupported, isAccepted);
		assertFalse(filter.accepts(null));
		assertFalse(filter.accepts(42));
		assertFalse(filter.accepts(new Object()));
	}

	@Test
	public void testDoublePredicateFilterthrowsExceptionOnNullFirstPredicate() {
		Predicate<Object> isSupported = null;
		Predicate<Object> isAccepted = (e) -> false;
		try {
			Filter.fromPredicates(isSupported, isAccepted);
			fail("No exception thrown");
		} catch (NullPointerException cause) {
			// OK
		}
	}

	@Test
	public void testDoublePredicateFilterthrowsExceptionOnNullSecondPredicate() {
		Predicate<Object> isSupported = (e) -> false;
		Predicate<Object> isAccepted = null;
		try {
			Filter.fromPredicates(isSupported, isAccepted);
			fail("No exception thrown");
		} catch (NullPointerException cause) {
			// OK
		}
	}

	@Test
	public void testPredicateReturnsTrueWhenFilterReturnsTrue() {
		Filter<Object> filter = (e) -> true;
		Predicate<Object> predicate = filter.toPredicate();
		assertTrue(predicate.test(null));
		assertTrue(predicate.test(42));
		assertTrue(predicate.test(new Object()));
	}

	@Test
	public void testPredicateReturnsFalseWhenFilterReturnsFalse() {
		Filter<Object> filter = (e) -> false;
		Predicate<Object> predicate = filter.toPredicate();
		assertFalse(predicate.test(null));
		assertFalse(predicate.test(42));
		assertFalse(predicate.test(new Object()));
	}

	@Test
	public void testPredicateThrowsExceptionWhenFilterReturnsNull() {
		Filter<Object> filter = (e) -> null;
		Predicate<Object> predicate = filter.toPredicate();
		try {
			predicate.test(null);
			fail("No exception thrown");
		} catch (IllegalArgumentException cause) {
			// OK
		}
		try {
			predicate.test(42);
			fail("No exception thrown");
		} catch (IllegalArgumentException cause) {
			// OK
		}
		try {
			predicate.test(new Object());
			fail("No exception thrown");
		} catch (IllegalArgumentException cause) {
			// OK
		}
	}

	@Test
	public void testPlusElementEnforcesAcceptanceOfElement() {
		Filter<Object> filter = Filter.acceptsNone().plus(42);
		assertTrue(filter.accepts(42));
	}

	@Test
	public void testPlusElementDoesNotChangeAcceptanceOfOtherElements() {
		Filter<Object> filter = Filter.acceptsNone().plus(42);
		assertFalse(filter.accepts(null));
		assertFalse(filter.accepts(13));
		assertFalse(filter.accepts(new Object()));
	}

	@Test
	public void testPlusElementWorksWithElementOfChildClass() {
		Filter.<Number>acceptsNone().plus(42);
	}

	@Test
	public void testMinusElementEnforcesRejectionOfElement() {
		Filter<Object> filter = Filter.acceptsAll().minus(42);
		assertFalse(filter.accepts(42));
	}

	@Test
	public void testMinusElementDoesNotChangeRejectionOfOtherElements() {
		Filter<Object> filter = Filter.acceptsAll().minus(42);
		assertTrue(filter.accepts(null));
		assertTrue(filter.accepts(13));
		assertTrue(filter.accepts(new Object()));
	}

	@Test
	public void testMinusElementWorksWithElementOfChildClass() {
		Filter.<Number>acceptsNone().minus(42);
	}

	@Test
	public void testIgnoreElementEnforcesNonSupportOfElement() {
		Filter<Object> filter = Filter.acceptsAll().ignore(42);
		assertNull(filter.accepts(42));
	}

	@Test
	public void testIgnoreElementDoesNotChangeAcceptanceOfOtherElements() {
		Filter<Object> filter = Filter.acceptsAll().ignore(42);
		assertTrue(filter.accepts(null));
		assertTrue(filter.accepts(13));
		assertTrue(filter.accepts(new Object()));
	}

	@Test
	public void testIgnoreElementWorksWithElementOfChildClass() {
		Filter.<Number>acceptsNone().ignore(42);
	}

	@Test
	public void testPlusAllElementsAcceptsEqualElements() {
		Filter<Object> filter = Filter.acceptsNone().plusAll(Arrays.asList(42, "test", Arrays.asList(1, 2, 3)));
		assertTrue(filter.accepts(42));
		assertTrue(filter.accepts("test"));
		assertTrue(filter.accepts(Arrays.asList(1, 2, 3)));
	}

	@Test
	public void testPlusAllElementsDoesNotChangeNonEqualElements() {
		Filter<Object> filter = Filter.acceptsNone().plusAll(Arrays.asList(42, "test", Arrays.asList(1, 2, 3)));
		assertFalse(filter.accepts(31));
		assertFalse(filter.accepts("test 2"));
		assertFalse(filter.accepts(Arrays.asList(3, 2, 1)));
	}

	@Test
	public void testPlusAllElementsReturnsEquivalentFilterOnEmptyCollection() {
		Filter<Object> filter = Filter.acceptsNone();
		assertEquals(filter, filter.plusAll(Collections.emptyList()));
	}

	@Test
	public void testPlusAllElementsThrowsExceptionOnNullCollection() {
		Filter<Object> filter = Filter.acceptsNone();
		try {
			filter.plusAll(null);
			fail("No exception thrown");
		} catch (NullPointerException cause) {
			// OK
		}
	}

	@Test
	public void testPlusAllElementsWorksWithElementsOfChildClass() {
		Filter.<Number>acceptsNone().plusAll(Arrays.<Integer>asList(1, 2, 3));
	}

	@Test
	public void testPlusNotSupportedEnforcesAcceptanceOfUnsupportedElements() {
		Filter<Object> filter = Filter.supportsNone().plusNotSupported();
		assertTrue(filter.accepts(null));
		assertTrue(filter.accepts(13));
		assertTrue(filter.accepts(new Object()));
	}

	@Test
	public void testMinusAllElementsRejectsEqualElements() {
		Filter<Object> filter = Filter.acceptsAll().minusAll(Arrays.asList(42, "test", Arrays.asList(1, 2, 3)));
		assertFalse(filter.accepts(42));
		assertFalse(filter.accepts("test"));
		assertFalse(filter.accepts(Arrays.asList(1, 2, 3)));
	}

	@Test
	public void testMinusAllElementsDoesNotChangeNonEqualElements() {
		Filter<Object> filter = Filter.acceptsAll().minusAll(Arrays.asList(42, "test", Arrays.asList(1, 2, 3)));
		assertTrue(filter.accepts(31));
		assertTrue(filter.accepts("test 2"));
		assertTrue(filter.accepts(Arrays.asList(3, 2, 1)));
	}

	@Test
	public void testMinusAllElementsReturnsEquivalentFilterOnEmptyCollection() {
		Filter<Object> filter = Filter.acceptsAll();
		assertEquals(filter, filter.minusAll(Collections.emptyList()));
	}

	@Test
	public void testMinusAllElementsThrowsExceptionOnNullCollection() {
		Filter<Object> filter = Filter.acceptsAll();
		try {
			filter.minusAll(null);
			fail("No exception thrown");
		} catch (NullPointerException cause) {
			// OK
		}
	}

	@Test
	public void testMinusAllElementsWorksWithElementsOfChildClass() {
		Filter.<Number>acceptsNone().minusAll(Arrays.<Integer>asList(1, 2, 3));
	}

	@Test
	public void testMinusNotSupportedEnforcesRejectionOfUnsupportedElements() {
		Filter<Object> filter = Filter.supportsNone().minusNotSupported();
		assertFalse(filter.accepts(null));
		assertFalse(filter.accepts(13));
		assertFalse(filter.accepts(new Object()));
	}

	@Test
	public void testIgnoreAllElementsIgnoreEqualElements() {
		Filter<Object> filter = Filter.acceptsAll().ignoreAll(Arrays.asList(42, "test", Arrays.asList(1, 2, 3)));
		assertNull(filter.accepts(42));
		assertNull(filter.accepts("test"));
		assertNull(filter.accepts(Arrays.asList(1, 2, 3)));
	}

	@Test
	public void testIgnoreAllElementsDoesNotChangeNonEqualElements() {
		Filter<Object> filter = Filter.acceptsAll().ignoreAll(Arrays.asList(42, "test", Arrays.asList(1, 2, 3)));
		assertTrue(filter.accepts(31));
		assertTrue(filter.accepts("test 2"));
		assertTrue(filter.accepts(Arrays.asList(3, 2, 1)));
	}

	@Test
	public void testIgnoreAllElementsReturnsEquivalentFilterOnEmptyCollection() {
		Filter<Object> filter = Filter.acceptsAll();
		assertEquals(filter, filter.ignoreAll(Collections.emptyList()));
	}

	@Test
	public void testIgnoreAllElementsThrowsExceptionOnNullCollection() {
		Filter<Object> filter = Filter.acceptsAll();
		try {
			filter.ignoreAll(null);
			fail("No exception thrown");
		} catch (NullPointerException cause) {
			// OK
		}
	}

	@Test
	public void testIgnoreAllElementsWorksWithElementsOfChildClass() {
		Filter.<Number>acceptsNone().ignoreAll(Arrays.<Integer>asList(1, 2, 3));
	}

	@Test
	public void testBeforeFilterUsesCurrentFilterForSupportedElements() {
		Filter<Object> filter = Filter.acceptsAll().before(Filter.acceptsNone());
		assertTrue(filter.accepts(null));
		assertTrue(filter.accepts(13));
		assertTrue(filter.accepts(new Object()));
	}

	@Test
	public void testBeforeFilterUsesArgumentFilterForNonSupportedElements() {
		Filter<Object> filter = Filter.supportsNone().before(Filter.acceptsNone());
		assertFalse(filter.accepts(null));
		assertFalse(filter.accepts(13));
		assertFalse(filter.accepts(new Object()));
	}

	@Test
	public void testBeforeFilterWorksWithFilterOfParentClass() {
		Filter.<Integer>acceptsNone().before(Filter.<Number>acceptsAll());
	}

	@Test
	public void testAfterFilterUsesArgumentFilterForSupportedElements() {
		Filter<Object> filter = Filter.acceptsAll().after(Filter.acceptsNone());
		assertFalse(filter.accepts(null));
		assertFalse(filter.accepts(13));
		assertFalse(filter.accepts(new Object()));
	}

	@Test
	public void testAfterFilterUsesCurrentFilterForNonSupportedElements() {
		Filter<Object> filter = Filter.acceptsAll().after(Filter.supportsNone());
		assertTrue(filter.accepts(null));
		assertTrue(filter.accepts(13));
		assertTrue(filter.accepts(new Object()));
	}

	@Test
	public void testAfterFilterWorksWithFilterOfParentClass() {
		Filter.<Integer>acceptsNone().after(Filter.<Number>acceptsAll());
	}

	@Test
	public void testApplyToCollectionKeepsAcceptedElements() {
		Collection<Integer> collection = Arrays.asList(1, 2, 3, 4, 5);
		Filter<Integer> filter = Filter.acceptsAll();
		assertEquals(collection, filter.applyToCollection(collection));
	}

	@Test
	public void testApplyToCollectionRemovesRejectedElements() {
		Collection<Integer> collection = Arrays.asList(1, 2, 3, 4, 5);
		Filter<Integer> filter = Filter.acceptsNone();
		assertEquals(Collections.emptyList(), filter.applyToCollection(collection));
	}

	@Test
	public void testApplyToCollectionThrowsExceptionOnNonSupportedElements() {
		Collection<Integer> collection = Arrays.asList(1, 2, 3, 4, 5);
		Filter<Integer> filter = Filter.supportsNone();
		try {
			filter.applyToCollection(collection);
			fail("No exception thrown");
		} catch (IllegalArgumentException cause) {
			// OK
		}
	}

	@Test
	public void testApplyToCollectionWorksWithFilterOnParentClass() {
		Collection<Integer> collection = Arrays.asList(1, 2, 3, 4, 5);
		Filter<Number> filter = Filter.acceptsAll();
		assertEquals(collection, filter.applyToCollection(collection));
	}

	@Test
	public void testApplyToMapKeysKeepsAcceptedEntries() {
		Map<String, Integer> map = new HashMap<>();
		map.put("a", 1);
		map.put("B", 20);
		map.put("c", 3);
		map.put("D", 40);
		map.put("e", 5);

		Filter<String> filter = Filter.acceptsAll();
		assertEquals(map, filter.applyToMapKeys(map));
	}

	@Test
	public void testApplyToMapKeysRemovesRejectedEntries() {
		Map<String, Integer> map = new HashMap<>();
		map.put("a", 1);
		map.put("B", 20);
		map.put("c", 3);
		map.put("D", 40);
		map.put("e", 5);

		Filter<String> filter = Filter.acceptsNone();
		assertEquals(new HashMap<>(), filter.applyToMapKeys(map));
	}

	@Test
	public void testApplyToMapKeysThrowsExceptionOnNonSupportedElements() {
		Map<String, Integer> map = new HashMap<>();
		map.put("a", 1);
		map.put("B", 20);
		map.put("c", 3);
		map.put("D", 40);
		map.put("e", 5);

		Filter<String> filter = Filter.supportsNone();
		try {
			filter.applyToMapKeys(map);
			fail("No exception thrown");
		} catch (IllegalArgumentException cause) {
			// OK
		}
	}

	@Test
	public void testApplyToMapKeysWorksWithFilterOnParentClass() {
		Map<String, Integer> map = new HashMap<>();
		Filter<Object> filter = Filter.acceptsAll();
		assertEquals(map, filter.applyToMapKeys(map));
	}

	@Test
	public void testApplyToMapValuesKeepsAcceptedEntries() {
		Map<String, Integer> map = new HashMap<>();
		map.put("a", 1);
		map.put("B", 20);
		map.put("c", 3);
		map.put("D", 40);
		map.put("e", 5);

		Filter<Integer> filter = Filter.acceptsAll();
		assertEquals(map, filter.applyToMapValues(map));
	}

	@Test
	public void testApplyToMapValuesRemovesRejectedEntries() {
		Map<String, Integer> map = new HashMap<>();
		map.put("a", 1);
		map.put("B", 20);
		map.put("c", 3);
		map.put("D", 40);
		map.put("e", 5);

		Filter<Integer> filter = Filter.acceptsNone();
		assertEquals(new HashMap<>(), filter.applyToMapValues(map));
	}

	@Test
	public void testApplyToMapValuesThrowsExceptionOnNonSupportedElements() {
		Map<String, Integer> map = new HashMap<>();
		map.put("a", 1);
		map.put("B", 20);
		map.put("c", 3);
		map.put("D", 40);
		map.put("e", 5);

		Filter<Integer> filter = Filter.supportsNone();
		try {
			filter.applyToMapValues(map);
			fail("No exception thrown");
		} catch (IllegalArgumentException cause) {
			// OK
		}
	}

	@Test
	public void testApplyToMapValuesWorksWithFilterOnParentClass() {
		Map<String, Integer> map = new HashMap<>();
		Filter<Number> filter = Filter.acceptsAll();
		assertEquals(map, filter.applyToMapValues(map));
	}

	@Test
	public void testApplyToStreamKeepsAcceptedEntries() {
		Stream<Integer> stream = Arrays.asList(1, 2, 3, 4, 5).stream();
		Filter<Integer> filter = Filter.acceptsAll();
		Stream<Integer> result = filter.applyToStream(stream);
		List<Integer> list = result.collect(Collectors.toList());
		assertEquals(Arrays.asList(1, 2, 3, 4, 5), list);
	}

	@Test
	public void testApplyToStreamRemovesRejectedEntries() {
		Stream<Integer> stream = Arrays.asList(1, 2, 3, 4, 5).stream();
		Filter<Integer> filter = Filter.acceptsNone();
		Stream<Integer> result = filter.applyToStream(stream);
		List<Integer> list = result.collect(Collectors.toList());
		assertEquals(Collections.emptyList(), list);
	}

	@Test
	public void testApplyToStreamThrowsExceptionOnNonSupportedElements() {
		Stream<Integer> stream = Arrays.asList(1, 2, 3, 4, 5).stream();
		Filter<Integer> filter = Filter.supportsNone();
		Stream<Integer> result = filter.applyToStream(stream);
		try {
			result.collect(Collectors.toList());
			fail("No exception thrown");
		} catch (IllegalStateException cause) {
			// OK
		}
	}

	@Test
	public void testApplyToStreamWorksWithFilterOnParentClass() {
		Stream<Integer> stream = Arrays.asList(1, 2, 3, 4, 5).stream();
		Filter<Number> filter = Filter.acceptsAll();
		filter.applyToStream(stream);
	}

	@Test
	public void testReverseAcceptsRejectedElements() {
		Filter<Object> filter = Filter.acceptsAll().reverse();
		assertFalse(filter.accepts(null));
		assertFalse(filter.accepts(42));
		assertFalse(filter.accepts(new Object()));
	}

	@Test
	public void testReverseRejectsAcceptedElements() {
		Filter<Object> filter = Filter.acceptsNone().reverse();
		assertTrue(filter.accepts(null));
		assertTrue(filter.accepts(42));
		assertTrue(filter.accepts(new Object()));
	}

	@Test
	public void testReverseDoesNotchangeNonSupportedElements() {
		Filter<Object> filter = Filter.supportsNone().reverse();
		assertNull(filter.accepts(null));
		assertNull(filter.accepts(42));
		assertNull(filter.accepts(new Object()));
	}
}
