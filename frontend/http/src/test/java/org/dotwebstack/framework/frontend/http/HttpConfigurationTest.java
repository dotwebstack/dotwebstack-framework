package org.dotwebstack.framework.frontend.http;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class HttpConfigurationTest {

  @Test
  public void construct() {
    new HttpConfiguration();
  }

  @Test
  public void resourceNotAlreadyRegisteredTest() {
    final String absolutePath = "https://run.forrest.run/";
    HttpConfiguration httpConfiguration = new HttpConfiguration();
    org.glassfish.jersey.server.model.Resource.Builder resourceBuilder = org.glassfish.jersey.server.model.Resource
        .builder().path(absolutePath);
    assertThat(httpConfiguration.resourceAlreadyRegistered(absolutePath), equalTo(false));
    httpConfiguration.registerResources(resourceBuilder.build());
    assertThat(httpConfiguration.getResources(), hasSize(1));
  }

  @Test
  public void resourceAlreadyRegisteredTest() {
    final String absolutePath = "https://run.forrest.run/";
    HttpConfiguration httpConfiguration = new HttpConfiguration();
    org.glassfish.jersey.server.model.Resource.Builder resourceBuilder = org.glassfish.jersey.server.model.Resource
        .builder().path(absolutePath);
    assertThat(httpConfiguration.resourceAlreadyRegistered(absolutePath), equalTo(false));
    httpConfiguration.registerResources(resourceBuilder.build());
    assertThat(httpConfiguration.getResources(), hasSize(1));
    assertThat(httpConfiguration.resourceAlreadyRegistered(absolutePath), equalTo(true));
  }

}
