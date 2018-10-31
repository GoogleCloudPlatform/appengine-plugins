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

package com.google.cloud.tools.appengine.api.debug;

import java.nio.file.Path;
import javax.annotation.Nullable;

/** Configuration for {@link GenRepoInfoFile#generate(GenRepoInfoFileConfiguration)}. */
public class GenRepoInfoFileConfiguration {
  @Nullable private final Path outputDirectory;
  @Nullable private final Path sourceDirectory;

  private GenRepoInfoFileConfiguration(
      @Nullable Path outputDirectory, @Nullable Path sourceDirectory) {
    this.outputDirectory = outputDirectory;
    this.sourceDirectory = sourceDirectory;
  }

  @Nullable
  public Path getOutputDirectory() {
    return outputDirectory;
  }

  @Nullable
  public Path getSourceDirectory() {
    return sourceDirectory;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    @Nullable private Path outputDirectory;
    @Nullable private Path sourceDirectory;

    private Builder() {}

    public Builder outputDirectory(@Nullable Path outputDirectory) {
      this.outputDirectory = outputDirectory;
      return this;
    }

    public Builder sourceDirectory(@Nullable Path sourceDirectory) {
      this.sourceDirectory = sourceDirectory;
      return this;
    }

    public GenRepoInfoFileConfiguration build() {
      return new GenRepoInfoFileConfiguration(outputDirectory, sourceDirectory);
    }
  }
}
