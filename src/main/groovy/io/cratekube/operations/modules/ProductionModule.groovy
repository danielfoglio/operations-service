package io.cratekube.operations.modules

import com.google.inject.Provides
import io.cratekube.operations.AppConfig
import io.cratekube.operations.api.OperationsApi
import io.cratekube.operations.model.EnvironmentCluster
import io.cratekube.operations.model.Status
import io.cratekube.operations.modules.annotation.OperationsCache
import io.cratekube.operations.service.OperationsService
import org.apache.commons.collections4.map.LRUMap
import ru.vyarus.dropwizard.guice.module.support.DropwizardAwareModule

import javax.inject.Singleton
import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 * Default module to be used when running this application.
 */
class ProductionModule extends DropwizardAwareModule<AppConfig> {
  @Override
  protected void configure() {
    bind OperationsApi annotatedWith OperationsCache to OperationsService
    bind Executor toInstance Executors.newCachedThreadPool()
    install new CloudMgmtModule(configuration())
    install new ClusterMgmtModule(configuration())
  }

  @Provides
  @OperationsCache
  @Singleton
  static Map<String, EnvironmentCluster> clusterBootstrapCache() {
    return new LRUMap<String, EnvironmentCluster>()
  }
}
