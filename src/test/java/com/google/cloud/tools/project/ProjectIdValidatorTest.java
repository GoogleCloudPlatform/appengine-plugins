package com.google.cloud.tools.project;

import org.junit.Assert;
import org.junit.Test;

public class ProjectIdValidatorTest {
  
  @Test
  public void testDomain() {
    Assert.assertTrue(ProjectIdValidator.validate("google.com:mystore"));
  }

  @Test
  public void testOneWord() {
    Assert.assertTrue(ProjectIdValidator.validate("word"));
  }

  @Test
  public void testUpperCase() {
    Assert.assertFalse(ProjectIdValidator.validate("WORD"));
  }
  
  @Test
  public void testLongWord() {
    boolean validate = ProjectIdValidator.validate(
        "012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890");
    Assert.assertFalse(validate);
  }
  
  @Test
  public void testContainsSpace() {
    Assert.assertFalse(ProjectIdValidator.validate("com google eclipse"));
  }

  @Test
  public void testEmptyString() {
    Assert.assertTrue(ProjectIdValidator.validate(""));
  }
  
  @Test
  public void testNull() {
    Assert.assertFalse(ProjectIdValidator.validate(null));
  }

}
