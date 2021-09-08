package org.dotwebstack.framework.ext.rml.mapping;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RmlMapperConfigurationTest {

  @Mock
  private RmlMapperConfigurer rmlMapperConfigurer;

  @Test
  void rmlMapper_configuredCorrectly_forConfig() {
    // Arrange
    var openApi = TestResources.openApi("config/openapi.yaml");
    var rmlOpenApiConfiguration = new RmlOpenApiConfiguration();
    var mappingsPerOperation = rmlOpenApiConfiguration.mappingsPerOperation(openApi);
    var rmlMapperConfiguration = new RmlMapperConfiguration();

    // Act
    var rmlMapper = rmlMapperConfiguration.rmlMapper(mappingsPerOperation, List.of(rmlMapperConfigurer));

    // Assert
    assertNotNull(rmlMapper);
    verify(rmlMapperConfigurer, times(1)).configureMapper(any());
  }
}
