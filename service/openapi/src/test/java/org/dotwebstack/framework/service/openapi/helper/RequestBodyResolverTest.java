package org.dotwebstack.framework.service.openapi.helper;

import static org.dotwebstack.framework.service.openapi.helper.RequestBodyResolver.REQUEST_BODIES_PATH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.parameters.RequestBody;
import java.util.Map;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RequestBodyResolverTest {
  @Mock
  private OpenAPI openApi;

  @Test
  void resolveRequestBody_throwsException_withWrongRefPath() {
    // Arrange
    RequestBody requestBody = mock(RequestBody.class);
    when(requestBody.get$ref()).thenReturn("wrongPath");

    // Act / Assert
    assertThrows(InvalidConfigurationException.class,
        () -> RequestBodyResolver.resolveRequestBody(openApi, requestBody));
  }

  @Test
  void resolveRequestBody_throwsException_withNoRequestBodies() {
    // Arrange
    RequestBody requestBody = mock(RequestBody.class);
    when(requestBody.get$ref()).thenReturn(REQUEST_BODIES_PATH + "UnknownRequestBody");
    when(openApi.getComponents()).thenReturn(mock(Components.class));

    // Act / Assert
    assertThrows(InvalidConfigurationException.class,
        () -> RequestBodyResolver.resolveRequestBody(openApi, requestBody));
  }

  @Test
  void resolveRequestBody_throwsException_withWrongPath() {
    // Arrange
    RequestBody requestBody = mock(RequestBody.class);
    when(requestBody.get$ref()).thenReturn(REQUEST_BODIES_PATH + "/Wrong");

    RequestBody referencedRequestBody = mock(RequestBody.class);
    Components components = mock(Components.class);
    when(components.getRequestBodies()).thenReturn(Map.of("MyRequestBody", referencedRequestBody));
    when(this.openApi.getComponents()).thenReturn(components);

    // Act / Assert
    assertThrows(InvalidConfigurationException.class,
        () -> RequestBodyResolver.resolveRequestBody(openApi, requestBody));
  }

  @Test
  void resolveRequestBody_resolves_withCorrectPath() {
    // Arrange
    RequestBody requestBody = mock(RequestBody.class);
    when(requestBody.get$ref()).thenReturn(REQUEST_BODIES_PATH + "MyRequestBody");

    RequestBody referencedRequestBody = mock(RequestBody.class);
    Components components = mock(Components.class);
    when(components.getRequestBodies()).thenReturn(Map.of("MyRequestBody", referencedRequestBody));
    when(this.openApi.getComponents()).thenReturn(components);

    // Act / Assert
    assertEquals(referencedRequestBody, RequestBodyResolver.resolveRequestBody(openApi, requestBody));
  }

}
