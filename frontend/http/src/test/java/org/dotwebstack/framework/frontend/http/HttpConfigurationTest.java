package org.dotwebstack.framework.frontend.http;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import java.util.Collections;
import org.dotwebstack.framework.frontend.http.provider.graph.TurtleGraphMessageBodyWriter;
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

  @Mock
  private SupportedMediaTypesScanner supportedMediaTypesScanner;

  @Test
  public void noErrorsWithoutExtensions() {
    // Act & assert
    new HttpConfiguration(ImmutableList.of(), supportedMediaTypesScanner);
  }

  @Test
  public void modulesInitialized() {
    // Act
    HttpConfiguration httpConfiguration =
        new HttpConfiguration(ImmutableList.of(moduleA, moduleB), supportedMediaTypesScanner);

    // Assert
    verify(moduleA).initialize(httpConfiguration);
    verify(moduleB).initialize(httpConfiguration);
  }

  @Test
  public void resourceNotAlreadyRegisteredTest() {
    // Arrange
    final String absolutePath = "https://run.forrest.run/";
    HttpConfiguration httpConfiguration =
        new HttpConfiguration(ImmutableList.of(moduleA, moduleB), supportedMediaTypesScanner);
    org.glassfish.jersey.server.model.Resource.Builder resourceBuilder =
        org.glassfish.jersey.server.model.Resource.builder().path(absolutePath);
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
    HttpConfiguration httpConfiguration =
        new HttpConfiguration(ImmutableList.of(moduleA, moduleB), supportedMediaTypesScanner);
    org.glassfish.jersey.server.model.Resource.Builder resourceBuilder =
        org.glassfish.jersey.server.model.Resource.builder().path(absolutePath);
    assertThat(httpConfiguration.resourceAlreadyRegistered(absolutePath), equalTo(false));

    // Act
    httpConfiguration.registerResources(resourceBuilder.build());

    // Assert
    assertThat(httpConfiguration.getResources(), hasSize(1));
    assertThat(httpConfiguration.resourceAlreadyRegistered(absolutePath), equalTo(true));
  }


  @Test
  public void registersSparqlProviders() {
    // Arrange
    when(supportedMediaTypesScanner.getSparqlProviders()).thenReturn(
        Collections.singletonList(new TurtleGraphMessageBodyWriter()));

    // Act
    HttpConfiguration httpConfiguration =
        new HttpConfiguration(ImmutableList.of(), supportedMediaTypesScanner);

    // Assert
    assertThat(httpConfiguration.getInstances(), hasSize(1));
  }

}
