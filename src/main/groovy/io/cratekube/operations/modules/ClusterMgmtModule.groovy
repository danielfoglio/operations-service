package io.cratekube.operations.modules

import com.google.inject.Binder
import com.google.inject.Module
import io.cratekube.clustermgmt.client.ApiClient
import io.cratekube.clustermgmt.client.api.DefaultApi as ClusterMgmtApi
import io.cratekube.operations.AppConfig

class ClusterMgmtModule implements Module {
  AppConfig config

  ClusterMgmtModule(AppConfig config) {
    this.config = config
  }

  @Override
  void configure(Binder binder) {
    binder.bind ClusterMgmtApi toInstance buildClusterMgmtApi()
  }

  ClusterMgmtApi buildClusterMgmtApi() {
    def adminApiToken = config.auth.apiKeys.find {it.name == 'admin'}.key
    return new ApiClient().with {
      adapterBuilder.baseUrl config.clusterMgmt.url
      addAuthorization('apiToken') { chain ->
        def req = chain.request().newBuilder().header('Authorization', "Bearer ${adminApiToken}").build()
        chain.proceed req
      }
      createService ClusterMgmtApi
    }
  }
}
