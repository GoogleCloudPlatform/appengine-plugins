package com.google.cloud.tools.managedcloudsdk.components;

import com.google.cloud.tools.managedcloudsdk.MessageListener;
import com.google.cloud.tools.managedcloudsdk.executors.SdkExecutorServiceFactory;
import com.google.cloud.tools.managedcloudsdk.executors.SingleThreadExecutorServiceFactory;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import java.nio.file.Path;
import java.util.concurrent.Callable;

/** Install an SDK component. */
public class SdkComponentInstaller {

  private final Path gcloud;
  private final ComponentInstallerFactory componentInstallerFactory;
  private final SdkExecutorServiceFactory executorServiceFactory;

  /** Use {@link #newComponentInstaller} to instantiate. */
  SdkComponentInstaller(
      Path gcloud,
      ComponentInstallerFactory componentInstallerFactory,
      SdkExecutorServiceFactory executorServiceFactory) {
    this.gcloud = gcloud;
    this.componentInstallerFactory = componentInstallerFactory;
    this.executorServiceFactory = executorServiceFactory;
  }

  /**
   * Install a component on a separate thread.
   *
   * @param component component to install
   * @param messageListener listener to receive feedback on
   * @return a resultless future for controlling the process
   */
  public ListenableFuture<Void> installComponent(
      final SdkComponent component, final MessageListener messageListener) {
    ListeningExecutorService executorService = executorServiceFactory.newExecutorService();
    ListenableFuture<Void> resultFuture =
        executorService.submit(
            new Callable<Void>() {
              @Override
              public Void call() throws Exception {
                componentInstallerFactory
                    .newInstaller(gcloud, component, messageListener)
                    .install();
                return null;
              }
            });
    executorService.shutdown(); // shutdown executor after install
    return resultFuture;
  }

  /**
   * Configure and create a new Component Installer instance.
   *
   * @param gcloud path to gcloud in the cloud sdk
   * @return a new configured Cloud Sdk component installer
   */
  public static SdkComponentInstaller newComponentInstaller(Path gcloud) {

    ComponentInstallerFactory componentInstallerFactory = new ComponentInstallerFactory();
    SdkExecutorServiceFactory executorServiceFactory = new SingleThreadExecutorServiceFactory();

    return new SdkComponentInstaller(gcloud, componentInstallerFactory, executorServiceFactory);
  }
}
