/*
 * Copyright 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.cloud.tools.test.utils;

import com.google.common.base.Preconditions;

import org.mockito.Mockito;
import org.mockito.exceptions.base.MockitoAssertionError;
import org.mockito.invocation.Invocation;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpyVerifier {
  private final Object objectToInspect;
  private final Class classToInspectAs;

  public static SpyVerifier newVerifier(Object objectUnderInspection) {
    return new SpyVerifier(objectUnderInspection);
  }

  private SpyVerifier(Object objectUnderInspection) {
    Preconditions.checkArgument(Mockito.mockingDetails(objectUnderInspection).isSpy());
    this.objectToInspect = objectUnderInspection;
    this.classToInspectAs = Mockito.mockingDetails(objectToInspect).getMockCreationSettings()
        .getTypeToMock();
  }

  /**
   * This method is to verify that all setters in a configuration were called. The motivation is
   * to ensure that configurations are fully built and we can ensure our handling is being
   * properly tested.
   */
  public SpyVerifier verifyDeclaredSetters() throws Exception {
    // extract all invocations of getters by inspecting the spy
    List<Method> knownSetters = Arrays.asList(classToInspectAs.getDeclaredMethods());

    Map<Method, Integer> methodInvocationCount = new HashMap<>();
    for (Invocation invocation : Mockito.mockingDetails(objectToInspect).getInvocations()) {
      Method m = invocation.getMethod();
      if (knownSetters.contains(m) && isSetter(m)) {
        if (methodInvocationCount.containsKey(m)) {
          methodInvocationCount.put(m, methodInvocationCount.get(m) + 1);
        }
        else {
          methodInvocationCount.put(m, 1);
        }
        invocation.markVerified();
      }
    }

    // compare setter invocations against our expections
    for (Method m : knownSetters) {
      if (isSetter(m)) {
        Integer invocationCount = methodInvocationCount.get(m);
        if (invocationCount == null || invocationCount != 1) {
          throw new MockitoAssertionError("Setter invocations for '"
              + m.getName()
              + "' expected 1, but was "
              + invocationCount);
        }
      }
    }
    return this;
  }

  private boolean isSetter(Method m) {
    return !m.isSynthetic() && Modifier.isPublic(m.getModifiers()) && m.getName().startsWith("set");
  }


  public SpyVerifier verifyDeclaredGetters() throws Exception {
    return verifyDeclaredGetters(Collections.<String, Integer>emptyMap());
  }

  /**
   * Verify getters were called once or if in the override map, called 'count' times
   * @param overrides Override default counts in the style ["getterName" : count]
   */
  public SpyVerifier verifyDeclaredGetters(Map<String, Integer> overrides) throws Exception {
    Method[] methods = classToInspectAs.getDeclaredMethods();
    for (Method m : methods) {
      if (!m.isSynthetic() && Modifier.isPublic(m.getModifiers())
          && m.getName().startsWith("get")) {
        Integer times = overrides.get(m.getName());
        times = (times == null) ? 1 : times;
        Mockito.verify(objectToInspect, Mockito.times(times)).getClass().getMethod(m.getName())
            .invoke(objectToInspect);
      }
    }
    return this;
  }
}
