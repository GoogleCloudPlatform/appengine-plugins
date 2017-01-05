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

import java.util.ArrayList;
/**
 * Utilities for working with command line arguments.
 */
public class CommandLine {

  /**
   * Split a full command line into an array of individual arguments,
   * similar to the shlex function in Python.
   * 
   * @param line the input line
   * @return an array of arguments
   */
  public static String[] split(String line) {
    String[] empty = new String[0];
    
    char quote = '"';
    boolean quoted = false;
    
    ArrayList<String> result = new ArrayList<>();
    StringBuffer arg = null;
    for (char c : line.toCharArray()) {
      if (!Character.isWhitespace(c)) {
        if (arg == null) {
          arg = new StringBuffer();
        }
        if (c == quote) {
          quoted = !quoted;
        }
        arg.append(c);
      } else if (quoted) { // quoted whitespace
        arg.append(c);
      } else {
        if (arg != null) {
          result.add(arg.toString());
          arg = null;
        }      
      }
    }
    
    if (arg != null) {
      result.add(arg.toString());
      arg = null;
    }
    
    return result.toArray(empty);
  }

}
