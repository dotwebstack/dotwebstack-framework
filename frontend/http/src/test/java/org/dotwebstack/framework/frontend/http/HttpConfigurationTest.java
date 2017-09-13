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
  private HttpExtension extensionA;

  @Mock
  private HttpExtension extensionB;

  @Test
  public void noErrorsWithoutExtensions() {
    // Act & Assert
    new HttpConfiguration(ImmutableList.of());
  }

  @Test
  public void extensionsInitialized() {
    // Act
    HttpConfiguration httpConfiguration =
        new HttpConfiguration(ImmutableList.of(extensionA, extensionB));

    // Assert
    verify(extensionA).initialize(httpConfiguration);
    verify(extensionB).initialize(httpConfiguration);
  }

  @Test
  public void resourceNotAlreadyRegisteredTest() {
    // Arrange
    final String absolutePath = "https://run.forrest.run/";

    // Act
    HttpConfiguration httpConfiguration =
        new HttpConfiguration(ImmutableList.of(extensionA, extensionB));

    // Assert
    assertThat(httpConfiguration.resourceAlreadyRegistered(absolutePath), equalTo(false));
  }

  @Test
  public void resourceAlreadyRegisteredTest() {
    // Arrange
    final String absolutePath = "https://run.forrest.run/";
    HttpConfiguration httpConfiguration =
        new HttpConfiguration(ImmutableList.of(extensionA, extensionB));
    org.glassfish.jersey.server.model.Resource.Builder resourceBuilder =
        org.glassfish.jersey.server.model.Resource.builder().path(absolutePath);

    // Act
    httpConfiguration.registerResources(resourceBuilder.build());

    // Assert
    assertThat(httpConfiguration.getResources(), hasSize(1));
    assertThat(httpConfiguration.resourceAlreadyRegistered(absolutePath), equalTo(true));
  }

}
