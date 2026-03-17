package org.jestassert.junit;

import java.lang.reflect.Method;
import org.jestassert.Test;
import org.junit.platform.engine.EngineExecutionListener;

public class JestEngine extends AbstractClassBasedTestEngine {
  @Override
  public String getId() {
    return "jest-assert";
  }

  @Override
  public boolean isTestClass(Class<?> candidate) {
    if (candidate == Test.class) return false;
    if (candidate.getEnclosingClass() != null) return false;
    var result = Test.class.isAssignableFrom(candidate);
    return result;
  }

  @Override
  public boolean isTestMethod(Method method) {
    return false;
  }

  @Override
  public void execute(EngineExecutionListener listener, TestClass testClass) {
    Test.run((Class<? extends Test>) testClass.getTestClass());
    // onStart, onEnd(List<AssertionError>)
    //Test.discover(class, test -> {

    //});
    // TODO TestExecutionResult result = execute(testInstance, (TestMethod) method);
  }
}
