package org.jestassert;

import org.jestassert.Assert;

public interface MyAssert extends Assert {

  record Range(int min, int max) {}

  default void assertInRange(int min, int max, int actual) {
    if (actual < min || actual > max) {
      String cause = "out of range";
      if (actual+1 == min || actual-1 == max) cause = "off by 1, classic!";
      fail(new AssertionError(new Range(min, max), actual, cause));
    }
  }

  default void assertSomething(String expected, Object actual) {
    assertNotNull(
        actual,
        () -> assertInstanceOf(expected.getClass(), actual, a -> assertEquals(expected, a)));
  }
}
