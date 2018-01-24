/*
 * Copyright 2018 Google Inc.
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

package com.google.cloud.tools.managedcloudsdk.textbars;

import com.google.cloud.tools.managedcloudsdk.MessageCollector;
import org.junit.Assert;
import org.junit.Test;

public class TextInfoBarTest {

  @Test
  public void testShow_shortMessage() {
    MessageCollector collector = new MessageCollector();
    new TextInfoBar(collector, "A short message").show();

    Assert.assertEquals(
        "#= A short message                                          =#\n", collector.getOutput());
  }

  @Test
  public void testShow_longMessage() {
    MessageCollector collector = new MessageCollector();
    new TextInfoBar(
            collector, "A message that is very long and really really needs to be truncated")
        .show();

    Assert.assertEquals(
        "#= A message that is very long and really really needs t... =#\n", collector.getOutput());
  }
}
