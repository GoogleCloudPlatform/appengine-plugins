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

package com.google.cloud.tools.managedcloudsdk.command;

import com.google.cloud.tools.managedcloudsdk.MessageListener;
import com.google.cloud.tools.managedcloudsdk.process.AsyncStreamHandler;
import com.google.common.base.Charsets;

/** Factory to create default implementations of {@link AsyncStreamHandler}. */
class AsyncStreamHandlerFactory {

  /**
   * Create a new AsyncStreamHandler using the {@link MessageListenerForwardingHandler}
   * implementation.
   */
  AsyncStreamHandler newHandler(MessageListener messageListener) {
    return new AsyncByteConsumer(new MessageListenerForwardingHandler(messageListener));
  }

  static class MessageListenerForwardingHandler implements ByteHandler {

    private final MessageListener messageListener;

    MessageListenerForwardingHandler(MessageListener messageListener) {
      this.messageListener = messageListener;
    }

    @Override
    public void bytes(byte[] bytes, int length) {
      messageListener.message(new String(bytes, 0, length, Charsets.UTF_8));
    }

    @Override
    public String getResult() {
      return null;
    }
  }
}
