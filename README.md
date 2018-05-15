[![experimental](http://badges.github.io/stability-badges/dist/experimental.svg)](http://github.com/badges/stability-badges)
[![Build Status](https://travis-ci.org/GoogleCloudPlatform/appengine-plugins-core.svg?branch=master)](https://travis-ci.org/GoogleCloudPlatform/appengine-plugins-core)

# Google App Engine Plugins Core Library

IMPORTANT:
This library is used by Google internal plugin development teams to share App Engine
related code.  Its use for any other purpose is highly discouraged and unsupported. Visit our
[App Engine documentation](https://cloud.google.com/appengine/docs/admin-api/) for more information 
on Google supported clients for App Engine administration.
 
# Requirements

This library requires Java 1.8 or higher to run.
This library requires Maven and Java 1.8 or higher to build.

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
// All operations require the cloud SDK
CloudSdk cloudSdk = new CloudSdk.Builder().sdkPath(some-path).javaHome(some-java-home).build();


// Operations that use gcloud command line are initiated by the Gcloud object.
Gcloud gcloud = Gcloud.builder(sdk).setMetricsEnvironment("asdf","12").setCredentialFile(some-file).build()

// Similarly for AppCfg and the DevAppServer
AppCfg appcfg = AppCfg.builder(sdk).build()
LocalRun localRun = LocalRun.builder(sdk).build()

// Operations are started as processes, access to these processes is handled
// by implementations of ProcessHandler. To continue to interface with processes
// as before, use LegacyProcessHandler.
ProcessHandler handler = LegacyProcessHandler.builder()...configureHandler...build();
gcloud.newDeployment(handler).deploy(myDeployConfiguration);
```

## SDK Manager

This library provides a mechanism for installing, adding components and updating the Cloud SDK. The operations are intended to run asynchronously, either on an executor or through mechanisms provided by an IDE.

```java
// Create a new Managed SDK instance
ManagedCloudSdk sdk = ManagedCloudSdk.newManagedSdk("123.123.123") // SDK fixed at version.
ManagedCloudSdk sdk = ManagedCloudSdk.newManagedSdk() // 'LATEST' sdk, can be updated.

// Implement the listener interface to listen to operation output
MessageListener listener = new MessageListener() {...};

// Always check if operations are needed before running them
if (!sdk.isInstalled()) {
  sdk.newInstaller().install(listener);
}

// use SdkComponent to reference a Cloud Sdk component
if (!sdk.hasComponent(SdkComponent.APP_ENGINE_JAVA)) {
  sdk.newComponentInstaller().installComponent(SdkComponent.APP_ENGINE_JAVA, listener);
}

// updates will only occur on 'LATEST' sdks
if (!sdk.isUpToDate) {
  sdk.newUpdater().update(listener);
}

// You can then create an SDK from a managed SDK instance
new CloudSdk.Builder().sdkPath(sdk.getSdkHome())...;
```
