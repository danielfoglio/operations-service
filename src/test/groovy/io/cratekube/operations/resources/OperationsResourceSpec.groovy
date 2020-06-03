package io.cratekube.operations.resources

import io.cratekube.operations.api.OperationsApi
import org.valid4j.errors.RequireViolation
import spock.lang.Specification
import spock.lang.Subject

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

  def 'get statuses should require valid args'() {
    when:
    subject.getEnvironmentClusters(null)

    then:
    thrown RequireViolation
  }

  def 'create cluster should require valid args'() {
    when:
    subject.createEnvironmentCluster(req, usr)

    then:
    thrown RequireViolation

    where:
    req                                        | usr
    null                                       | null
    new OperationsResource.EnvironmentClusterRequest() | null
  }
}
