package org.dotwebstack.framework.frontend.http;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;

import com.google.common.collect.ImmutableList;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HttpConfigurationTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Mock
  private HttpModule moduleA;

  @Mock
  private HttpModule moduleB;


  @Test
  public void constructor_ThrowsException_WithMissingHttpModules() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new HttpConfiguration(null);
  }

  @Test
  public void constructor_ThrowsNoExceptions_WithoutExtensions() {
    // Act & assert
    new HttpConfiguration(ImmutableList.of());
  }

  @Test
  public void constructor_ModulesInitialized_WhenGiven() {
    // Act
    HttpConfiguration httpConfiguration = new HttpConfiguration(ImmutableList.of(moduleA, moduleB));

    // Assert
    verify(moduleA).initialize(httpConfiguration);
    verify(moduleB).initialize(httpConfiguration);
  }

  @Test
  public void registerResources_RegisterOnce_WhenAlreadyRegistered() {
    // Arrange
    final String absolutePath = "https://run.forrest.run/";
    HttpConfiguration httpConfiguration = new HttpConfiguration(ImmutableList.of(moduleA, moduleB));
    org.glassfish.jersey.server.model.Resource.Builder resourceBuilder =
        org.glassfish.jersey.server.model.Resource.builder().path(absolutePath);
    assertThat(httpConfiguration.resourceAlreadyRegistered(absolutePath), equalTo(false));

    // Act
    httpConfiguration.registerResources(resourceBuilder.build());

    // Assert
    assertThat(httpConfiguration.getResources(), hasSize(1));
  }

  @Test
  public void resourceAlreadyRegistered_ThrowException_WithNullValue() {
    // Arrange
    HttpConfiguration httpConfiguration = new HttpConfiguration(ImmutableList.of(moduleA, moduleB));

    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    httpConfiguration.resourceAlreadyRegistered(null);
  }

  @Test
  public void registerResources_RegisterAlreadyRegisteredTrue_WhenResourceRegistered() {
    // Arrange
    final String absolutePath = "https://run.forrest.run/";
    HttpConfiguration httpConfiguration = new HttpConfiguration(ImmutableList.of(moduleA, moduleB));
    org.glassfish.jersey.server.model.Resource.Builder resourceBuilder =
        org.glassfish.jersey.server.model.Resource.builder().path(absolutePath);
    assertThat(httpConfiguration.resourceAlreadyRegistered(absolutePath), equalTo(false));

    // Act
    httpConfiguration.registerResources(resourceBuilder.build());

    // Assert
    assertThat(httpConfiguration.resourceAlreadyRegistered(absolutePath), equalTo(true));
  }

}
