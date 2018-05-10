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

package com.google.cloud.tools.appengine.cloudsdk.serialization;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;
import javax.annotation.Nullable;

/** Representation of a CloudSdkComponent. Used for JSON serialization/deserialization. */
public class CloudSdkComponent {

  @Nullable private String id;
  @Nullable private String name;

  @SerializedName("current_version_string")
  @Nullable
  private String currentVersion;

  @SerializedName("latest_version_string")
  @Nullable
  private String latestVersion;

  @SerializedName("size")
  @Nullable
  private Integer sizeInBytes;

  @Nullable private State state;

  @SerializedName("is_configuration")
  @Nullable
  private Boolean isConfiguration;

  @SerializedName("is_hidden")
  @Nullable
  private Boolean isHidden;

  private static final Gson gson = new Gson();

  public String toJson() {
    return gson.toJson(this);
  }

  public static CloudSdkComponent fromJson(String json) throws JsonSyntaxException {
    return gson.fromJson(json, CloudSdkComponent.class);
  }

  public static List<CloudSdkComponent> fromJsonList(String jsonList) throws JsonSyntaxException {
    Type type = new TypeToken<List<CloudSdkComponent>>() {}.getType();
    return gson.fromJson(jsonList, type);
  }

  @Nullable
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  @Nullable
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Nullable
  public String getCurrentVersion() {
    return currentVersion;
  }

  public void setCurrentVersion(String currentVersion) {
    this.currentVersion = currentVersion;
  }

  @Nullable
  public String getLatestVersion() {
    return latestVersion;
  }

  public void setLatestVersionString(String latestVersion) {
    this.latestVersion = latestVersion;
  }

  @Nullable
  public Integer getSizeInBytes() {
    return sizeInBytes;
  }

  public void setSizeInBytes(Integer sizeInBytes) {
    this.sizeInBytes = sizeInBytes;
  }

  @Nullable
  public Boolean getIsConfiguration() {
    return isConfiguration;
  }

  public void setIsConfiguration(Boolean isConfiguration) {
    this.isConfiguration = isConfiguration;
  }

  @Nullable
  public Boolean getIsHidden() {
    return isHidden;
  }

  public void setIsHidden(Boolean isHidden) {
    this.isHidden = isHidden;
  }

  @Nullable
  public State getState() {
    return state;
  }

  public void setState(State state) {
    this.state = state;
  }

  public static class State {
    @Nullable private String name;

    public boolean isInstalled() {
      return "Installed".equals(name) || "Update Available".equals(name);
    }

    @Nullable
    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }
  }
}
