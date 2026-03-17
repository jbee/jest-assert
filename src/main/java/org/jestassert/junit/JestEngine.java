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
    System.out.println(candidate + " is a Test? " + result);
    return result;
  }

  @Override
  public boolean isTestMethod(Method method) {
    return false;
  }

  @Override
  public void execute(EngineExecutionListener listener, TestClass testClass) {
    // TODO listener.executionStarted(method);
    Test.run((Class<? extends Test>) testClass.getTestClass());
    // TODO TestExecutionResult result = execute(testInstance, (TestMethod) method);
    // TODO listener.executionFinished(method, result);
  }
}
