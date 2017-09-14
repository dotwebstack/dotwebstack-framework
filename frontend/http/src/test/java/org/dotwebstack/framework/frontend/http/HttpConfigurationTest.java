package org.dotwebstack.framework.frontend.http;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HttpConfigurationTest {

  @Mock
  private HttpModule moduleA;

  @Mock
  private HttpModule moduleB;

  @Test
  public void noErrorsWithoutExtensions() {
    // Act & assert
    new HttpConfiguration(ImmutableList.of());
  }

  @Test
  public void modulesInitialized() {
    // Act
    HttpConfiguration httpConfiguration = new HttpConfiguration(ImmutableList.of(moduleA, moduleB));

    // Assert
    verify(moduleA).initialize(httpConfiguration);
    verify(moduleB).initialize(httpConfiguration);
  }

  @Test
  public void resourceNotAlreadyRegisteredTest() {
    // Arrange
    final String absolutePath = "https://run.forrest.run/";
    HttpConfiguration httpConfiguration = new HttpConfiguration(ImmutableList.of(moduleA, moduleB));
    org.glassfish.jersey.server.model.Resource.Builder resourceBuilder =
        org.glassfish.jersey.server.model.Resource.builder().path(absolutePath);
    // Assert
    assertThat(httpConfiguration.resourceAlreadyRegistered(absolutePath), equalTo(false));
    // Act
    httpConfiguration.registerResources(resourceBuilder.build());
    // Assert
    assertThat(httpConfiguration.getResources(), hasSize(1));
  }

  @Test
  public void resourceAlreadyRegisteredTest() {
    // Arrange
    final String absolutePath = "https://run.forrest.run/";
    HttpConfiguration httpConfiguration = new HttpConfiguration(ImmutableList.of(moduleA, moduleB));
    org.glassfish.jersey.server.model.Resource.Builder resourceBuilder =
        org.glassfish.jersey.server.model.Resource.builder().path(absolutePath);
    // Assert
    assertThat(httpConfiguration.resourceAlreadyRegistered(absolutePath), equalTo(false));
    // Act
    httpConfiguration.registerResources(resourceBuilder.build());
    // Assert
    assertThat(httpConfiguration.getResources(), hasSize(1));
    assertThat(httpConfiguration.resourceAlreadyRegistered(absolutePath), equalTo(true));
  }

}
