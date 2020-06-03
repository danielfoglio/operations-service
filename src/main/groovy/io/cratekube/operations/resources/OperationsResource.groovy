package io.cratekube.operations.resources

import groovy.transform.Immutable
import groovy.util.logging.Slf4j
import io.cratekube.operations.api.OperationsApi
import io.cratekube.operations.auth.User
import io.cratekube.operations.model.EnvironmentCluster
import io.dropwizard.auth.Auth
import io.swagger.annotations.Api
import io.swagger.annotations.ApiImplicitParam
import io.swagger.annotations.ApiImplicitParams
import io.swagger.annotations.ApiParam

import javax.annotation.security.RolesAllowed
import javax.inject.Inject
import javax.ws.rs.Consumes
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.Response

import static org.hamcrest.Matchers.notNullValue
import static org.valid4j.Assertive.require

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

  @POST
  @RolesAllowed('admin')
  @ApiImplicitParams(
    @ApiImplicitParam(name = 'Authorization', value = 'API token', required = true, dataType = 'string', paramType = 'header')
  )
  Response createEnvironmentCluster(
    @ApiParam EnvironmentClusterRequest body,
    @ApiParam(hidden = true) @Auth User user
  ) {
    require body, notNullValue()
    require user, notNullValue()
    log.debug '[create-cluster] user [{}] creating cluster {}', user.name, body

    return Response.accepted().build()
  }

  @GET
  @RolesAllowed('admin')
  @ApiImplicitParams(
    @ApiImplicitParam(name = 'Authorization', value = 'API token', required = true, dataType = 'string', paramType = 'header')
  )
  Map<String, EnvironmentCluster> getEnvironmentClusters(
    @ApiParam(hidden = true) @Auth User user
  ) {
    require user, notNullValue()
    log.debug '[get-statuses] user [{}] retrieving cluster statuses', user.name

    return [:]
  }

  /**
   * Request object for creating new clusters
   */
  @Immutable
  static class EnvironmentClusterRequest {
    /**
     * Name of the cluster to create
     */
    String name
  }
}
