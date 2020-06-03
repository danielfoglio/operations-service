package io.cratekube.operations

import de.spinscale.dropwizard.jobs.JobConfiguration
import groovy.transform.Immutable
import io.cratekube.operations.auth.ApiKeyAuthConfig
import io.dropwizard.Configuration
import io.dropwizard.client.JerseyClientConfiguration
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration

import javax.validation.Valid
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

/**
 * Configuration class for this Dropwizard application.
 */
class AppConfig extends Configuration implements JobConfiguration {
  JerseyClientConfiguration jerseyClient

  @Valid
  @NotNull
  SwaggerBundleConfiguration swagger

  @Valid
  @NotNull
  ApiKeyAuthConfig auth

  @Valid
  @NotNull
  CloudMgmtConfig cloudMgmt

  @Valid
  @NotNull
  ClusterMgmtConfig clusterMgmt
}

@Immutable
class CloudMgmtConfig {
  @NotEmpty
  String url
}

@Immutable
class ClusterMgmtConfig {
  @NotEmpty
  String url
}
