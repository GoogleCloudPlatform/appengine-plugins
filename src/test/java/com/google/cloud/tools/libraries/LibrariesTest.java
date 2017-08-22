package com.google.cloud.tools.libraries;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonReaderFactory;
import javax.json.JsonValue;

public class LibrariesTest {

  @Test
  public void testWellFormed() throws ParserConfigurationException, SAXException, IOException {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    DocumentBuilder parser = factory.newDocumentBuilder();
    File in = new File("src/main/java/com/google/cloud/tools/libraries/libraries.xml");
    parser.parse(in);
  }
  
  @Test
  public void testJson() throws FileNotFoundException {
    JsonReaderFactory factory = Json.createReaderFactory(null);
    InputStream in =
        new FileInputStream("src/main/java/com/google/cloud/tools/libraries/libraries.json");
    JsonReader reader = factory.createReader(in); 
    Iterator<JsonValue> apis = reader.readArray().iterator();
    Assert.assertTrue(apis.hasNext());
    for (JsonObject api = (JsonObject) apis.next(); apis.hasNext(); api = (JsonObject) apis.next()) {
       JsonArray clients = api.getJsonArray("clients");
       Assert.assertFalse(clients.isEmpty());
       for (int i = 0; i < clients.size(); i++) {
         JsonObject client = (JsonObject) clients.get(i);
         String status = client.getString("status");
         Assert.assertTrue(status, 
             "beta".equals(status) || "alpha".equals(status) || "GA".equals(status));
       }
    }
  }

}
