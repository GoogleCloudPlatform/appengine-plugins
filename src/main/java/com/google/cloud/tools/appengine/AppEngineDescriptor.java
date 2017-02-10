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

import java.io.IOException;
import java.io.InputStream;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Utilities to obtain information from appengine-web.xml.
 */
public class AppEngineDescriptor {

  private static final String APP_ENGINE_NAMESPACE = "http://appengine.google.com/ns/1.0";
  private final Document document;
  
  // private to force use of parse method
  private AppEngineDescriptor(Document document) {
    this.document = document;
  }

  /**
   * Parses an appengine-web.xml file.
   * 
   * @param in the contents of appengine-web.xml
   * @return a fully parsed object that can be queried 
   * @throws IOException if parsing fails for any reason including malformed XML
   */
  public static AppEngineDescriptor parse(InputStream in) throws IOException {
    try {
      DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
      documentBuilderFactory.setNamespaceAware(true);
      return new AppEngineDescriptor(documentBuilderFactory.newDocumentBuilder().parse(in));
    } catch (SAXException | ParserConfigurationException exception) {
      throw new IOException("Cannot parse appengine-web.xml", exception);
    }
  }

  /**
   * @return project ID from the &lt;application&gt; element of the appengine-web.xml or null
   *         if it is missing
   */
  public String getProjectId()  {
    return getTopLevelValue(document, "appengine-web-app", "application");
  }

  /**
   * @return runtime from the &lt;runtime&gt; element of the appengine-web.xml or null
   *         if it is missing
   */
  public String getRuntime()  {
    return getTopLevelValue(document, "appengine-web-app", "runtime");
  }
  /**
   * @return project version from the &lt;version&gt; element of the appengine-web.xml or
   *         null if it is missing
   */
  public String getProjectVersion() {
    return getTopLevelValue(document, "appengine-web-app", "version");
  }

  /**
   * @return service ID from the &lt;service&gt; element of the appengine-web.xml, or
   *         null if it is missing. Will also look at module ID.
   */
  public String getServiceId() {
    String serviceId = getTopLevelValue(document, "appengine-web-app", "service");
    if (serviceId != null) {
      return serviceId;
    }
    return getTopLevelValue(document, "appengine-web-app", "module");
  }

  private static String getTopLevelValue(Document doc, String parentTagName, String childTagName) {
    try {
      NodeList parentElements = doc.getElementsByTagNameNS(APP_ENGINE_NAMESPACE, parentTagName);
      if (parentElements.getLength() > 0) {
        Node parent = parentElements.item(0);
        if (parent.hasChildNodes()) {
          for (int i = 0; i < parent.getChildNodes().getLength(); ++i) {
            Node child = parent.getChildNodes().item(i);
            if (child.getNodeName().equals(childTagName)) {
              return child.getTextContent();
            }
          }
        }
      }
      return null;
    } catch (DOMException ex) {
      // this shouldn't happen barring a very funky DOM implementation
      return null;
    }
  }
}