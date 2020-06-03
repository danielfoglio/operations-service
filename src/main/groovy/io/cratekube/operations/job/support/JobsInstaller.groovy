package io.cratekube.operations.job.support

import de.spinscale.dropwizard.jobs.Job
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller
import ru.vyarus.dropwizard.guice.module.installer.install.TypeInstaller
import ru.vyarus.dropwizard.guice.module.installer.util.FeatureUtils
import ru.vyarus.dropwizard.guice.module.installer.util.Reporter

/**
 * Installer performs utility tasks:
 *  - searches for jobs and bind them to guice context (so {@link JobsManager} could install them
 *  - print registered jobs to console
 *
 * @see https://github.com/xvik/dropwizard-guicey-examples/blob/master/dropwizard-jobs/src/main/java/ru/vyarus/dropwizard/guice/examples/support/JobsInstaller.java
 */

class JobsInstaller implements FeatureInstaller, TypeInstaller<Job> {
  final Reporter reporter = new Reporter(JobsInstaller, 'jobs =')

  @Override
  boolean matches(Class type) {
    return FeatureUtils.is(type, Job)
  }

  @Override
  void report() {
    reporter.report()
  }

  @Override
  void install(Environment environment, Class<Job> type) {
    reporter.line('%s', type.name)
  }
}
