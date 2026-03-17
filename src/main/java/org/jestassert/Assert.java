package org.jestassert;

import java.util.function.Consumer;

public interface Assert {

  record AssertionError(Object expected, Object actual, String cause) {
    public boolean thrownUnexpectedException() {
      return actual instanceof UnexpectedException;
    }
  }

  void fail(AssertionError error);

  default void assertEquals(char expected, char actual) {
    assertEquals("" + expected, "" + actual);
  }

  default void assertEquals(String expected, String actual) {
    if (expected == null && actual == null) return;
    if (actual == null) {
      fail(new AssertionError(expected, null, "NPEs are bad"));
    } else {
      if (!actual.equals(expected))
        fail(new AssertionError(expected, actual, "apples and pears, anyone?"));
    }
  }

  default void assertNotNull(Object actual, Runnable thenAssert) {
    assertNotNull(actual, "we got a lemon, boys!", thenAssert);
  }

  default void assertNotNull(Object actual, String message, Runnable thenAssert) {
    if (actual == null) {
      fail(new AssertionError("not null", null, message));
    } else {
      thenAssert.run();
    }
  }

  default <T> void assertInstanceOf(Class<T> expected, Object actual, Consumer<T> thenAssert) {
    if (!expected.isInstance(actual)) {
      fail(new AssertionError(expected, actual, "was of type: " + actual.getClass()));
    } else {
      thenAssert.accept(expected.cast(actual));
    }
  }

  final class UnexpectedException extends RuntimeException {
    public UnexpectedException(Throwable cause) {
      super(cause);
    }

    @Override
    public String toString() {
      Throwable cause = getCause();
      return cause == null ? "nothing" : cause.getClass().getSimpleName();
    }
  }
}
