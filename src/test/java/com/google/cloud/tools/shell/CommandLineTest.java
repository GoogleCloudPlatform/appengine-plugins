/* Copyright 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.cloud.tools.shell;

import org.junit.Assert;
import org.junit.Test;

public class CommandLineTest {

  @Test
  public void testSplit_oneArg() {
    String[] args = CommandLine.split("  --foo ");
    String[] expected = {"--foo"};
    Assert.assertArrayEquals(expected, args);
  }

  @Test
  public void testSplit_threeArgs() {
    String[] args = CommandLine.split("  --foo -bar baz");
    String[] expected = {"--foo", "-bar", "baz"};
    Assert.assertArrayEquals(expected, args);
  }
  
  @Test
  public void testSplit_emptyString() {
    String[] args = CommandLine.split("  ");
    Assert.assertEquals(0, args.length);
  }
  
  @Test
  public void testSplit_null() {
    try {
      CommandLine.split(null);
      Assert.fail("split null");
    } catch (NullPointerException expected) {
    }
  }
  
  @Test
  public void testSplit_oneQuotedArg() {
    String[] args = CommandLine.split("--foo=\"bar baz\"");
    String[] expected = {"--foo=\"bar baz\""};
    Assert.assertArrayEquals(expected, args);
  }
  
  @Test
  public void testSplit_surpriseQuotes() {
    String[] args = CommandLine.split("java \"-vers\"\"ion\"");
    String[] expected = {"java", "\"-vers\"\"ion\""};
    Assert.assertArrayEquals(expected, args);
  }
  
  @Test
  public void testSplit_singleQuoted() {
    String[] args = CommandLine.split("java '-version'");
    String[] expected = {"java", "'-version'"};
    Assert.assertArrayEquals(expected, args);
  }
  
  @Test
  public void testSplit_singleQuotedSpaces() {
    String[] args = CommandLine.split("echo 'foo  bar'");
    String[] expected = {"echo", "'foo  bar'"};
    Assert.assertArrayEquals(expected, args);
  }
  
  @Test
  public void testSplit_quotedArgs() {
    String[] args = CommandLine.split("  --foo=\"bar baz\" -name=value baz");
    String[] expected = {"--foo=\"bar baz\"", "-name=value", "baz"};
    Assert.assertArrayEquals(expected, args);
  }

}
