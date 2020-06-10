package io.cratekube.operations.resources

import groovy.transform.Immutable
import groovy.util.logging.Slf4j
import io.cratekube.operations.api.OperationsApi
import io.cratekube.operations.auth.User
import io.cratekube.operations.model.EnvironmentCluster
import io.dropwizard.auth.Auth
import io.swagger.annotations.Api
import io.swagger.annotations.ApiParam

import javax.annotation.security.RolesAllowed
import javax.inject.Inject
import javax.validation.Valid
import javax.validation.constraints.NotEmpty
import javax.ws.rs.Consumes
import javax.ws.rs.DELETE
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.Response

import static org.hamcrest.Matchers.notNullValue
import static org.valid4j.Assertive.require
import static org.valid4j.matchers.ArgumentMatchers.notEmptyString

/**
 * This API is responsible for generating cloud resources and provisioning Kubernetes clusters.
 */
@Slf4j
@Api('operations')
@Path('operations')
@Produces('application/json')
@Consumes('application/json')
class OperationsResource {
  OperationsApi operationsApi

  @Inject
  OperationsResource(OperationsApi operationsApi) {
    this.operationsApi = require operationsApi, notNullValue()
  }

  /**
   * Creates infrastructure and deploys a Kubernetes cluster on that infrastructure.
   *
   * @param body {@code non-null} request object
   * @param user {@code non-null} user principal
   * @return 202 accepted response given no exception, otherwise a 4xx/5xx response depending on the exception
   */
  @POST
  @RolesAllowed('admin')
  Response createEnvironmentCluster(
    @ApiParam @Valid EnvironmentClusterRequest body,
    @ApiParam(hidden = true) @Auth User user
  ) {
    require body, notNullValue()
    require user, notNullValue()

    log.debug '[create-cluster] user [{}] creating cluster {}', user.name, body
    operationsApi.bootstrapEnvironmentCluster(body.name)

    return Response.accepted().build()
  }

  /**
   * Retrieves all environment clusters
   *
   * @param user {@code non-null} user principal
   * @return all environment clusters or an empty map if none exist.
   */
  @GET
  @RolesAllowed('admin')
  Map<String, EnvironmentCluster> getEnvironmentClusters(
    @ApiParam(hidden = true) @Auth User user
  ) {
    require user, notNullValue()

    log.debug '[get-environment-cluster] user [{}]', user.name

    return operationsApi.environmentClusters
  }

  /**
   * Deletes a cluster and its infrastructure by name
   *
   * @param name {@code non-empty} cluster name
   * @param user {@code non-null} user principal
   * @return 202 accepted response given no exception, otherwise a 4xx/5xx response depending on the exception
   */
  @DELETE
  @Path('{name}')
  @RolesAllowed('admin')
  Response deleteEnvironmentCluster(
    @PathParam('name') String name,
    @ApiParam(hidden = true) @Auth User user
  ) {
    require name, notEmptyString()
    require user, notNullValue()

    log.debug '[delete-cluster] user [{}] deleting cluster [{}]', user.name, name
    operationsApi.deleteEnvironmentCluster(name)

    return Response.accepted().build()
  }

  /**
   * Request object for creating new clusters
   */
  @Immutable
  static class EnvironmentClusterRequest {
    /**
     * Name of the cluster to create
     */
    @NotEmpty
    String name
  }
}
