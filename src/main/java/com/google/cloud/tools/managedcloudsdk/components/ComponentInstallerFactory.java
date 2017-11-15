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

package com.google.cloud.tools.managedcloudsdk.components;

import com.google.cloud.tools.managedcloudsdk.MessageListener;
import com.google.cloud.tools.managedcloudsdk.MessageListenerForwardingHandler;
import com.google.cloud.tools.managedcloudsdk.process.AsyncStreamHandler;
import com.google.cloud.tools.managedcloudsdk.process.CommandExecutorFactory;
import com.google.cloud.tools.managedcloudsdk.process.StreamConsumerFactory;
import java.nio.file.Path;

/** {@link ComponentInstaller} Factory. */
final class ComponentInstallerFactory {

  /**
   * Returns a new {@link ComponentInstaller} instance.
   *
   * @param gcloud full path to the gcloud executable
   * @param messageListener listener on installer script output
   * @return a {@link ComponentInstaller} instance.
   */
  ComponentInstaller newInstaller(
      Path gcloud, SdkComponent component, MessageListener messageListener) {

    return new ComponentInstaller(
        gcloud,
        component,
        messageListener,
        new CommandExecutorFactory(),
        new AsyncStreamHandler<>(
            new StreamConsumerFactory<>(new MessageListenerForwardingHandler(messageListener))),
        new AsyncStreamHandler<>(
            new StreamConsumerFactory<>(new MessageListenerForwardingHandler(messageListener))));
  }
}
