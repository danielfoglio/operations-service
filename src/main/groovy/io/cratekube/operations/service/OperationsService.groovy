package io.cratekube.operations.service

import groovy.util.logging.Slf4j
import io.cratekube.cloud.client.api.EnvironmentsApi
import io.cratekube.cloud.client.model.Environment
import io.cratekube.cloud.client.model.Environment.StatusEnum as EnvironmentStatusEnum
import io.cratekube.cloud.client.model.EnvironmentRequest
import io.cratekube.clustermgmt.client.model.BootstrapRequest
import io.cratekube.clustermgmt.client.model.Cluster
import io.cratekube.operations.api.OperationsApi
import io.cratekube.operations.model.EnvironmentCluster
import io.cratekube.operations.model.Status
import io.cratekube.clustermgmt.client.model.ClusterNode.StatusEnum as ClusterStatusEnum
import io.cratekube.clustermgmt.client.api.DefaultApi as ClusterMgmtApi
import javax.inject.Inject
import java.util.concurrent.Executor

import static io.cratekube.operations.model.Constants.TEN
import static io.cratekube.operations.model.Constants.THIRTY_THOUSAND
import static org.hamcrest.Matchers.notNullValue
import static org.valid4j.Assertive.require
import static org.valid4j.matchers.ArgumentMatchers.notEmptyString

@Slf4j
class OperationsService implements OperationsApi {
  ClusterMgmtApi clusterMgmtApi
  EnvironmentsApi cloudMgmtApi
  Executor executor

  @Inject
  OperationsService(ClusterMgmtApi clusterMgmtApi, EnvironmentsApi cloudMgmtApi, Executor executor) {
    this.clusterMgmtApi = require clusterMgmtApi, notNullValue()
    this.cloudMgmtApi = require cloudMgmtApi, notNullValue()
    this.executor = require executor, notNullValue()
  }

  @Override
  void bootstrapEnvironmentCluster(String name) {
    require name, notEmptyString()

    EnvironmentCluster environmentCluster = environmentClusters[name]
    if (environmentCluster) {
      log.debug 'Environment cluster [{}] exists with status [{}]', name, environmentCluster.status
      return
    }

    log.debug 'Creating infrastructure for [{}]', name
    cloudMgmtApi.createEnvironment(new EnvironmentRequest(name: name)).execute()

    executor.execute {
      waitForEnvironmentStatus(name, EnvironmentStatusEnum.APPLIED)

      def environment = cloudMgmtApi.environments.execute().body().find { it.name == name }
      log.debug 'Infrastructure provisioning completed with state: [{}]', environment
      def hostnames = environment.resources.findAll {it.type == 'aws_instance'}.metadata.publicDns
      def bootstrapReq = new BootstrapRequest(clusterName: name, hostnames: hostnames)

      log.debug 'Creating cluster [{}] for request {}', name, bootstrapReq
      clusterMgmtApi.bootstrapCluster(name, bootstrapReq).execute()
      waitForClusterStatus(name, ClusterStatusEnum.COMPLETED)

      log.debug 'Environment cluster [{}] bootstrap completed', name
    }
  }

  @Override
  Map<String, EnvironmentCluster> getEnvironmentClusters() {
    def environments = cloudMgmtApi.environments.execute().body()
    if (!environments) {
      return [:]
    }

     return environments.collectEntries {
       def environmentCluster = new EnvironmentCluster(env: it)
       switch (it.status) {
         case EnvironmentStatusEnum.FAILED:
           environmentCluster.status = Status.CLOUD_CREATE_FAILED
           break
         case EnvironmentStatusEnum.PENDING:
           environmentCluster.status = Status.CLOUD_CREATE_IN_PROGRESS
           break
         case EnvironmentStatusEnum.APPLIED:
           environmentCluster.status = Status.CLOUD_CREATE_COMPLETE
       }

       def clusterReq = clusterMgmtApi.getCluster(it.name, it.name).execute()
       if (clusterReq.isSuccessful()) {
         Cluster cluster = clusterReq.body()
         environmentCluster.cluster = cluster
         switch (cluster.nodes.first().status) {
           case ClusterStatusEnum.FAILED:
             environmentCluster.status = Status.CLUSTER_CREATE_FAILED
             break
           case ClusterStatusEnum.IN_PROGRESS:
             environmentCluster.status = Status.CLUSTER_CREATE_IN_PROGRESS
             break
           case ClusterStatusEnum.COMPLETED:
             environmentCluster.status = Status.CLUSTER_CREATE_COMPLETE
         }
       }

       [it.name, environmentCluster]
     }
  }

  @Override
  void deleteEnvironmentCluster(String name) {
    require name, notEmptyString()

    EnvironmentCluster environmentCluster = environmentClusters[name]
    if (!environmentCluster) {
      log.debug 'No Environment cluster with name [{}]', name
      return
    }

    executor.execute {
      // ensure all operations are complete
      if (environmentCluster.status < Status.CLOUD_CREATE_COMPLETE) {
        waitForEnvironmentStatus(name, EnvironmentStatusEnum.APPLIED)
      }
      if (environmentCluster.status < Status.CLUSTER_CREATE_COMPLETE) {
        waitForClusterStatus(name, ClusterStatusEnum.COMPLETED)
      }

      // unused status results in waiting until the env/cluster has been removed
      log.debug 'Deleting cluster [{}]', name
      clusterMgmtApi.deleteCluster(name, name).execute()
      waitForClusterStatus(name, ClusterStatusEnum.NOT_STARTED)

      log.debug 'Deleting infrastructure for [{}]', name
      cloudMgmtApi.deleteEnvironmentByName(name).execute()
      waitForEnvironmentStatus(name, EnvironmentStatusEnum.FAILED)
    }
  }

  Environment waitForEnvironmentStatus(String name, EnvironmentStatusEnum status) {
    require name, notEmptyString()
    require status, notNullValue()

    Environment environment = null
    def attempts = 0
    while (environment?.status != status && attempts < TEN) {
      log.debug 'Waiting for environment [{}] to reach status [{}]. Attempt [{}] of 10. Current status [{}].', name, status, attempts, environment?.status
      if (attempts > 0) {
        sleep THIRTY_THOUSAND
      }
      environment = cloudMgmtApi.environments.execute().body().find { it.name == name }
      if (!environment) {
        log.debug 'Environment [{}] has been deleted or does not exist', name
        return null
      }
      attempts++
    }
    return environment?.status == status ? environment : null
  }

  Cluster waitForClusterStatus(String name, ClusterStatusEnum status) {
    require name, notEmptyString()
    require status, notNullValue()

    Cluster cluster = null
    def attempts = 0
    while (cluster?.nodes?.first()?.status != status && attempts < TEN) {
      log.debug 'Waiting for cluster [{}] to reach status [{}]. Attempt [{}] of 10. Current status [{}].', name, status, attempts, cluster?.nodes?.first()?.status
      if (attempts > 0) {
        sleep THIRTY_THOUSAND
      }
      def clusterRequest = clusterMgmtApi.getCluster(name, name).execute()
      if (!clusterRequest.isSuccessful()) {
        log.debug 'Cluster [{}] has been deleted or does not exist', name
        return null
      }
      cluster = clusterRequest.body()
      attempts++
    }
    return cluster?.nodes?.first()?.status == status ? cluster : null
  }
}
