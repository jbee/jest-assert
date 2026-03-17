package org.jestassert;

import java.util.List;

public interface MyTest extends Test, MyAssert {

  static void main(String[] args) {
    Test.run(MyTest.class);
  }

  default void run() {
    // whatever setup
    run(this::testSomething);
    run("any text I want, eh?", this::testSomethingElse);
    // do some more setup or whatever...
    run(this::testSomethingElseWith, List.of("foo", "bar", "hi", "my dude", "mister"));
    run(this::testMore);
    run(this::testNullStuff);
  }

  default void testSomething() {
    assertEquals("foo", "bar");
  }

  default void testSomethingElse() {
    assertEquals("foo", "bar");
  }

  default void testSomethingElseWith(String name) {
    assertInRange(3, 5, name.length());
    assertEquals("foo", name);
  }

  default void testMore() {
    assertSomething("year", 42);
  }

  default void testNullStuff() {
    String actual = null;
    assertNotNull(actual, () -> {
      assertEquals("", actual);
      assertInRange(1,4, actual.length());
    });
    assertEquals("foo", null);
  }
}
