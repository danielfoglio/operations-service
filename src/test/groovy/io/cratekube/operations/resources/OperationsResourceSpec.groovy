package io.cratekube.operations.resources

import io.cratekube.operations.api.OperationsApi
import io.cratekube.operations.auth.User
import io.cratekube.operations.model.Constants
import io.cratekube.operations.model.EnvironmentCluster
import io.cratekube.operations.model.Status
import org.valid4j.errors.RequireViolation
import spock.lang.Specification
import spock.lang.Subject

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.hasKey
import static org.hamcrest.Matchers.notNullValue
import static spock.util.matcher.HamcrestSupport.expect

class OperationsResourceSpec extends Specification {
  @Subject OperationsResource subject
  OperationsApi operationsApi

  def setup() {
    operationsApi = Mock(OperationsApi)
    subject = new OperationsResource(operationsApi)
  }

  def 'should require constructor args'() {
    when:
    new OperationsResource(null)

    then:
    thrown RequireViolation
  }

  def 'create environment cluster should require valid args'() {
    when:
    subject.createEnvironmentCluster(req, usr)

    then:
    thrown RequireViolation

    where:
    req                                                                 | usr
    null                                                                | null
    new OperationsResource.EnvironmentClusterRequest()                  | null
    new OperationsResource.EnvironmentClusterRequest(Constants.DEFAULT) | null
  }

  def 'create environment cluster should return accepted response'() {
    given:
    def body = new OperationsResource.EnvironmentClusterRequest(Constants.DEFAULT)

    when:
    def response = subject.createEnvironmentCluster(body, new User())

    then:
    1 * operationsApi.bootstrapEnvironmentCluster(Constants.DEFAULT)
    expect response, notNullValue()
    expect response.status, equalTo(202)
  }

  def 'get environment cluster should require valid args'() {
    when:
    subject.getEnvironmentClusters(null)

    then:
    thrown RequireViolation
  }

  def 'get environment cluster should return environment clusters'() {
    given:
    EnvironmentCluster environmentCluster = new EnvironmentCluster(status: Status.CLUSTER_CREATE_COMPLETE)
    operationsApi.environmentClusters >> [(Constants.DEFAULT): environmentCluster]

    when:
    def response = subject.getEnvironmentClusters(new User())

    then:
    expect response, notNullValue()
    expect response, hasKey(Constants.DEFAULT)
    expect response[Constants.DEFAULT], equalTo(environmentCluster)
  }

  def 'delete environment cluster should require valid args'() {
    when:
    subject.deleteEnvironmentCluster(name, usr)

    then:
    thrown RequireViolation

    where:
    name              | usr
    null              | null
    Constants.DEFAULT | null
  }

  def 'delete environment cluster should return accepted response'() {
    when:
    def response = subject.deleteEnvironmentCluster(Constants.DEFAULT, new User())

    then:
    1 * operationsApi.deleteEnvironmentCluster(Constants.DEFAULT)
    expect response, notNullValue()
    expect response.status, equalTo(202)
  }
}
