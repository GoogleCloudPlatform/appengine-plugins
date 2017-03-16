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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
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

  public void verifyDeclaredGetters() throws Exception {
    verifyDeclaredGetters(Collections.<String, Integer>emptyMap());
  }

  /**
   * Verify getters were called once or if in the exception map, called 'count' times
   * @param exceptions Exceptions of the form ["getter name" : count]
   */
  public void verifyDeclaredGetters(Map<String, Integer> exceptions) throws Exception {
    Method[] methods = classToInspectAs.getDeclaredMethods();
    for (Method m : methods) {
      if (!m.isSynthetic() && Modifier.isPublic(m.getModifiers())
          && m.getName().startsWith("get")) {
        Integer times = exceptions.get(m.getName());
        times = (times == null) ? 1 : times;
        Mockito.verify(objectToInspect, Mockito.times(times)).getClass().getMethod(m.getName())
            .invoke(objectToInspect);
      }
    }
  }
}
