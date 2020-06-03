package io.cratekube.operations.modules

import com.google.common.util.concurrent.MoreExecutors
import com.google.inject.Provides
import io.cratekube.cloud.client.api.EnvironmentsApi
import io.cratekube.clustermgmt.client.api.DefaultApi as ClusterMgmtApi
import io.cratekube.operations.AppConfig
import io.cratekube.operations.api.OperationsApi
import io.cratekube.operations.model.Status
import io.cratekube.operations.modules.annotation.OperationsCache
import io.dropwizard.client.JerseyClientBuilder
import org.apache.commons.collections4.map.LRUMap
import ru.vyarus.dropwizard.guice.module.support.DropwizardAwareModule
import spock.mock.DetachedMockFactory

import javax.inject.Singleton
import javax.ws.rs.client.Client
import java.util.concurrent.Executor

/**
 * Guice module used for integration specs.
 */
class IntegrationSpecModule extends DropwizardAwareModule<AppConfig> {
  DetachedMockFactory mockFactory = new DetachedMockFactory()

  @Override
  protected void configure() {
    bind OperationsApi toInstance mock(OperationsApi)
    bind Executor toInstance MoreExecutors.directExecutor()
    bind EnvironmentsApi toInstance mock(EnvironmentsApi)
    bind ClusterMgmtApi toInstance mock(ClusterMgmtApi)
  }
  def <T> T mock(Class<T> type) {
    return mockFactory.Mock(type)
  }

  @Provides
  @Singleton
  Client clientProvider() {
    return new JerseyClientBuilder(environment()).using(configuration().jerseyClient).build('external-client')
  }
}
