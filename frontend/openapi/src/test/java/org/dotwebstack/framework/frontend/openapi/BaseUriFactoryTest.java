package org.dotwebstack.framework.frontend.openapi;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;

import java.net.URI;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;


public class BaseUriFactoryTest {

  @Rule
  public MockitoRule rule = MockitoJUnit.rule();

  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Mock
  private URI absolutePath;

  @Test
  public void newBaseUri_returnsBaseUriString_ifAbsoluteNotEmpty() {
    // Arrange
    when(absolutePath.getScheme()).thenReturn("http");
    when(absolutePath.getHost()).thenReturn("ruimtelijkeplannen.nl");
    when(absolutePath.getPort()).thenReturn(8485);
    String basePath = "/rest/v2";
    String expectedUri = "http://ruimtelijkeplannen.nl:8485/rest/v2";

    // Act
    String baseUri = BaseUriFactory.newBaseUri(absolutePath, basePath);

    // Assert
    assertThat(baseUri, is(expectedUri));
  }

  @Test
  public void newBaseUri_returnsBaseUriString_ifPortEmpty() {
    // Arrange
    when(absolutePath.getScheme()).thenReturn("http");
    when(absolutePath.getHost()).thenReturn("ruimtelijkeplannen.nl");
    String basePath = "/rest/v2";
    String expectedUri = "http://ruimtelijkeplannen.nl:0/rest/v2";

    // Act
    String baseUri = BaseUriFactory.newBaseUri(absolutePath, basePath);

    // Assert
    assertThat(baseUri, is(expectedUri));
  }

  @Test
  public void newBaseUri_ThrowsIllegalStateException_whenMalformed() {
    // Arrange
    when(absolutePath.getScheme()).thenReturn("h t t p ");
    when(absolutePath.getHost()).thenReturn("ruimtelijkeplannen.nl");
    String basePath = "/rest/v2";

    // Assert
    exception.expect(IllegalStateException.class);
    exception.expectMessage("BaseUri could not be made");

    // Act
    BaseUriFactory.newBaseUri(absolutePath, basePath);
  }

}