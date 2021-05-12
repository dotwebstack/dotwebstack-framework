package org.dotwebstack.framework.service.openapi.response;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.parameters.RequestBody;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;

@ExtendWith(MockitoExtension.class)
class RequestBodyContextBuilderTest {

  @Test
  void validate_succeeds_forApplicationJsonMediaType() {
    // Arrange
    RequestBody requestBody = mock(RequestBody.class);
    Content content = mock(Content.class);
    when(content.get(MediaType.APPLICATION_JSON.toString()))
        .thenReturn(mock(io.swagger.v3.oas.models.media.MediaType.class));
    when(requestBody.getContent()).thenReturn(content);

    // Act / Assert
    RequestBodyContextBuilder.validate(requestBody);
  }

  @Test
  void validate_throwsException_forNullContent() {
    // Arrange
    RequestBody requestBody = mock(RequestBody.class);

    // Act / Assert
    assertThrows(IllegalArgumentException.class, () -> RequestBodyContextBuilder.validate(requestBody));
  }

  @Test
  void validate_throwsException_forMissingMediaType() {
    // Arrange
    RequestBody requestBody = mock(RequestBody.class);
    Content content = mock(Content.class);
    when(content.get(MediaType.APPLICATION_JSON.toString())).thenReturn(null);
    when(requestBody.getContent()).thenReturn(content);

    // Act / Assert
    assertThrows(InvalidConfigurationException.class, () -> RequestBodyContextBuilder.validate(requestBody));
  }
}
