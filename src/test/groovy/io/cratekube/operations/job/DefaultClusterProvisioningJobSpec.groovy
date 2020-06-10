package io.cratekube.operations.job

import io.cratekube.operations.api.OperationsApi
import io.cratekube.operations.model.Constants
import org.quartz.JobExecutionContext
import org.valid4j.errors.RequireViolation
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

  def "do job creates a default cluster"() {
    when:
    subject.doJob(_ as JobExecutionContext)

    then:
    1 * operationsApi.bootstrapEnvironmentCluster(Constants.DEFAULT)
  }
}
