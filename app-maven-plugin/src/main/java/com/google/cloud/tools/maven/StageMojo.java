/*
 * Copyright 2016 Google LLC. All Rights Reserved.
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

package com.google.cloud.tools.maven;

import com.google.cloud.tools.appengine.api.deploy.StageFlexibleConfiguration;
import com.google.cloud.tools.appengine.api.deploy.StageStandardConfiguration;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Generates a deploy-ready application directory for App Engine standard or flexible environment
 * deployment.
 */
@Mojo(name = "stage", defaultPhase = LifecyclePhase.PACKAGE)
public class StageMojo extends AbstractStageMojo
    implements StageStandardConfiguration, StageFlexibleConfiguration {

  @Override
  public void execute() throws MojoExecutionException {
    AppEngineStager stager = AppEngineStager.Factory.newStager(this);
    stager.overrideAppEngineDirectory();
    stager.stage();
  }
}
