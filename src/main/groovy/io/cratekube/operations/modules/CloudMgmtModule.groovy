package io.cratekube.operations.modules

import com.google.inject.Binder
import com.google.inject.Module
import io.cratekube.cloud.client.ApiClient
import io.cratekube.cloud.client.api.EnvironmentsApi
import io.cratekube.operations.AppConfig

class CloudMgmtModule implements Module {
  AppConfig config

  CloudMgmtModule(AppConfig config) {
    this.config = config
  }

  @Override
  void configure(Binder binder) {
    binder.bind EnvironmentsApi toInstance buildEnvironmentsApi()
  }

  EnvironmentsApi buildEnvironmentsApi() {
    def adminApiToken = config.auth.apiKeys.find {it.name == 'admin'}.key
    return new ApiClient().with {
      adapterBuilder.baseUrl config.cloudMgmt.url
      addAuthorization('apiToken') { chain ->
        def req = chain.request().newBuilder().header('Authorization', "Bearer ${adminApiToken}").build()
        chain.proceed req
      }
      createService EnvironmentsApi
    }
  }
}
