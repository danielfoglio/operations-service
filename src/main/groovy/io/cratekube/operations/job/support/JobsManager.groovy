package io.cratekube.operations.job.support

import com.google.inject.Injector
import de.spinscale.dropwizard.jobs.GuiceJobManager
import io.cratekube.operations.AppConfig

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Bean will be recognized as Managed and installed automatically.
 * Used as a replacement for {@link de.spinscale.dropwizard.jobs.GuiceJobsBundle}.
 *
 * @see https://github.com/xvik/dropwizard-guicey-examples/blob/master/dropwizard-jobs/src/main/java/ru/vyarus/dropwizard/guice/examples/support/JobsManager.java
 */
@Singleton
class JobsManager extends GuiceJobManager {
  @Inject
  JobsManager(Injector injector, AppConfig config) {
    super(injector)
    configure(config)
  }
}
