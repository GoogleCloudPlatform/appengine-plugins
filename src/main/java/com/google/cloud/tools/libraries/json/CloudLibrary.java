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

package com.google.cloud.tools.libraries.json;

import java.util.List;
import javax.annotation.Nullable;

/** Holds metadata about a single Cloud Library. */
public final class CloudLibrary {

  @Nullable private String name;
  @Nullable private String id;
  @Nullable private String serviceName;
  @Nullable private List<String> serviceRoles;
  @Nullable private String documentation;
  @Nullable private String description;
  @Nullable private String icon;
  @Nullable private List<String> transports;
  @Nullable private List<CloudLibraryClient> clients;

  /** Prevents direct instantiation. GSON instantiates these objects using dark magic. */
  private CloudLibrary() {}

  /** Returns the name of this library. */
  @Nullable
  public String getName() {
    return name;
  }

  /** Returns the ID of this library. */
  @Nullable
  public String getId() {
    return id;
  }

  /** Returns the service name associated with this library. */
  @Nullable
  public String getServiceName() {
    return serviceName;
  }

  /** Returns the service roles associated with this library. */
  @Nullable
  public List<String> getServiceRoles() {
    return serviceRoles;
  }

  /** Returns a URL to the documentation for this library. */
  @Nullable
  public String getDocumentation() {
    return documentation;
  }

  /** Returns the description for this library. */
  @Nullable
  public String getDescription() {
    return description;
  }

  /** Returns a URL to the icon for this library. */
  @Nullable
  public String getIcon() {
    return icon;
  }

  /** Returns the list of supported transports for this library (e.g. http, grpc, etc.). */
  @Nullable
  public List<String> getTransports() {
    return transports;
  }

  /** Returns the list of available clients for this library. */
  @Nullable
  public List<CloudLibraryClient> getClients() {
    return clients;
  }
}
