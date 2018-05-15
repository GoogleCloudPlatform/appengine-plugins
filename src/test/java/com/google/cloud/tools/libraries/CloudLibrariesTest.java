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

package com.google.cloud.tools.libraries;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.MissingResourceException;
import org.junit.Test;

/** Unit tests for {@link CloudLibraries}. */
public final class CloudLibrariesTest {

  @Test
  public void getCloudLibraries_returnsList() throws IOException {
    assertFalse(CloudLibraries.getCloudLibraries().isEmpty());
  }

  @Test
  public void getLibraries_withMissingFile_throwsException() throws IOException {
    CloudLibraries cloudLibraries = new CloudLibraries("does-not.exist");
    try {
      cloudLibraries.getLibraries();
      fail("Expected MissingResourceException to be thrown.");
    } catch (MissingResourceException ex) {
      assertEquals("Resource not found when loading libraries", ex.getMessage());
    }
  }
}
