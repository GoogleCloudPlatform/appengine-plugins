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
import com.google.cloud.tools.managedcloudsdk.process.AsyncByteConsumer;
import com.google.cloud.tools.managedcloudsdk.process.AsyncStreamHandler;
import com.google.cloud.tools.managedcloudsdk.process.AsyncStreamSaver;
import com.google.cloud.tools.managedcloudsdk.process.CollectingByteHandler;
import com.google.cloud.tools.managedcloudsdk.process.ProcessExecutorFactory;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/** {@link CommandRunner}/{@link CommandCaller} factory for convenience. */
public class CommandFactory {

  /**
   * Returns a new {@link CommandRunner} instance.
   *
   * @param command command to run
   * @param workingDirectory the working directory to run in, can be {@code null}
   * @param environment map of environment variables, can be {@code null}
   * @param messageListener listener on command output
   * @return a {@link CommandRunner} configured to run the command
   */
  public CommandRunner newRunner(
      List<String> command,
      Path workingDirectory,
      Map<String, String> environment,
      MessageListener messageListener) {
    AsyncStreamHandler stdOut =
        new AsyncByteConsumer(new MessageListenerForwardingHandler(messageListener));
    AsyncStreamHandler stdErr =
        new AsyncByteConsumer(new MessageListenerForwardingHandler(messageListener));

    return new CommandRunner(
        command, workingDirectory, environment, new ProcessExecutorFactory(), stdOut, stdErr);
  }

  /**
   * Returns a new {@link CommandCaller} instance.
   *
   * @param command command to run
   * @param workingDirectory the working directory to run in, can be {@code null}
   * @param environment map of environment variables, can be {@code null}
   * @return a {@link CommandCaller} configured to run the command and return the result
   */
  public CommandCaller newCaller(
      List<String> command, Path workingDirectory, Map<String, String> environment) {
    AsyncStreamSaver stdOut = new AsyncByteConsumer(new CollectingByteHandler());
    AsyncStreamSaver stdErr = new AsyncByteConsumer(new CollectingByteHandler());

    return new CommandCaller(
        command, workingDirectory, environment, new ProcessExecutorFactory(), stdOut, stdErr);
  }
}
