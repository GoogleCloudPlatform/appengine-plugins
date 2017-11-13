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

package com.google.cloud.tools.managedcloudsdk.process;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/** Tests for {@link StreamByteConsumer}. */
public class StreamByteConsumerTest {

  private static final String TEST_STRING = "test line1\ntest line2\n";

  @Mock private ByteHandler<Void> byteHandler;
  private InputStream inputStream = new ByteArrayInputStream(TEST_STRING.getBytes());

  @Before
  public void setupMocks() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testCall_smokeTest() throws Exception {
    StreamByteConsumer<Void> testConsumer = new StreamByteConsumer<>(inputStream, byteHandler);
    testConsumer.call();
    ArgumentCaptor<byte[]> bytes = ArgumentCaptor.forClass(byte[].class);
    ArgumentCaptor<Integer> nBytes = ArgumentCaptor.forClass(Integer.class);
    Mockito.verify(byteHandler, Mockito.atLeastOnce()).bytes(bytes.capture(), nBytes.capture());

    int count = bytes.getAllValues().size();
    StringBuilder result = new StringBuilder("");
    for (int i = 0; i < count; i++) {
      result.append(new String(bytes.getAllValues().get(i), 0, nBytes.getAllValues().get(i)));
    }
    Assert.assertEquals(TEST_STRING, result.toString());

    Mockito.verify(byteHandler).getResult();
    Mockito.verifyNoMoreInteractions(byteHandler);
  }
}
