package org.dotwebstack.framework.frontend.openapi;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import io.swagger.models.Scheme;
import io.swagger.models.Swagger;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;


public class BaseUriFactoryTest {

  @Rule
  public MockitoRule rule = MockitoJUnit.rule();

  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Test
  public void newBaseUri_returnsBaseUriString_ifAbsoluteNotEmpty() {
    // Arrange
    String basePath = "/rest/v2";
    Swagger swagger = new Swagger()
        .scheme(Scheme.HTTP)
        .host("ruimtelijkeplannen.nl:8485")
        .basePath(basePath);

    String expectedUri = "http://ruimtelijkeplannen.nl:8485/rest/v2";

    // Act
    String baseUri = BaseUriFactory.newBaseUri(swagger);

    // Assert
    assertThat(baseUri, is(expectedUri));
  }

  @Test
  public void newBaseUri_returnsBaseUriString_ifPortEmpty() {
    // Arrange
    String basePath = "/rest/v2";
    Swagger swagger = new Swagger()
        .scheme(Scheme.HTTP)
        .host("ruimtelijkeplannen.nl")
        .basePath(basePath);
    String expectedUri = "http://ruimtelijkeplannen.nl/rest/v2";

    // Act
    String baseUri = BaseUriFactory.newBaseUri(swagger);

    // Assert
    assertThat(baseUri, is(expectedUri));
  }

  @Test
  public void newBaseUri_returnsBaseUriString_ifSchemesEmpty() {
    // Arrange
    String basePath = "/rest/v2";
    Swagger swagger = new Swagger()
        .host("ruimtelijkeplannen.nl")
        .basePath(basePath);
    String expectedUri = "https://ruimtelijkeplannen.nl/rest/v2";

    // Act
    String baseUri = BaseUriFactory.newBaseUri(swagger);

    // Assert
    assertThat(baseUri, is(expectedUri));
  }

  @Test
  public void newBaseUri_ThrowsIllegalStateException_whenMalformed() {
    // Arrange
    String basePath = "/rest/v2";
    Swagger swagger = new Swagger()
        .scheme(Scheme.HTTP)
        .host("ruimtelijke plannen.nl : 8485")
        .basePath(basePath);

    // Assert
    exception.expect(IllegalStateException.class);
    exception.expectMessage("BaseUri could not be made");

    // Act
    BaseUriFactory.newBaseUri(swagger);
  }

}