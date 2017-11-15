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

package com.google.cloud.tools.managedcloudsdk.update;

import com.google.cloud.tools.managedcloudsdk.MessageListener;
import com.google.cloud.tools.managedcloudsdk.MessageListenerForwardingHandler;
import com.google.cloud.tools.managedcloudsdk.process.AsyncStreamHandler;
import com.google.cloud.tools.managedcloudsdk.process.CommandExecutorFactory;
import com.google.cloud.tools.managedcloudsdk.process.StreamConsumerFactory;
import java.nio.file.Path;

/** {@link Updater} Factory. */
public final class UpdaterFactory {

  /**
   * Returns a new {@link Updater} instance.
   *
   * @param gcloud path to the Cloud SDK directory
   * @param messageListener listener on installer script output
   * @return a {@link Updater} instance.
   */
  Updater newUpdater(Path gcloud, MessageListener messageListener) {

    return new Updater(
        gcloud,
        messageListener,
        new CommandExecutorFactory(),
        new AsyncStreamHandler<>(
            new StreamConsumerFactory<>(new MessageListenerForwardingHandler(messageListener))),
        new AsyncStreamHandler<>(
            new StreamConsumerFactory<>(new MessageListenerForwardingHandler(messageListener))));
  }
}
