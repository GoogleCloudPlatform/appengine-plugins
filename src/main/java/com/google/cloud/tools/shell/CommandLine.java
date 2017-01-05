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

import com.google.common.annotations.Beta;

import java.util.ArrayList;

/**
 * Utilities for working with command line arguments.
 */
@Beta
public class CommandLine {

  /**
   * Split a full command line into an array of individual arguments
   * according to POSIX rules, similar to the shlex function in Python. 
   * All input characters are preserved except for separating whitespace. 
   * This function tokenizes the input string, but does not attempt to parse it.
   * For example, it recognizes that a space escaped with a backslash does not
   * start a new token, but it does not replace the "\ " with a single space. 
   * 
   * @param line the input line
   * @return a non-null but possibly empty array of arguments
   * @throws NullPointerException if line is null
   */
  public static String[] split(String line) {    
    char quote = '"';
    boolean quoted = false;
    boolean escaped = false;
    
    ArrayList<String> result = new ArrayList<>();
    StringBuilder arg = null;
    for (char c : line.toCharArray()) {
      if (escaped) {
        arg.append(c);
        escaped = false;
      } else if (!Character.isWhitespace(c)) {
        if (!quoted && (c == '"' || c == '\'')) { // opening quote
          quoted = true;
          quote = c;
        } else if (quoted && c == quote) { // closing quote
          quoted = false;
        } else if (c == '\\') { // escape next character {
          escaped = true;
        }
        
        if (arg == null) { // start of token
          arg = new StringBuilder();
        }
        arg.append(c);
      } else if (quoted) { // quoted whitespace
        arg.append(c);
      } else { // end of token
        if (arg != null) {
          result.add(arg.toString());
          arg = null;
        }      
      }
    }
    
    if (arg != null) { // final token
      result.add(arg.toString());
      arg = null;
    }
    
    return result.toArray(new String[0]);
  }

}
