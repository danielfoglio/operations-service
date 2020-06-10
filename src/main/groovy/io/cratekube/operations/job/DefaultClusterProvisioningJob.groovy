package io.cratekube.operations.job

import de.spinscale.dropwizard.jobs.Job
import de.spinscale.dropwizard.jobs.annotations.DelayStart
import de.spinscale.dropwizard.jobs.annotations.Every
import groovy.util.logging.Slf4j
import io.cratekube.operations.api.OperationsApi
import io.cratekube.operations.model.Constants
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException

import javax.inject.Inject

import static org.hamcrest.Matchers.notNullValue
import static org.valid4j.Assertive.require

/**
 * Responsible for provisioning the default user infrastructure and cluster.
 */
@Slf4j
// delaying the start by 30 seconds & only execute job once
@Every(value = '1min', repeatCount = 0)
@DelayStart('30s')
class DefaultClusterProvisioningJob extends Job {
  OperationsApi operationsApi

  @Inject
  DefaultClusterProvisioningJob(OperationsApi operationsApi) {
    this.operationsApi = require operationsApi, notNullValue()
  }

  /**
   * {@inheritDoc }
   */
  @Override
  void doJob(JobExecutionContext context) throws JobExecutionException {
    log.debug 'Executing DefaultClusterProvisioningJob'
    operationsApi.bootstrapEnvironmentCluster(Constants.DEFAULT)
  }
}
