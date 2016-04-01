[![experimental](http://badges.github.io/stability-badges/dist/experimental.svg)](http://github.com/badges/stability-badges)
# Google App Engine Java app management client

This is the client Java library to manage App Engine Java applications. It should be used by any application that performs App Engine Java application management. For example, the Maven, Gradle and Eclipse App Engine plugins, custom user tools, etc.

# Requirements

This library requires Java 1.7 or higher to run, and Maven to build.

You must also install the Cloud SDK command line interface (CLI), if it isn't installed yet, following the [instructions](https://cloud.google.com/sdk/).

After installing Cloud SDK, you have to login and configure it:

    gcloud auth login
    gcloud config set project <your project name>

You must also install the app-engine-java component:

    gcloud components update app-engine-java

# Supported operations

The library implements the following operations:

* Deploy an application
* Run an application on the local server
* Generate missing configuration files
* Delete a version of one or more modules
* Get logs for a version of one or more modules
* List versions of one or more modules
* Set the default version of a module
* Sets a Managed VM instance to managed by Google or the user
* Starts serving a version of one or more modules
* Stops serving a version of one or more modules

# How to use

Build the library using the "mvn clean install" command at the repository root directory, where the pom.xml file is located. This produces a app-tools-lib-for-java-0.3-SNAPSHOT.jar file in the "target" directory that you can import to your application's class path.

To deploy a new version, a client calls the library in the following way:

// Create action configuration. For example:

Path appYaml = Paths.get("path", "to", "appYaml");

DeployConfiguration configuration = DefaultDeployConfiguration.newBuilder(appYaml)

  .bucket("gs://my-gcs-bucket")

  .force(true)

  .promote(true)

  .build();

// Create an action object.

AppAction deployAction = new DeployAction(configuration);

// Execute the action.

deployAction.execute();
