/*
 * Copyright 2017 Google Inc.
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

package com.google.cloud.tools.test.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * A simple log storing handler. Apply this to the logger you want to track - for example the logger
 * in some class ABC
 *
 * <p>Logger log = Logger.getLogger(ABC.class.getName()); handler = new LogStoringHandler();
 * log.addHandler(handler);
 *
 * <p>The handler now stores all logs created by that logger.
 */
public class LogStoringHandler extends Handler {

  private List<LogRecord> logs = new ArrayList<>();

  /**
   * Convenience method for test handler creation and registration. It will configure the logger and
   * handler to Level.ALL to accept all logs.
   */
  public static LogStoringHandler getForLogger(String loggerName) {
    LogStoringHandler handler = new LogStoringHandler();
    Logger log = Logger.getLogger(loggerName);
    log.addHandler(handler);
    log.setLevel(Level.ALL);
    handler.setLevel(Level.ALL);
    return handler;
  }

  @Override
  public void publish(LogRecord record) {
    logs.add(record);
  }

  @Override
  public void flush() {
    // purposely does nothing
  }

  @Override
  public void close() throws SecurityException {
    // purposely does nothing
  }

  public List<LogRecord> getLogs() {
    return logs;
  }
}
