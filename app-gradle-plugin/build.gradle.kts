import net.researchgate.release.GitAdapter.GitConfig
import java.util.Date
import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL

/*
 * Copyright 2022 Google LLC. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

group = "com.google.cloud.tools"
version = "2.7.1-SNAPSHOT" // {x-version-update:app-gradle-plugin:current}

plugins {
  id("java")
  id("maven")
  id("java-gradle-plugin")
  id("net.researchgate.release") version "2.6.0"
  id("com.github.sherter.google-java-format") version "0.9"
  id("checkstyle")
  id("jacoco")
  id("maven-publish")
  id("io.github.gradle-nexus.publish-plugin") version "1.3.0"
  id("signing")
}

repositories {
  mavenLocal()
  mavenCentral()
}

java {
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
  implementation(localGroovy())
  implementation(gradleApi())
  api("com.google.cloud.tools:appengine-plugins-core:0.12.1-SNAPSHOT") // {x-version-update:appengine-plugins-core:current}

  testImplementation("commons-io:commons-io:2.11.0")
  testImplementation("junit:junit:4.13.2")
  testImplementation("org.hamcrest:hamcrest-library:2.2")
  testImplementation("org.mockito:mockito-core:4.11.0")
}

tasks.wrapper {
  gradleVersion = "6.9"
}

tasks.jar.configure {
  manifest {
    attributes(
      mapOf(
        "Implementation-Title" to project.name,
        "Implementation-Version" to project.version,
        "Built-By" to System.getProperty("user.name"),
        "Built-Date" to Date(),
        "Built-JDK" to System.getProperty("java.version"),
        "Built-Gradle" to gradle.gradleVersion
      )
    )
  }
}

tasks.withType<JavaCompile>().configureEach { 
    options.compilerArgs = options.compilerArgs + listOf(
      "-Xlint:all"
    )
}

// Gradle 6 needs a special treatment for Guava 31+; otherwise you get "... However we
// cannot choose between the following variants..." error.
// https://github.com/google/guava/releases/tag/v32.1.0
sourceSets.all {
  configurations.getByName(runtimeClasspathConfigurationName) {
    attributes.attribute(Attribute.of("org.gradle.jvm.environment", String::class.java), "standard-jvm")
  }
  configurations.getByName(compileClasspathConfigurationName) {
    attributes.attribute(Attribute.of("org.gradle.jvm.environment", String::class.java), "standard-jvm")
  }
}

/* TESTING */
tasks.test.configure {
  testLogging {
    showStandardStreams = true
    exceptionFormat = FULL
  }
}

sourceSets {
  create("integTest") {
    compileClasspath += main.get().output
    runtimeClasspath += main.get().output
  }
}

configurations {
  named("integTestCompile").get().extendsFrom(testCompileClasspath.get())
  named("integTestRuntime").get().extendsFrom(testRuntimeClasspath.get())
}

tasks.register<Test>("integTest") {
  testClassesDirs = sourceSets.getByName("integTest").output.classesDirs
  classpath = sourceSets.getByName("integTest").runtimeClasspath
  outputs.upToDateWhen { false }
}
/* TESTING */

/* RELEASING */
tasks.register<Jar>("sourceJar") {
  from(sourceSets.main.get().allJava)
  archiveClassifier.set("sources")
}

tasks.register<Jar>("javadocJar") {
  dependsOn(tasks.javadoc)
  from(tasks.javadoc.map { it.destinationDir!! })
  archiveClassifier.set("javadoc")
}

// for kokoro releases
release {
  tagTemplate = "v\$version"
  getProperty("git").apply {
    this as GitConfig
    requireBranch = """^release-v\d+.*$"""  //regex
  }
}

// disable gradlePlugin auto publishing to avoid duplicate uploads,
// see https://github.com/gradle/gradle/issues/10384 for more info.
gradlePlugin { isAutomatedPublishing = false }

tasks.withType<GenerateModuleMetadata> {
  enabled = false
}

publishing {
  publications {
    create<MavenPublication>("mavenJava") {
      artifactId = "appengine-gradle-plugin"
      from(components["java"])
      artifact(tasks.named("sourceJar"))
      artifact(tasks.named("javadocJar"))

      pom {
        name.set("App Engine Gradle Plugin")
        description.set("This Gradle plugin provides tasks to build and deploy Google App Engine applications.")
        url.set("https://github.com/GoogleCloudPlatform/appengine-plugins")
        inceptionYear.set("2016")
        licenses {
          license {
            name.set("The Apache Software License, Version 2.0")
            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            distribution.set("repo")
          }
        }
        developers {
          developer {
            id.set("loosebazooka")
            name.set("Appu Goundan")
            email.set("appu@google.com")
          }
        }
        scm {
          connection.set("https://github.com/GoogleCloudPlatform/appengine-plugins")
          developerConnection.set("scm:git://github.com/GoogleCloudPlatform/appengine-plugins.git")
          url.set("https://github.com/GoogleCloudPlatform/appengine-plugins")
        }
      }
    }
  }
}

nexusPublishing {
  repositories {
    sonatype {
      nexusUrl.set(uri("https://google.oss.sonatype.org/service/local/"))
      snapshotRepositoryUrl.set(uri("https://google.oss.sonatype.org/content/repositories/snapshots"))
      if (project.hasProperty("ossrhUsername")) {
        username.set(project.property("ossrhUsername").toString())
        password.set(project.property("ossrhPassword").toString())
      }
    }
  }
}

signing {
  setRequired({ gradle.taskGraph.hasTask(":${name}:publishToSonatype") })
  if (project.hasProperty("signing.gnupg.executable")) {
    useGpgCmd()
  }
  sign(publishing.publications["mavenJava"])
}
/* RELEASING */

/* FORMATTING */
googleJavaFormat {
  toolVersion = "1.7"
}


tasks.check.configure {
  dependsOn(tasks.verifyGoogleJavaFormat)
}
tasks.withType<Checkstyle>().configureEach {
  // Set up a soft dependency so that verifyGoogleFormat suggests running googleJavaFormat,
  // before devs start fixing individual checkstyle violations manually.
  shouldRunAfter(tasks.verifyGoogleJavaFormat)
}
// to auto-format run ./gradlew googleJavaFormat

checkstyle {
  toolVersion = "8.37"
  // Get the google_checks.xml file from the actual tool we're invoking.
  config = resources.text.fromArchiveEntry(configurations.checkstyle.get().files.first(), "google_checks.xml")
  maxErrors = 0
  maxWarnings = 0
  tasks.checkstyleTest.configure {
    enabled = false
  }
}
/* FORMATTING */

/* TEST COVERAGE */
jacoco {
  toolVersion = "0.8.8"
}

tasks.jacocoTestReport {
  reports {
    xml.isEnabled = true
    html.isEnabled = false
  }
}
/* TEST COVERAGE */
