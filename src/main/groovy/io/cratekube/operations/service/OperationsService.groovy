package io.cratekube.operations.service

import groovy.util.logging.Slf4j
import io.cratekube.cloud.client.api.EnvironmentsApi
import io.cratekube.cloud.client.model.Environment
import io.cratekube.cloud.client.model.Environment.StatusEnum as EnvironmentStatusEnum
import io.cratekube.clustermgmt.client.api.DefaultApi as ClusterMgmtApi
import io.cratekube.clustermgmt.client.model.Cluster
import io.cratekube.operations.api.OperationsApi
import io.cratekube.operations.model.EnvironmentCluster
import io.cratekube.operations.modules.annotation.OperationsCache
import io.cratekube.clustermgmt.client.model.ClusterNode.StatusEnum as ClusterStatusEnum
import javax.inject.Inject
import java.util.concurrent.Executor

import static org.hamcrest.Matchers.notNullValue
import static org.valid4j.Assertive.require
import static org.valid4j.matchers.ArgumentMatchers.notEmptyString

@Slf4j
class OperationsService implements OperationsApi {
  ClusterMgmtApi clusterMgmtApi
  EnvironmentsApi cloudMgmtApi
  Map<String, EnvironmentCluster> operationsCache
  Executor executor

  @Inject
  OperationsService(ClusterMgmtApi clusterMgmtApi, EnvironmentsApi cloudMgmtApi, @OperationsCache Map<String, EnvironmentCluster> operationsCache, Executor executor) {
    this.clusterMgmtApi = require clusterMgmtApi, notNullValue()
    this.cloudMgmtApi = require cloudMgmtApi, notNullValue()
    this.operationsCache = require operationsCache, notNullValue()
    this.executor = require executor, notNullValue()
  }

  @Override
  void bootstrapEnvironmentCluster(String name) {
    require name, notEmptyString()
  }

  @Override
  Map<String, EnvironmentCluster> getEnvironmentClusters() {
    return [:]
  }

  Environment waitForEnvironmentStatus(
    String name,
    EnvironmentStatusEnum status
  ) {
    require name, notEmptyString()
    require status, notNullValue()

    return null
  }

  Cluster waitForClusterStatus(
    String name,
    ClusterStatusEnum status
  ) {
    require name, notEmptyString()
    require status, notNullValue()

    return null
  }
}
