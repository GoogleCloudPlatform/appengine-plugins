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

package com.google.cloud.tools.project;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;

/**
 * Check if a string is a legal <a
 * href='https://support.google.com/cloud/answer/6158840?hl=en'>Google Cloud Platform Project
 * ID</a>. Source: com.google.apphosting.base.AppId
 *
 * <p>
 *
 * <pre>
 * project-id ::= [(partition)~][(domain):](display-project-id)
 * partition ::= [a-z\d\-\.]{1,100}
 * domain ::= r'(?!-)[a-z\d\-\.]{1,100}'
 * display-project-id ::= r'(?!-)[a-z\d\-]{1,100}'
 * </pre>
 *
 * Note that in some older documentation this is referred to as an "application ID."
 */
public class ProjectIdValidator {

  private static final int MAX_LENGTH = 100;
  private static final Pattern DISPLAY_PROJECT_ID_REGEX =
      Pattern.compile("[a-z\\d\\-]{1," + MAX_LENGTH + "}", Pattern.CASE_INSENSITIVE);
  private static final Pattern DOMAIN_REGEX =
      Pattern.compile("([a-z\\d\\-\\.]{1," + MAX_LENGTH + "})?\\:", Pattern.CASE_INSENSITIVE);
  private static final Pattern PARTITION_REGEX =
      Pattern.compile("([a-z\\d\\-]{1," + MAX_LENGTH + "})?\\~", Pattern.CASE_INSENSITIVE);
  private static final Pattern PROJECT_ID_REGEX =
      Pattern.compile(
          "(?:"
              + PARTITION_REGEX
              + ")?((?:"
              + DOMAIN_REGEX
              + ")?("
              + DISPLAY_PROJECT_ID_REGEX
              + "))",
          Pattern.CASE_INSENSITIVE);

  /**
   * Check whether a string is a syntactically correct project ID. This method only checks syntax.
   * It does not check that the ID actually identifies a project in the Google Cloud Platform.
   *
   * @param id the alleged project ID
   * @return true if it's correct, false otherwise
   */
  public static boolean validate(@Nullable String id) {
    if (id == null) {
      return false;
    }
    Matcher matcher = PROJECT_ID_REGEX.matcher(id);
    return matcher.matches();
  }
}
