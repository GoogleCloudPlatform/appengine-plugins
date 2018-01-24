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

public class TextProgressBarTest {

  @Test
  public void testTextProgressBar_simple() {
    MessageCollector collector = new MessageCollector();
    // test should reflect every 2 progress updates as a "bar"
    TextProgressBar testProgressBar = new TextProgressBar(collector, 58 * 2);

    testProgressBar.start();
    Assert.assertEquals("#=", collector.getOutput());

    testProgressBar.update(1); // 1 (0 bars)
    Assert.assertEquals("#=", collector.getOutput());

    testProgressBar.update(1); // 2  (1 bar)
    Assert.assertEquals("#==", collector.getOutput());

    testProgressBar.update(56); // 58 (29 bars) -- half way
    Assert.assertEquals("#==============================", collector.getOutput());

    testProgressBar.update(58); // 114 (all bars)
    Assert.assertEquals(
        "#===========================================================", collector.getOutput());

    testProgressBar.done();
    Assert.assertEquals(
        "#============================================================#\n", collector.getOutput());
  }

  @Test
  public void testProgressBar_smallTotals() {
    MessageCollector collector = new MessageCollector();
    TextProgressBar testProgressBar = new TextProgressBar(collector, 2);

    testProgressBar.start();
    Assert.assertEquals("#=", collector.getOutput());

    testProgressBar.update(1); // (should be half full bar)
    Assert.assertEquals("#==============================", collector.getOutput());

    testProgressBar.update(1); // (should be full bar)
    Assert.assertEquals(
        "#===========================================================", collector.getOutput());

    testProgressBar.done();
    Assert.assertEquals(
        "#============================================================#\n", collector.getOutput());
  }

  @Test
  public void testProgressBar_doesntPassEnd() {
    MessageCollector collector = new MessageCollector();
    TextProgressBar testProgressBar = new TextProgressBar(collector, 10);

    testProgressBar.start();
    Assert.assertEquals("#=", collector.getOutput());

    testProgressBar.update(20); // max is 10 units.
    Assert.assertEquals(
        "#===========================================================", collector.getOutput());

    testProgressBar.done();
    Assert.assertEquals(
        "#============================================================#\n", collector.getOutput());
  }

  @Test
  public void testDone_fillsOutBar() {
    MessageCollector collector = new MessageCollector();
    TextProgressBar testProgressBar = new TextProgressBar(collector, 58);

    testProgressBar.start();
    Assert.assertEquals("#=", collector.getOutput());

    testProgressBar.done();
    Assert.assertEquals(
        "#============================================================#\n", collector.getOutput());
  }
}
