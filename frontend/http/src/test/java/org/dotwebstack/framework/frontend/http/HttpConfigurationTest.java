package org.dotwebstack.framework.frontend.http;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import org.glassfish.jersey.server.model.Resource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HttpConfigurationTest {

  private HttpConfiguration httpConfiguration;

  @Before
  public void setUp() {
    httpConfiguration = new HttpConfiguration();
  }

  @Test
  public void registerResource() {
    // Arrange
    Resource resource = Resource.builder().build();

    // Act
    httpConfiguration.registerResource(resource);

    // Assert
    assertThat(httpConfiguration.getResources(), hasSize(1));
  }

}
