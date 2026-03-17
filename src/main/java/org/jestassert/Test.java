package org.jestassert;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
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

  record Executable(Class<? extends Test> test, String scenario, Method method) {}

  interface Observer {
    void before(Executable e);

    void after(List<AssertionError> errors);
  }

  static void run(Class<? extends Test> test) {
    run(
        test,
        new Observer() {
          @Override
          public void before(Executable e) {
            System.out.println(e.scenario);
          }

          @Override
          public void after(List<AssertionError> errors) {
            for (AssertionError e : errors) System.out.println("\tfailed: " + e);
          }
        });
  }

  static void run(Class<? extends Test> test, Observer observer) {
    Runner runner = new Runner(test);
    runner.observer = observer;
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
                    runner.beforeScenario(method, args);
                  }
                  if (args != null && args.length > 0)
                    return getDefaultMethodHandle(method).bindTo(proxy).invokeWithArguments(args);
                  return getDefaultMethodHandle(method).bindTo(proxy).invoke();
                });
    t.run();
  }

  class Runner implements Test {

    final Class<? extends Test> test;
    String name;
    String parameter;
    Observer observer;
    List<AssertionError> errors = new ArrayList<>();

    public Runner(Class<? extends Test> test) {
      this.test = test;
    }

    public void beforeScenario(Method method, Object[] args) {
      String scenario = name == null ? method.getName() : name;
      scenario += parameter == null ? "()" : "(" + parameter + ")";
      Executable e = new Executable(test, scenario, method);
      if (observer != null) observer.before(e);
    }

    @Override
    public void fail(AssertionError error) {
      errors.add(error);
    }

    private void fail(Exception ex) {
      String cause = "Oh boy, that escalated quickly: " + ex.getMessage();
      fail(new AssertionError("?", new UnexpectedException(ex), cause));
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
      afterScenario();
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
      afterScenario();
    }

    private void afterScenario() {
      if (observer != null) observer.after(List.copyOf(errors));
      errors.clear();
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
        afterScenario();
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
