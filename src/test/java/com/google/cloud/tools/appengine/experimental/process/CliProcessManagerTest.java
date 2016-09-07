/*
 * Copyright 2016 Google Inc.
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

package com.google.cloud.tools.appengine.experimental.process;

import static org.junit.Assume.assumeTrue;

import com.google.cloud.tools.appengine.experimental.OutputHandler;
import com.google.cloud.tools.appengine.experimental.internal.process.CliProcessManager;
import com.google.cloud.tools.appengine.experimental.process.io.CollectingOutputHandler;
import com.google.cloud.tools.appengine.experimental.process.io.DumbConverter;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class CliProcessManagerTest {

  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Rule
  public TemporaryFolder testRoot = new TemporaryFolder();

  @Test
  public void testManage_linuxEcho() throws IOException, ExecutionException, InterruptedException {
    assumeTrue(!System.getProperty("os.name").startsWith("Windows"));

    CollectingOutputHandler outputHandler = new CollectingOutputHandler();
    Future<String> future = createTestProcess("echo 'stdout'; echo 'stderr' 1>&2", outputHandler);

    String result = future.get();

    Assert.assertEquals("stdout\n", result);
    Assert.assertEquals(Collections.singletonList("stderr"), outputHandler.getLines());
  }

  @Test
  public void testManage_linuxEchoFail()
      throws IOException, InterruptedException, ExecutionException {
    assumeTrue(!System.getProperty("os.name").startsWith("Windows"));

    CollectingOutputHandler outputHandler = new CollectingOutputHandler();
    Future<String> future = createTestProcess("echo 'stdout'; echo 'stderr' 1>&2; exit 1",
        outputHandler);

    exception.expect(ExecutionException.class);
    exception.expectMessage("Process failed with exit code : 1");
    future.get();

    Assert.assertEquals(Collections.singletonList("stderr"), outputHandler.getLines());
  }

  @Test
  public void testManage_linuxEchoCancel()
      throws IOException, InterruptedException, ExecutionException {
    assumeTrue(!System.getProperty("os.name").startsWith("Windows"));

    CollectingOutputHandler outputHandler = new CollectingOutputHandler();
    Future<String> future = createTestProcess("sleep 10000", outputHandler);

    boolean interrupted = future.cancel(false);
    Assert.assertTrue(interrupted);
    Assert.assertTrue(future.isCancelled());

    exception.expect(CancellationException.class);
    future.get();
  }

  // test specific helper
  private Future<String> createTestProcess(String commandFileContents, OutputHandler outputHandler)
      throws IOException {
    File echo = testRoot.newFile();

    Files.write(echo.toPath(), commandFileContents.getBytes(Charset.forName("UTF-8")));

    ProcessBuilder pb = new ProcessBuilder()
        .command(Arrays.asList("sh", echo.getName()))
        .directory(testRoot.getRoot());

    return new CliProcessManager.Provider<String>().manage(pb.start(), new DumbConverter(), outputHandler);
  }
}
