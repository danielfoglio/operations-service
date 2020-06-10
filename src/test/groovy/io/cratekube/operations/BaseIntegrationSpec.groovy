package io.cratekube.operations

import groovy.transform.Memoized
import io.cratekube.cloud.client.api.EnvironmentsApi
import io.cratekube.clustermgmt.client.api.DefaultApi as ClusterMgmtApi
import io.cratekube.operations.api.OperationsApi
import org.spockframework.mock.MockUtil
import ru.vyarus.dropwizard.guice.test.spock.UseDropwizardApp
import spock.lang.Specification

import javax.inject.Inject
import javax.ws.rs.client.Client
import javax.ws.rs.client.Invocation
import java.util.concurrent.Executor

/**
 * Base class for all integration specs.  This class provides a client for interacting with the
 * Dropwizard application's API.
 */
@UseDropwizardApp(value = App, hooks = IntegrationSpecHook, config = 'src/test/resources/testapp.yml')
abstract class BaseIntegrationSpec extends Specification {
  MockUtil mockUtil = new MockUtil()
  @Inject Client client
  @Inject Executor executor
  @Inject OperationsApi operationsApi
  @Inject EnvironmentsApi cloudMgmtApi
  @Inject ClusterMgmtApi clusterMgmtApi
  @Inject AppConfig config

  def setup() {
    [operationsApi, cloudMgmtApi, clusterMgmtApi].findAll { mockUtil.isMock(it) }
      .each { mockUtil.attachMock(it, this) }
  }
  /**
   * Base path to use for API requests.  Extending classes should use a
   * {@code basePath} property to set the base path that should be used
   * for tests.
   *
   * @return base path for API requests
   */
  abstract String getBasePath()

  /**
   * Creates a client invocation builder using the provided path.
   *
   * @param path {@code non-null} api path to call
   * @return an {@link Invocation.Builder} instance for the request
   */
  Invocation.Builder baseRequest(String path = '') {
    return client.target("http://localhost:9000${basePath}${path}").request()
  }

  @Memoized
  protected Invocation.Builder requestWithAdminToken(String path = '') {
    baseRequest(path)
      .header('Authorization', "Bearer ${config.auth.apiKeys.find {it.name == 'admin'}.key}")
  }
}
