package org.dotwebstack.framework.frontend.http;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.google.common.collect.ImmutableList;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MediaType;
import org.glassfish.jersey.process.Inflector;
import org.glassfish.jersey.server.model.Resource;
import org.junit.Before;
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

  private HttpConfiguration httpConfiguration;

  @Mock
  private HttpModule moduleA;

  @Mock
  private HttpModule moduleB;

  @Before
  public void setUp() {
    httpConfiguration = new HttpConfiguration(ImmutableList.of(moduleA, moduleB));
  }

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
    new HttpConfiguration(ImmutableList.of(moduleA, moduleB));
  }

  @Test
  public void constructor_ModulesInitialized_WhenGiven() {
    // Assert
    verify(moduleA).initialize(httpConfiguration);
    verify(moduleB).initialize(httpConfiguration);
  }

  @Test
  public void registerResources_RegisterOnce_WhenAlreadyRegistered() {
    // Arrange
    final String absolutePath = "https://run.forrest.run/";
    final Inflector<ContainerRequestContext, ?> inflector = mock(Inflector.class);
    final MediaType mediaType = mock(MediaType.class);
    Resource.Builder resourceBuilder = Resource.builder().path(absolutePath);
    resourceBuilder.addMethod(HttpMethod.GET).handledBy(inflector).produces(mediaType).nameBindings(
        ExpandFormatParameter.class);
    assertThat(httpConfiguration.resourceAlreadyRegistered(absolutePath, HttpMethod.GET),
        equalTo(false));

    // Act
    httpConfiguration.registerResources(resourceBuilder.build());

    // Assert
    assertThat(httpConfiguration.getResources(), hasSize(1));
  }

  @Test
  public void resourceAlreadyRegistered_ThrowException_WithNullValue() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    httpConfiguration.resourceAlreadyRegistered(null, null);
  }

  @Test
  public void registerResources_RegisterAlreadyRegisteredTrue_WhenResourceRegistered() {
    // Arrange
    final String absolutePath = "https://run.forrest.run/";
    final Inflector<ContainerRequestContext, ?> inflector = mock(Inflector.class);
    final MediaType mediaType = mock(MediaType.class);
    Resource.Builder resourceBuilder = Resource.builder().path(absolutePath);
    resourceBuilder.addMethod(HttpMethod.GET).handledBy(inflector).produces(mediaType).nameBindings(
        ExpandFormatParameter.class);
    assertThat(httpConfiguration.resourceAlreadyRegistered(absolutePath, HttpMethod.GET),
        equalTo(false));

    // Act
    httpConfiguration.registerResources(resourceBuilder.build());

    // Assert
    assertThat(httpConfiguration.resourceAlreadyRegistered(absolutePath, HttpMethod.GET),
        equalTo(true));
  }

}
