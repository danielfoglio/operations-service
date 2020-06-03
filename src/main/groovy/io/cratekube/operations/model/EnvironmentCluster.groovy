package io.cratekube.operations.model

import io.cratekube.cloud.client.model.Environment
import io.cratekube.clustermgmt.client.model.Cluster

/**
 * Environment cluster with status
 */
class EnvironmentCluster {
  /** cloud infrastructure */
  Environment env
  /** cluster configuration */
  Cluster cluster
  /** status of environment cluster */
  Status status
}

/**
 * Status of environment cluster
 */
enum Status {
  CLOUD_CREATE_IN_PROGRESS,
  CLOUD_CREATE_FAILED,
  CLOUD_CREATE_COMPLETE,
  CLUSTER_CREATE_IN_PROGRESS,
  CLUSTER_CREATE_FAILED,
  CLUSTER_CREATE_COMPLETE,
}
