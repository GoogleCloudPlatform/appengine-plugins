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

package com.google.cloud.tools.appengine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.cloud.tools.appengine.api.AppEngineException;
import com.google.common.collect.ImmutableMap;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.Test;
import org.xml.sax.SAXException;

public class AppEngineDescriptorTest {

  private static final String TEST_VERSION = "fooVersion";
  private static final String TEST_ID = "fooId";
  private static final String RUNTIME_ID = "java8";

  private static final String ROOT_END_TAG = "</appengine-web-app>";
  private static final String ROOT_START_TAG =
      "<appengine-web-app xmlns='http://appengine.google.com/ns/1.0'>";
  private static final String ROOT_START_TAG_WITH_INVALID_NS =
      "<appengine-web-app xmlns='http://foo.bar.com/ns/42'>";
  private static final String PROJECT_ID = "<application>" + TEST_ID + "</application>";
  private static final String VERSION = "<version>" + TEST_VERSION + "</version>";
  private static final String COMMENT = "<!-- this is a test comment -->";
  private static final String COMMENT_AFTER_VERSION =
      "<version>" + TEST_VERSION + COMMENT + "</version>";
  private static final String COMMENT_BEFORE_VERSION =
      "<version>" + COMMENT + TEST_VERSION + "</version>";
  private static final String SERVICE = "<service>" + TEST_ID + "</service>";
  private static final String MODULE = "<module>" + TEST_ID + "</module>";
  private static final String RUNTIME = "<runtime>" + RUNTIME_ID + "</runtime>";
  private static final String ENVIRONMENT =
      "<env-variables><env-var name='keya' value='vala' /><env-var name='key2' value='val2' /><env-var name='keyc' value='valc' /></env-variables>";

  private static final String XML_WITHOUT_PROJECT_ID = ROOT_START_TAG + ROOT_END_TAG;
  private static final String XML_WITHOUT_VERSION = ROOT_START_TAG + PROJECT_ID + ROOT_END_TAG;
  private static final String XML_WITH_VERSION_AND_PROJECT_ID =
      ROOT_START_TAG + PROJECT_ID + VERSION + ROOT_END_TAG;
  private static final String XML_WITH_COMMENT_BEFORE_VERSION =
      ROOT_START_TAG + PROJECT_ID + COMMENT_BEFORE_VERSION + ROOT_END_TAG;
  private static final String XML_WITH_COMMENT_AFTER_VERSION =
      ROOT_START_TAG + PROJECT_ID + COMMENT_AFTER_VERSION + ROOT_END_TAG;
  private static final String XML_WITH_VERSION_AND_PROJECT_ID_WRONG_NS =
      ROOT_START_TAG_WITH_INVALID_NS + PROJECT_ID + VERSION + ROOT_END_TAG;

  @Test
  public void testParse_noProjectId() throws IOException, SAXException, AppEngineException {
    AppEngineDescriptor descriptor = parse(XML_WITHOUT_PROJECT_ID);

    assertNull(descriptor.getProjectId());
  }

  @Test
  public void testParse_noVersion() throws AppEngineException, IOException, SAXException {
    AppEngineDescriptor descriptor = parse(XML_WITHOUT_VERSION);

    assertEquals(TEST_ID, descriptor.getProjectId());
    assertNull(descriptor.getProjectVersion());
  }

  @Test
  public void testParse_properXml() throws AppEngineException, IOException, SAXException {
    AppEngineDescriptor descriptor = parse(XML_WITH_VERSION_AND_PROJECT_ID);

    assertEquals(TEST_ID, descriptor.getProjectId());
    assertEquals(TEST_VERSION, descriptor.getProjectVersion());
  }

  @Test
  public void testParse_xmlWithCommentBeforeValue()
      throws AppEngineException, IOException, SAXException {
    AppEngineDescriptor descriptor = parse(XML_WITH_COMMENT_BEFORE_VERSION);

    assertEquals(TEST_ID, descriptor.getProjectId());
    assertEquals(TEST_VERSION, descriptor.getProjectVersion());
  }

  @Test
  public void testParse_xmlWithCommentAfterValue()
      throws AppEngineException, IOException, SAXException {
    AppEngineDescriptor descriptor = parse(XML_WITH_COMMENT_AFTER_VERSION);

    assertEquals(TEST_ID, descriptor.getProjectId());
    assertEquals(TEST_VERSION, descriptor.getProjectVersion());
  }

  @Test
  public void testParse_xmlWithInvalidNamespace()
      throws AppEngineException, IOException, SAXException {
    AppEngineDescriptor descriptor = parse(XML_WITH_VERSION_AND_PROJECT_ID_WRONG_NS);

    assertNull(descriptor.getProjectId());
    assertNull(descriptor.getProjectVersion());
  }

  @Test
  public void testService_noContent() throws AppEngineException, IOException, SAXException {
    AppEngineDescriptor descriptor = parse(ROOT_START_TAG + ROOT_END_TAG);

    assertNull(descriptor.getServiceId());
  }

  @Test
  public void testService_service() throws AppEngineException, IOException, SAXException {
    AppEngineDescriptor descriptor = parse(ROOT_START_TAG + SERVICE + ROOT_END_TAG);

    assertEquals(TEST_ID, descriptor.getServiceId());
  }

  @Test
  public void testService_module() throws AppEngineException, IOException, SAXException {
    AppEngineDescriptor descriptor = parse(ROOT_START_TAG + MODULE + ROOT_END_TAG);

    assertEquals(TEST_ID, descriptor.getServiceId());
  }

  @Test
  public void testJava8Runtime() throws AppEngineException, IOException, SAXException {
    AppEngineDescriptor descriptor = parse(ROOT_START_TAG + RUNTIME + ROOT_END_TAG);

    assertEquals(RUNTIME_ID, descriptor.getRuntime());
    assertTrue(descriptor.isJava8());
  }

  @Test
  public void testUnknownRuntime() throws AppEngineException, IOException, SAXException {
    AppEngineDescriptor descriptor =
        parse(
            "<appengine-web-app xmlns='http://appengine.google.com/ns/1.0'>"
                + "<runtime>java9</runtime>"
                + "</appengine-web-app>");

    assertEquals("java9", descriptor.getRuntime());
    assertFalse(descriptor.isJava8());
  }

  @Test
  public void testJava81() throws AppEngineException, IOException, SAXException {
    AppEngineDescriptor descriptor =
        parse(
            "<appengine-web-app xmlns='http://appengine.google.com/ns/1.0'>"
                + "<runtime>java8h</runtime>"
                + "</appengine-web-app>");

    assertEquals("java8h", descriptor.getRuntime());
    assertFalse(descriptor.isJava8());
  }

  @Test
  public void testInternalRuntime() throws AppEngineException, IOException, SAXException {
    AppEngineDescriptor descriptor =
        parse(
            "<appengine-web-app xmlns='http://appengine.google.com/ns/1.0'>"
                + "<runtime>java8g</runtime>"
                + "</appengine-web-app>");

    assertEquals("java8g", descriptor.getRuntime());
    assertTrue(descriptor.isJava8());
  }

  @Test
  public void testJava6Runtime() throws AppEngineException, IOException, SAXException {
    AppEngineDescriptor descriptor =
        parse(
            "<appengine-web-app xmlns='http://appengine.google.com/ns/1.0'>"
                + "<runtime>java</runtime>"
                + "</appengine-web-app>");

    assertEquals("java", descriptor.getRuntime());
    assertFalse(descriptor.isJava8());
  }

  @Test
  public void testParseAttributeMapValues() throws AppEngineException, IOException, SAXException {
    Map<String, String> environment =
        parse(ROOT_START_TAG + ENVIRONMENT + ROOT_END_TAG).getEnvironment();
    Map<String, String> expectedEnvironment =
        ImmutableMap.of("keya", "vala", "key2", "val2", "keyc", "valc");

    assertEquals(expectedEnvironment, environment);
  }

  private static AppEngineDescriptor parse(String xmlString) throws IOException, SAXException {
    return AppEngineDescriptor.parse(
        new ByteArrayInputStream(xmlString.getBytes(StandardCharsets.UTF_8)));
  }
}
