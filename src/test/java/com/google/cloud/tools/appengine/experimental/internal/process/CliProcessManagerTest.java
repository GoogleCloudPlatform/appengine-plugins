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

package com.google.cloud.tools.appengine.experimental.internal.process;

import com.google.cloud.tools.appengine.experimental.internal.process.io.DumbConverter;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.junit.Assume.assumeTrue;

public class CliProcessManagerTest {

  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Rule
  public TemporaryFolder testRoot = new TemporaryFolder();

  @Test
  public void testManage_linuxEcho() throws IOException, ExecutionException, InterruptedException {
    assumeTrue(!System.getProperty("os.name").startsWith("Windows"));

    CliProcessManager<String> future = createTestProcess("echo 'stdout'; echo 'stderr' 1>&2;");

    String output = CharStreams.toString(new InputStreamReader(future.getInputStream(), Charsets.UTF_8));
    String result = future.get();
    String[] lines = output.split("\n");

    Assert.assertEquals("stdout\n", result);
    Assert.assertEquals(1, lines.length);
    Assert.assertEquals("stderr", lines[0]);
  }

  @Test
  public void testManage_linuxEchoFail()
      throws IOException, InterruptedException, ExecutionException {
    assumeTrue(!System.getProperty("os.name").startsWith("Windows"));

    CliProcessManager<String> future = createTestProcess("echo 'stdout'; echo 'stderr' 1>&2; exit 1");

    exception.expect(ExecutionException.class);
    exception.expectMessage("Process failed with exit code : 1");
    future.get();
    String output = CharStreams.toString(new InputStreamReader(future.getInputStream(), Charsets.UTF_8));
    String[] lines = output.split("\n");

    Assert.assertEquals(1, lines.length);
    Assert.assertEquals("stderr", lines[0]);
  }

  @Test
  public void testManage_linuxEchoCancel()
      throws IOException, InterruptedException, ExecutionException {
    assumeTrue(!System.getProperty("os.name").startsWith("Windows"));

    Future<String> future = createTestProcess("sleep 10000");

    boolean interrupted = future.cancel(false);
    Assert.assertTrue(interrupted);
    Assert.assertTrue(future.isCancelled());

    exception.expect(CancellationException.class);
    future.get();
  }

  // test specific helper
  private CliProcessManager<String> createTestProcess(String commandFileContents)
      throws IOException {
    File echo = testRoot.newFile();

    Files.write(echo.toPath(), commandFileContents.getBytes(Charset.forName("UTF-8")));

    ProcessBuilder pb = new ProcessBuilder()
        .command(Arrays.asList("sh", echo.getName()))
        .directory(testRoot.getRoot());

    return new CliProcessManager.Provider<String>().manage(pb.start(), new DumbConverter());
  }
}
