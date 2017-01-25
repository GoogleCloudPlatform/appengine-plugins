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
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.Test;

public class AppEngineDescriptorTest {

  private static final String TEST_VERSION = "fooVersion";
  private static final String TEST_ID = "fooId";

  private static final String ROOT_END_TAG = "</appengine-web-app>";
  private static final String ROOT_START_TAG =
      "<appengine-web-app xmlns='http://appengine.google.com/ns/1.0'>";
  private static final String ROOT_START_TAG_WITH_INVALID_NS =
      "<appengine-web-app xmlns='http://foo.bar.com/ns/42'>";
  private static final String PROJECT_ID = "<application>" + TEST_ID + "</application>";
  private static final String VERSION = "<version>" + TEST_VERSION + "</version>";
  private static final String COMMENT = "<!-- this is a test comment -->";
  private static final String COMMENT_AFTER_VERSION = "<version>" + TEST_VERSION + COMMENT + "</version>";
  private static final String COMMENT_BEFORE_VERSION = "<version>" + COMMENT + TEST_VERSION + "</version>";
  private static final String SERVICE = "<service>" + TEST_ID + "</service>";
  private static final String MODULE = "<module>" + TEST_ID + "</module>";
  
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
  public void testParse_noProjectId() throws IOException {
    AppEngineDescriptor descriptor = parse(XML_WITHOUT_PROJECT_ID);

    assertNull(descriptor.getProjectId());
  }

  @Test
  public void testParse_noVersion() throws IOException {
    AppEngineDescriptor descriptor = parse(XML_WITHOUT_VERSION);

    assertEquals(TEST_ID, descriptor.getProjectId());
    assertNull(descriptor.getProjectVersion());
  }

  @Test
  public void testParse_properXml() throws IOException {
    AppEngineDescriptor descriptor = parse(XML_WITH_VERSION_AND_PROJECT_ID);

    assertEquals(TEST_ID, descriptor.getProjectId());
    assertEquals(TEST_VERSION, descriptor.getProjectVersion());
  }

  @Test
  public void testParse_xmlWithCommentBeforeValue() throws IOException {
    AppEngineDescriptor descriptor = parse(XML_WITH_COMMENT_BEFORE_VERSION);

    assertEquals(TEST_ID, descriptor.getProjectId());
    assertEquals(TEST_VERSION, descriptor.getProjectVersion());
  }

  @Test
  public void testParse_xmlWithCommentAfterValue() throws IOException {
    AppEngineDescriptor descriptor = parse(XML_WITH_COMMENT_AFTER_VERSION);

    assertEquals(TEST_ID, descriptor.getProjectId());
    assertEquals(TEST_VERSION, descriptor.getProjectVersion());
  }
  
  @Test
  public void testParse_xmlWithInvalidNamespace() throws IOException {
    AppEngineDescriptor descriptor = parse(XML_WITH_VERSION_AND_PROJECT_ID_WRONG_NS);

    assertNull(descriptor.getProjectId());
    assertNull(descriptor.getProjectVersion());
  }

  public void testService_noContent() throws IOException {
    AppEngineDescriptor descriptor = parse(ROOT_START_TAG + ROOT_END_TAG);

    assertNull(descriptor.getServiceId());
  }

  public void testService_service() throws IOException {
    AppEngineDescriptor descriptor = parse(ROOT_START_TAG + SERVICE + ROOT_END_TAG);

    assertEquals(TEST_VERSION, descriptor.getServiceId());
  }

  public void testService_module() throws IOException {
    AppEngineDescriptor descriptor = parse(ROOT_START_TAG + MODULE + ROOT_END_TAG);

    assertEquals(TEST_VERSION, descriptor.getServiceId());
  }

  private static AppEngineDescriptor parse(String xmlString) throws IOException {
    return AppEngineDescriptor
        .parse(new ByteArrayInputStream(xmlString.getBytes(StandardCharsets.UTF_8)));
  }

}