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

public class ServiceNameValidatorTest {

  @Test
  public void testDomain() {
    Assert.assertFalse(ServiceNameValidator.validate("google.com:mystore"));
  }

  @Test
  public void testPartition() {
    Assert.assertFalse(ServiceNameValidator.validate("s~google.com:mystore"));
  }

  @Test
  public void testOneWord() {
    Assert.assertTrue(ServiceNameValidator.validate("word"));
  }

  @Test
  public void testUpperCase() {
    Assert.assertTrue(ServiceNameValidator.validate("WORD"));
  }

  @Test
  public void testLongWord() {
    boolean validate =
        ServiceNameValidator.validate(
            "012345678901234567890123456789012345678901234567890123456789"
                + "012345678901234567890123456789012345678901234567890");
    Assert.assertFalse(validate);
  }

  @Test
  public void testContainsSpace() {
    Assert.assertFalse(ServiceNameValidator.validate("com google eclipse"));
  }

  @Test
  public void testEmptyString() {
    Assert.assertFalse(ServiceNameValidator.validate(""));
  }

  @Test
  public void testNull() {
    Assert.assertFalse(ServiceNameValidator.validate(null));
  }

  @Test
  public void testBeginsWithHyphen() {
    Assert.assertFalse(ServiceNameValidator.validate("-foo"));
  }

  @Test
  public void testEndsWithHyphen() {
    Assert.assertFalse(ServiceNameValidator.validate("-bar"));
  }

  @Test
  public void testContainsHyphen() {
    Assert.assertTrue(ServiceNameValidator.validate("foo-bar"));
  }
}
