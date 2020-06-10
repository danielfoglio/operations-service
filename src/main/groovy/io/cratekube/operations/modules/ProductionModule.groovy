package io.cratekube.operations.modules

import io.cratekube.operations.AppConfig
import io.cratekube.operations.api.OperationsApi
import io.cratekube.operations.service.OperationsService
import ru.vyarus.dropwizard.guice.module.support.DropwizardAwareModule

import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 * Default module to be used when running this application.
 */
class ProductionModule extends DropwizardAwareModule<AppConfig> {
  @Override
  protected void configure() {
    bind OperationsApi to OperationsService
    bind Executor toInstance Executors.newCachedThreadPool()
    install new CloudMgmtModule(configuration())
    install new ClusterMgmtModule(configuration())
  }
}
