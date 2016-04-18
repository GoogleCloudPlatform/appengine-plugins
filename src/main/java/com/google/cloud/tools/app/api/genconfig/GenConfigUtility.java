package com.google.cloud.tools.app.api.genconfig;

import com.google.cloud.tools.app.api.AppEngineException;

/**
 * Created by meltsufin on 4/18/16.
 */
public interface GenConfigUtility {
  void genConfig(GenConfigParams config) throws AppEngineException;
}
