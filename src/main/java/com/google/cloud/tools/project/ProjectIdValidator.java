package com.google.cloud.tools.project;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Check if a string is a legal <a href='https://support.google.com/cloud/answer/6158840?hl=en'>Google 
 * Cloud Platform Project ID</a>. Source: com.google.apphosting.base.AppId
 * 
 * <pre>
 * app-id ::= [(domain):](display-app-id)
 * domain ::= r'(?!-)[a-z\d\-\.]{1,100}'
 * display-app-id ::= r'(?!-)[a-z\d\-]{1,100}'
 * </pre>
 * 
 * Note that in some older documentation this is referred to as an "application ID."
 */
public class ProjectIdValidator {

  private static final Pattern PATTERN = Pattern.compile("([a-z\\d\\-\\.]{1,100}:)?[a-z\\d\\-\\.]{1,100}");
  
  public static boolean validate(String id) {
    if (id == null) {
      return false;
    } else if (id.isEmpty()) {
      return true;
    }
    Matcher matcher = PATTERN.matcher(id);
    return matcher.matches();
  }

}
