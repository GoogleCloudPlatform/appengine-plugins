[![experimental](http://badges.github.io/stability-badges/dist/experimental.svg)](http://github.com/badges/stability-badges)
# Google App Engine Plugins Core Library

This is the client Java library to manage App Engine Java applications. It should be used by any application that performs App Engine Java application management. For example, the Maven, Gradle and Eclipse App Engine plugins, custom user tools, etc.

# Requirements

This library requires Java 1.7 or higher to run, and Maven to build.

You must also install the Cloud SDK command line interface (CLI), if it isn't installed yet, following the [instructions](https://cloud.google.com/sdk/).

You must also install the app-engine-java component:

    gcloud components install app-engine-java

# Supported operations

The library implements the following operations:

* Deploy an application to standard or flexible environment
* Stage an application for deployment
* Run an application on the local server synchronously or asynchronously

# How to use

Build the library using the "mvn clean install" command at the repository root directory, where the pom.xml file is located. This produces the appengine-plugins-core-*version*-SNAPSHOT.jar file in the "target" directory that you can add to your application's class path.

To deploy a new version, a client calls the library in the following way:

```java
// Create a Cloud SDK
CloudSdk cloudSdk = new CloudSdk.Builder().build();

// Create a deployment
AppEngineDeployment deployment = new CloudSdkAppEngineDeployment(cloudSdk);

// Configure deployment
DefaultDeployConfiguration configuration = new DefaultDeployConfiguration();
configuration.setDeployables(Arrays.asList(appYaml1));
configuration.setBucket("gs://a-bucket");
configuration.setDockerBuild("cloud");
configuration.setForce(true);
configuration.setImageUrl("imageUrl");
configuration.setProject("project");
configuration.setPromote(true);
configuration.setServer("appengine.google.com");
configuration.setStopPreviousVersion(true);
configuration.setVersion("v1");

// deploy
deployment.deploy(deployConfiguration);
```
