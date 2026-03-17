package org.jestassert;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

public interface Test extends Assert {

  @FunctionalInterface
  interface Scenario {
    void run() throws Exception;
  }

  @FunctionalInterface
  interface ScenarioWith<T> {
    void run(T value) throws Exception;
  }

  void run();

  void run(Scenario scenario);

  void run(String name, Scenario scenario);

  <T> void run(ScenarioWith<T> scenario, List<T> parameters);










  static void run(Class<? extends Test> test) {
    Runner runner = new Runner();
    Test t =
        (Test)
            Proxy.newProxyInstance(
                Thread.currentThread().getContextClassLoader(),
                new Class[] {test, Test.class},
                (proxy, method, args) -> {
                  String name = method.getName();
                  if ((name.equals("run") || name.equals("fail")) && method.getParameterCount() > 0)
                    return getCoreApiMethodHandle(method).bindTo(runner).invokeWithArguments(args);

                  if (method.getName().startsWith("test")) {
                    String testCase = runner.name == null ? method.getName() : runner.name;
                    testCase += runner.parameter == null ? "()" : "(" + runner.parameter + ")";
                    System.out.println(testCase);
                  }

                  if (args != null && args.length > 0)
                    return getDefaultMethodHandle(method).bindTo(proxy).invokeWithArguments(args);
                  return getDefaultMethodHandle(method).bindTo(proxy).invoke();
                });
    t.run();
  }

  class Runner implements Test {

    String name;
    String parameter;

    @Override
    public void fail(AssertionError error) {
      System.out.println("\tfailed: " + error);
      // TODO flag to ignore asserts until next run
    }

    private void fail(Exception ex) {
      fail(
          new AssertionError(
              ex, null, null, "Unexpected exception was thrown: " + ex.getMessage()));
    }

    @Override
    public void run() {}

    @Override
    public void run(Scenario scenario) {
      name = null;
      parameter = null;
      try {
        scenario.run();
      } catch (Exception e) {
        fail(e);
      }
    }

    @Override
    public void run(String name, Scenario scenario) {
      this.name = name;
      parameter = null;
      try {
        scenario.run();
      } catch (Exception e) {
        fail(e);
      }
    }

    @Override
    public <T> void run(ScenarioWith<T> scenario, List<T> parameters) {
      name = null;
      for (T e : parameters) {
        try {
          parameter = e.toString();
          scenario.run(e);
        } catch (Exception ex) {
          fail(ex);
        }
      }
    }
  }

  private static MethodHandle getCoreApiMethodHandle(Method m) {
    try {
      return MethodHandles.lookup().unreflect(m);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  private static MethodHandle getDefaultMethodHandle(Method method) {
    try {
      Class<?> declaringClass = method.getDeclaringClass();
      return MethodHandles.lookup()
          .findSpecial(
              declaringClass,
              method.getName(),
              MethodType.methodType(method.getReturnType(), method.getParameterTypes()),
              declaringClass);
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }
}
