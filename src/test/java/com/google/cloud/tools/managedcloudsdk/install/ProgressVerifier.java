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

package com.google.cloud.tools.managedcloudsdk.install;

import com.google.cloud.tools.managedcloudsdk.ProgressListener;
import org.junit.Assert;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

public class ProgressVerifier {
  public static void verifyProgress(ProgressListener mockProgressListener, String message) {
    ArgumentCaptor<Long> totalProgressCaptor = ArgumentCaptor.forClass(Long.class);
    Mockito.verify(mockProgressListener).start(Mockito.eq(message), totalProgressCaptor.capture());

    ArgumentCaptor<Long> updateCaptor = ArgumentCaptor.forClass(Long.class);
    Mockito.verify(mockProgressListener, Mockito.atLeastOnce()).update(updateCaptor.capture());

    long total = totalProgressCaptor.getValue();
    long sum = 0;
    for (long update : updateCaptor.getAllValues()) {
      sum += update;
    }

    Assert.assertEquals(total, sum);

    Mockito.verify(mockProgressListener).done();
    Mockito.verifyNoMoreInteractions(mockProgressListener);
  }

  public static void verifyUnknownProgress(ProgressListener mockProgressListener, String message) {
    Mockito.verify(mockProgressListener).start(message, -1);
    // any update calls are irrelevant, they may or may not be called
    Mockito.verify(mockProgressListener).done();
  }
}
