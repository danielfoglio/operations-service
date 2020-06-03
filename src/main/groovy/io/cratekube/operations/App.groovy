package io.cratekube.operations

import com.codahale.metrics.SharedMetricRegistries
import de.spinscale.dropwizard.jobs.Job
import io.cratekube.operations.modules.ProductionModule
import io.cratekube.operations.auth.ApiKeyAuthBundle
import io.cratekube.operations.auth.ApiKeyAuthConfig
import io.dropwizard.Application
import io.dropwizard.configuration.EnvironmentVariableSubstitutor
import io.dropwizard.configuration.SubstitutingSourceProvider
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import io.federecio.dropwizard.swagger.SwaggerBundle
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.test.binding.BindingsOverrideInjectorFactory

/**
 * Application for this Dropwizard application.
 */
class App extends Application<AppConfig> {
  static void main(String... args) {
    new App().run(args)
  }

  @Override
  void initialize(Bootstrap<AppConfig> bootstrap) {
    bootstrap.with {
      // allows the app.yml to contain env var references
      configurationSourceProvider = new SubstitutingSourceProvider(
        configurationSourceProvider,
        new EnvironmentVariableSubstitutor(false)
      )

      // adds access to the swagger UI once the app is running
      addBundle new SwaggerBundle<AppConfig>() {
        @Override
        protected SwaggerBundleConfiguration getSwaggerBundleConfiguration(AppConfig configuration) {
          return configuration.swagger
        }
      }

      addBundle new ApiKeyAuthBundle<AppConfig>() {
        @Override
        protected ApiKeyAuthConfig getApiKeyAuthConfig(AppConfig configuration) {
          return configuration.auth
        }
      }

      // configures the application to use Guice for dependency injection
      addBundle GuiceBundle.builder()
                           .enableAutoConfig('io.cratekube.operations')
                           .injectorFactory(new BindingsOverrideInjectorFactory())
                           .modules(new ProductionModule())
                           .build()

      // force dropwizard-jobs using main metrics registry for all jobs
      SharedMetricRegistries.add(Job.DROPWIZARD_JOBS_KEY, metricRegistry)
    }
  }

  @Override
  void run(AppConfig configuration, Environment environment) throws Exception {}
}
