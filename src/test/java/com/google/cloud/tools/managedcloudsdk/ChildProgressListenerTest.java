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

package com.google.cloud.tools.managedcloudsdk;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class ChildProgressListenerTest {

  @Mock ProgressListener mockParent;

  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testChildProgressListener_normalUse() {
    ChildProgressListener testListener = new ChildProgressListener(mockParent, 100);
    testListener.start("start", 100);
    testListener.update(10);
    testListener.update(80);
    testListener.update(10);
    testListener.done();

    InOrder verifier = Mockito.inOrder(mockParent);
    verifier.verify(mockParent).update("start");
    verifier.verify(mockParent).update(10);
    verifier.verify(mockParent).update(80);
    verifier.verify(mockParent).update(10);
    verifier.verifyNoMoreInteractions();
  }

  @Test
  public void testChildProgressListener_unfinished() {
    ChildProgressListener testListener = new ChildProgressListener(mockParent, 100);
    testListener.start("start", 100);
    testListener.update(10);
    testListener.done();

    InOrder verifier = Mockito.inOrder(mockParent);
    verifier.verify(mockParent).update("start");
    verifier.verify(mockParent).update(10);
    verifier.verify(mockParent).update(90);
    verifier.verifyNoMoreInteractions();
  }

  @Test
  public void testChildProgressListener_fractional() {
    ChildProgressListener testListener = new ChildProgressListener(mockParent, 100);

    // allocation multiplier is 0.5 (100/200)
    testListener.start("start", 200);

    // trigger 5
    testListener.update(10);

    // trigger 1, combine two update(1)s
    testListener.update(1);
    testListener.update(1);

    // trigger 2, carry over 0.5
    testListener.update(5);

    // trigger 2, (use carry-over)
    testListener.update(3);

    // trigger 90
    testListener.done();

    InOrder verifier = Mockito.inOrder(mockParent);
    verifier.verify(mockParent).update("start");
    verifier.verify(mockParent).update(5);
    verifier.verify(mockParent).update(1);
    verifier.verify(mockParent, Mockito.times(2)).update(2);
    verifier.verify(mockParent).update(90);
    verifier.verifyNoMoreInteractions();
  }

  @Test
  public void testChildProgressListener_inverseFractional() {
    ChildProgressListener testListener = new ChildProgressListener(mockParent, 100);

    // allocation multiplier is 2 (100/50)
    testListener.start("start", 50);

    // trigger 20
    testListener.update(10);

    // trigger 30
    testListener.update(15);

    // trigger 50
    testListener.done();

    InOrder verifier = Mockito.inOrder(mockParent);
    verifier.verify(mockParent).update("start");
    verifier.verify(mockParent).update(20);
    verifier.verify(mockParent).update(30);
    verifier.verify(mockParent).update(50);
    verifier.verifyNoMoreInteractions();
  }

  @Test
  public void testChildProgressListener_unknown() {
    ChildProgressListener testListener = new ChildProgressListener(mockParent, 100);

    testListener.start("start", ProgressListener.UNKNOWN);
    testListener.update(10);
    testListener.update(80);
    testListener.update(10);
    // should only update parent at done
    testListener.done();

    InOrder verifier = Mockito.inOrder(mockParent);
    verifier.verify(mockParent).update("start");
    verifier.verify(mockParent).update(100);
    verifier.verifyNoMoreInteractions();
  }

  @Test
  public void testChildProgressListener_doneBeforeAnything() {
    ChildProgressListener testListener = new ChildProgressListener(mockParent, 100);

    try {
      testListener.done();
      Assert.fail("expected illegal argument exception");
    } catch (IllegalArgumentException ex) {
      // pass
    }
  }

  @Test
  public void testChildProgressListener_startAfterStart() {
    ChildProgressListener testListener = new ChildProgressListener(mockParent, 100);

    testListener.start("asdf", 100);
    try {
      testListener.start("asdf", 100);
      Assert.fail("expected illegal argument exception");
    } catch (IllegalArgumentException ex) {
      // pass
    }
  }

  @Test
  public void testChildProgressListener_updateWorkBeforeStart() {
    ChildProgressListener testListener = new ChildProgressListener(mockParent, 100);

    try {
      testListener.update(100);
      Assert.fail("expected illegal argument exception");
    } catch (IllegalArgumentException ex) {
      // pass
    }
  }

  @Test
  public void testChildProgressListener_updateMessageBeforeStart() {
    ChildProgressListener testListener = new ChildProgressListener(mockParent, 100);

    try {
      testListener.update("hello");
      Assert.fail("expected illegal argument exception");
    } catch (IllegalArgumentException ex) {
      // pass
    }
  }

  @Test
  public void testChildProgressListener_updateWorkAfterDone() {
    ChildProgressListener testListener = new ChildProgressListener(mockParent, 100);

    testListener.start("asdf", 100);
    testListener.done();
    try {
      testListener.update(100);
      Assert.fail("expected illegal argument exception");
    } catch (IllegalArgumentException ex) {
      // pass
    }
  }

  @Test
  public void testChildProgressListener_updateMessageAfterDone() {
    ChildProgressListener testListener = new ChildProgressListener(mockParent, 100);

    testListener.start("asdf", 100);
    testListener.done();
    try {
      testListener.update("hello");
      Assert.fail("expected illegal argument exception");
    } catch (IllegalArgumentException ex) {
      // pass
    }
  }

  @Test
  public void testChildProgressListener_startAfterDone() {
    ChildProgressListener testListener = new ChildProgressListener(mockParent, 100);

    testListener.start("asdf", 100);
    testListener.done();
    try {
      testListener.start("asdf", 1000);
      Assert.fail("expected illegal argument exception");
    } catch (IllegalArgumentException ex) {
      // pass
    }
  }
}
