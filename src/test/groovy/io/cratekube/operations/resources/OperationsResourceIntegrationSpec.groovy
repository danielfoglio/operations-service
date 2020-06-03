package io.cratekube.operations.resources

import io.cratekube.operations.BaseIntegrationSpec

import static javax.ws.rs.client.Entity.json
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.notNullValue
import static spock.util.matcher.HamcrestSupport.expect

class OperationsResourceIntegrationSpec extends BaseIntegrationSpec {
  String basePath = '/operations'

  def 'should get list response when executing GET'() {
    when:
    def response = requestWithAdminToken().get()
    then:
    expect response, notNullValue()
    expect response.status, equalTo(200)
  }

  def 'should get accepted response when executing POST'() {
    when:
    def response = requestWithAdminToken().post(json(new OperationsResource.EnvironmentClusterRequest()))
    then:
    expect response, notNullValue()
    expect response.status, equalTo(202)
  }
}
