/*
 * Copyright 2017 Google LLC.
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

package com.google.cloud.tools.managedcloudsdk.command;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AsyncByteConsumerTest {

  private static final String TEST_STRING = "test line1\ntest line2\n";
  private final InputStream fakeInputStream =
      new ByteArrayInputStream(TEST_STRING.getBytes(StandardCharsets.UTF_8));

  @Mock private ExecutorService executorService;
  @Mock private ByteHandler mockByteHandler;
  @Mock private InputStream mockInputStream;

  private SettableFuture<String> future = SettableFuture.create();

  @Test
  public void testHandleStream() {
    Mockito.when(executorService.isShutdown()).thenReturn(false);

    ListeningExecutorService listeningExecutorService =
        MoreExecutors.listeningDecorator(executorService);

    AsyncByteConsumer consumer =
        new AsyncByteConsumer(mockByteHandler, listeningExecutorService, future);
    consumer.handleStream(mockInputStream);

    Mockito.verify(executorService).isShutdown();
    Mockito.verify(executorService).execute(Mockito.<Runnable>any());
    Mockito.verify(executorService).shutdown();
    Mockito.verifyNoMoreInteractions(executorService);
  }

  @Test
  public void testHandleStream_failIfReused() {
    Mockito.when(executorService.isShutdown()).thenReturn(true);
    ListeningExecutorService listeningExecutorService =
        MoreExecutors.listeningDecorator(executorService);

    try {
      new AsyncByteConsumer(mockByteHandler, listeningExecutorService, future)
          .handleStream(mockInputStream);
      Assert.fail("IllegalStateException expected but not thrown");
    } catch (IllegalStateException ex) {
      // pass
      Assert.assertEquals("Cannot reuse " + AsyncByteConsumer.class.getName(), ex.getMessage());
    }
  }

  @Test
  public void testConsumeBytes() throws Exception {
    ListeningExecutorService listeningExecutorService =
        MoreExecutors.listeningDecorator(executorService);
    new AsyncByteConsumer(mockByteHandler, listeningExecutorService, future)
        .consumeBytes(fakeInputStream);

    ArgumentCaptor<byte[]> bytes = ArgumentCaptor.forClass(byte[].class);
    ArgumentCaptor<Integer> nBytes = ArgumentCaptor.forClass(Integer.class);
    Mockito.verify(mockByteHandler, Mockito.atLeastOnce()).bytes(bytes.capture(), nBytes.capture());

    int count = bytes.getAllValues().size();
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < count; i++) {
      byte[] rawBytes = bytes.getAllValues().get(i);
      int length = nBytes.getAllValues().get(i);
      result.append(new String(rawBytes, 0, length, StandardCharsets.UTF_8));
    }
    Assert.assertEquals(TEST_STRING, result.toString());

    Mockito.verify(mockByteHandler).getResult();
    Mockito.verifyNoMoreInteractions(mockByteHandler);
  }
}
