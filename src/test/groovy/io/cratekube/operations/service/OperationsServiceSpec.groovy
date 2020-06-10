package io.cratekube.operations.service

import com.google.common.util.concurrent.MoreExecutors
import io.cratekube.cloud.client.api.EnvironmentsApi
import io.cratekube.cloud.client.model.Environment
import io.cratekube.cloud.client.model.EnvironmentRequest
import io.cratekube.cloud.client.model.ManagedResource
import io.cratekube.clustermgmt.client.model.BootstrapRequest
import io.cratekube.clustermgmt.client.model.Cluster
import io.cratekube.clustermgmt.client.model.ClusterNode
import io.cratekube.operations.model.Constants
import io.cratekube.operations.model.Status
import org.valid4j.errors.RequireViolation
import retrofit2.Call
import retrofit2.Response
import spock.lang.Specification
import io.cratekube.clustermgmt.client.api.DefaultApi as ClusterMgmtApi
import spock.lang.Subject
import spock.lang.Unroll

import java.util.concurrent.Executor

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.hasKey
import static org.hamcrest.Matchers.hasSize
import static org.hamcrest.Matchers.notNullValue
import static spock.util.matcher.HamcrestSupport.expect

class OperationsServiceSpec extends Specification {
  @Subject OperationsService subject
  ClusterMgmtApi clusterMgmtApi
  EnvironmentsApi cloudMgmtApi
  Executor executor

  def setup() {
    clusterMgmtApi = Mock(ClusterMgmtApi)
    cloudMgmtApi = Mock(EnvironmentsApi)
    executor = MoreExecutors.directExecutor()
    subject = new OperationsService(clusterMgmtApi, cloudMgmtApi, executor)
  }

  def 'should require constructor args'() {
    when:
    new OperationsService(cluster, cloud, exec)

    then:
    thrown RequireViolation

    where:
    cluster             | cloud             | exec
    null                | null              | null
    this.clusterMgmtApi | null              | null
    this.clusterMgmtApi | this.cloudMgmtApi | null
  }

  @Unroll
  def 'bootstrap environment cluster should require valid arg [#name]'() {
    when:
    subject.bootstrapEnvironmentCluster(name)

    then:
    thrown RequireViolation

    where:
    name << [null, '']
  }

  def 'boostrap environment cluster should halt execution if it already exists'() {
    given:
    cloudMgmtApi.environments >> Mock(Call) {
      execute() >> Response.success([new Environment(name: Constants.DEFAULT, status: Environment.StatusEnum.APPLIED)])
    }
    clusterMgmtApi.getCluster(Constants.DEFAULT, Constants.DEFAULT) >> Mock(Call) {
      execute() >> GroovyMock(Response) {
        isSuccessful() >> false
      }
    }

    when:
    subject.bootstrapEnvironmentCluster(Constants.DEFAULT)

    then:
    0 * cloudMgmtApi.createEnvironment(_)
    0 * clusterMgmtApi.bootstrapCluster(_, _)
  }
  def 'bootstrap environment cluster should create infrastructure and cluster'() {
    given:
    def awsInstance = 'aws_instance'
    def publicDns = 'xx.yy.zz'
    def publicDns2 = 'ww.xx.yy.zz'

    when:
    subject.bootstrapEnvironmentCluster(Constants.DEFAULT)

    then:
    1 * cloudMgmtApi.environments >> Mock(Call) {
      execute() >> Response.success([])
    }
    1 * cloudMgmtApi.createEnvironment(new EnvironmentRequest(name: Constants.DEFAULT)) >> Mock(Call)
    2 * cloudMgmtApi.environments >> Mock(Call) {
      execute() >> Response.success([new Environment(
        name: Constants.DEFAULT,
        status: Environment.StatusEnum.APPLIED,
        resources: [
          new ManagedResource(type: awsInstance, metadata: [publicDns: publicDns]),
          new ManagedResource(type: awsInstance, metadata: [publicDns: publicDns2])
        ]
      )])
    }
    1 * clusterMgmtApi.bootstrapCluster(Constants.DEFAULT, new BootstrapRequest(clusterName: Constants.DEFAULT, hostnames: [publicDns, publicDns2])) >> Mock(Call)
    1 * clusterMgmtApi.getCluster(Constants.DEFAULT, Constants.DEFAULT) >> Mock(Call) {
      execute() >> Response.success(new Cluster(nodes: [new ClusterNode(status: ClusterNode.StatusEnum.COMPLETED)]))
    }
  }

  def 'get environment clusters should return no environment clusters'() {
    when:
    def result = subject.environmentClusters

    then:
    1 * cloudMgmtApi.environments >> Mock(Call) {
      execute() >> Response.success([])
    }
    expect result, notNullValue()
    expect result.entrySet(), hasSize(0)
  }

  @Unroll
  def 'get environment cluster should return only environments with status #opsStatus'() {
    given:
    clusterMgmtApi.getCluster(Constants.DEFAULT, Constants.DEFAULT) >> Mock(Call) {
      execute() >> GroovyMock(Response) {
        isSuccessful() >> false
      }
    }
    when:
    def result = subject.environmentClusters

    then:
    1 * cloudMgmtApi.environments >> Mock(Call) {
      execute() >> Response.success([new Environment(name: Constants.DEFAULT, status: status)])
    }
    expect result, notNullValue()
    expect result, hasKey(Constants.DEFAULT)
    expect result[Constants.DEFAULT].status, equalTo(opsStatus)

    where:
    status                         | opsStatus
    Environment.StatusEnum.FAILED  | Status.CLOUD_CREATE_FAILED
    Environment.StatusEnum.PENDING | Status.CLOUD_CREATE_IN_PROGRESS
    Environment.StatusEnum.APPLIED | Status.CLOUD_CREATE_COMPLETE
  }

  @Unroll
  def 'get environment cluster should return environment and cluster with status #opsStatus'() {
    given:
    cloudMgmtApi.environments >> Mock(Call) {
      execute() >> Response.success([new Environment(name: Constants.DEFAULT, status: Environment.StatusEnum.APPLIED)])
    }

    when:
    def result = subject.environmentClusters

    then:
    1 * clusterMgmtApi.getCluster(Constants.DEFAULT, Constants.DEFAULT) >> Mock(Call) {
      execute() >> GroovyMock(Response) {
        body() >> cluster
        isSuccessful() >> true
      }
    }
    expect result, notNullValue()
    expect result, hasKey(Constants.DEFAULT)
    expect result[Constants.DEFAULT].status, equalTo(opsStatus)

    where:
    cluster                                                                           | opsStatus
    new Cluster(nodes: [new ClusterNode(status: ClusterNode.StatusEnum.FAILED)])      | Status.CLUSTER_CREATE_FAILED
    new Cluster(nodes: [new ClusterNode(status: ClusterNode.StatusEnum.IN_PROGRESS)]) | Status.CLUSTER_CREATE_IN_PROGRESS
    new Cluster(nodes: [new ClusterNode(status: ClusterNode.StatusEnum.COMPLETED)])   | Status.CLUSTER_CREATE_COMPLETE
  }

  def 'delete environment cluster should stop execution if environment cluster does not exist'() {
    given:
    cloudMgmtApi.environments >> Mock(Call) {
      execute() >> Response.success([])
    }

    when:
    subject.deleteEnvironmentCluster(Constants.DEFAULT)

    then:
    0 * cloudMgmtApi.deleteEnvironmentByName(_)
    0 * clusterMgmtApi.deleteCluster(_, _)
  }

  def 'delete environment cluster deletes environment cluster'() {
    given:
    cloudMgmtApi.environments >> Mock(Call) {
      execute() >> Response.success([new Environment(name: Constants.DEFAULT, status: Environment.StatusEnum.PENDING)])
    }

    when:
    subject.deleteEnvironmentCluster(Constants.DEFAULT)

    then:
    1 * cloudMgmtApi.environments >> Mock(Call) {
      execute() >> Response.success([new Environment(name: Constants.DEFAULT, status: Environment.StatusEnum.APPLIED)])
    }
    1 * clusterMgmtApi.getCluster(Constants.DEFAULT, Constants.DEFAULT) >> Mock(Call) {
      execute() >> Response.success(new Cluster(nodes: [new ClusterNode(status: ClusterNode.StatusEnum.COMPLETED)]))
    }
    1 * clusterMgmtApi.deleteCluster(Constants.DEFAULT, Constants.DEFAULT) >> Mock(Call)
    1 * clusterMgmtApi.getCluster(Constants.DEFAULT, Constants.DEFAULT) >> Mock(Call) {
      execute() >> GroovyMock(Response) {
        isSuccessful() >> false
      }
    }
    1 * cloudMgmtApi.deleteEnvironmentByName(Constants.DEFAULT) >> Mock(Call)
    1 * cloudMgmtApi.environments >> Mock(Call) {
      execute() >> Response.success([])
    }
  }
}
