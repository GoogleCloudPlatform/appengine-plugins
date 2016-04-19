/**
 * Copyright 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.cloud.tools.app.api.module;

import com.google.cloud.tools.app.api.AppEngineException;

/**
 * This set of commands can be used to manage existing App Engine modules. To create new deployments
 * of modules, use {@link com.google.cloud.tools.app.api.deploy.AppEngineDeployment}
 */
public interface AppEngineModuleService {

  /**
   * Start serving a specific version of the given modules.
   */
  void start(ModuleSelectionConfiguration configuration) throws AppEngineException;

  /**
   * Stop serving a specific version of the given modules.
   */
  void stop(ModuleSelectionConfiguration configuration) throws AppEngineException;

  /**
   * Delete a specific version of the given modules.
   */
  void delete(ModuleSelectionConfiguration configuration) throws AppEngineException;

  /**
   * Set the default serving version for the given modules
   */
  void setDefault(ModuleSelectionConfiguration configuration) throws AppEngineException;

  /**
   * Gets the logs for the given module.
   */
  void getLogs(GetLogsConfiguration configuration) throws AppEngineException;

  /**
   * List your existing deployed modules and versions.
   */
  void list(ListConfiguration configuration) throws AppEngineException;

  /**
   * Sets the policy for the Managed VMs of the given modules and version.
   */
  void setManagedBy(SetManagedByConfiguration configuration) throws AppEngineException;

}
