package io.cratekube.operations.job

import io.cratekube.operations.api.OperationsApi
import io.cratekube.operations.model.Status
import io.cratekube.operations.resources.OperationsResource
import io.cratekube.operations.service.OperationsService
import org.valid4j.errors.RequireViolation
import spock.lang.PendingFeature
import spock.lang.Specification
import spock.lang.Subject

class DefaultClusterProvisioningJobSpec extends Specification {
  @Subject DefaultClusterProvisioningJob subject
  OperationsApi operationsApi

  def setup() {
    operationsApi = Mock(OperationsApi)
    subject = new DefaultClusterProvisioningJob(operationsApi)
  }

  def 'should require constructor args'() {
    when:
    new DefaultClusterProvisioningJob(null)

    then:
    thrown RequireViolation
  }

//  @PendingFeature
//  def "do job creates a default cluster"() {
//    when:
//    subject.doJob(_)
//
//    then:
//
//  }
}
