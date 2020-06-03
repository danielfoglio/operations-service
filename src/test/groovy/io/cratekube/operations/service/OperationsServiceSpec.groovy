package io.cratekube.operations.service

import com.google.common.util.concurrent.MoreExecutors
import io.cratekube.cloud.client.api.EnvironmentsApi
import io.cratekube.operations.model.EnvironmentCluster
import org.valid4j.errors.RequireViolation
import spock.lang.Specification
import io.cratekube.clustermgmt.client.api.DefaultApi as ClusterMgmtApi
import spock.lang.Subject
import spock.lang.Unroll

import java.util.concurrent.Executor

class OperationsServiceSpec extends Specification {
  @Subject OperationsService subject
  ClusterMgmtApi clusterMgmtApi
  EnvironmentsApi cloudMgmtApi
  Map<String, EnvironmentCluster> operationsCache
  Executor executor

  def setup() {
    clusterMgmtApi = Mock(ClusterMgmtApi)
    cloudMgmtApi = Mock(EnvironmentsApi)
    operationsCache = [:]
    executor = MoreExecutors.directExecutor()
    subject = new OperationsService(clusterMgmtApi, cloudMgmtApi, operationsCache, executor)
  }

  def 'should require constructor args'() {
    when:
    new OperationsService(cluster, cloud, cache, exec)

    then:
    thrown RequireViolation

    where:
    cluster             | cloud             | cache                | exec
    null                | null              | null                 | null
    this.clusterMgmtApi | null              | null                 | null
    this.clusterMgmtApi | this.cloudMgmtApi | null                 | null
    this.clusterMgmtApi | this.cloudMgmtApi | this.operationsCache | null
  }

  @Unroll
  def 'bootstrap cluster should require valid arg [#name]'() {
    when:
    subject.bootstrapEnvironmentCluster(name)

    then:
    thrown RequireViolation

    where:
    name << [null, '']
  }
}
