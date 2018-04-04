/*
 * Copyright 2016 Google Inc.
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

package com.google.cloud.tools.project;

import org.junit.Assert;
import org.junit.Test;

public class ProjectIdValidatorTest {

  @Test
  public void testDomain() {
    Assert.assertTrue(ProjectIdValidator.validate("google.com:mystore"));
  }

  @Test
  public void testPartition() {
    Assert.assertTrue(ProjectIdValidator.validate("s~google.com:mystore"));
  }

  @Test
  public void testOneWord() {
    Assert.assertTrue(ProjectIdValidator.validate("word"));
  }

  @Test
  public void testUpperCase() {
    Assert.assertTrue(ProjectIdValidator.validate("WORD"));
  }

  @Test
  public void testLongWord() {
    boolean validate =
        ProjectIdValidator.validate(
            "012345678901234567890123456789012345678901234567890123456789"
                + "012345678901234567890123456789012345678901234567890");
    Assert.assertFalse(validate);
  }

  @Test
  public void testContainsSpace() {
    Assert.assertFalse(ProjectIdValidator.validate("com google eclipse"));
  }

  @Test
  public void testEmptyString() {
    Assert.assertFalse(ProjectIdValidator.validate(""));
  }

  @Test
  public void testNull() {
    Assert.assertFalse(ProjectIdValidator.validate(null));
  }
}
