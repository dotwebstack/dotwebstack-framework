package org.dotwebstack.framework.backend.json.query;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

@ExtendWith(MockitoExtension.class)
public class JsonDataServiceTest {

  @Mock
  private ResourcePatternResolver resourceLoader;

  @Test
  void loadJsonDataFilesTest() throws IOException {
    Resource testJsonResource = mock(Resource.class);
    when(testJsonResource.getFilename()).thenReturn("test.json");
    when(testJsonResource.exists()).thenReturn(true);

    when(resourceLoader.getResources(anyString())).thenReturn(new Resource[] {testJsonResource});

    JsonDataService jsonDataService = new JsonDataService(resourceLoader);

    assertDoesNotThrow(() -> jsonDataService.loadJsonData());
  }

  @Test
  void loadUnknownFileThrowsInvalidConfigurationExceptionTest() throws IOException {
    Resource testJsonResource = mock(Resource.class);
    when(testJsonResource.getFilename()).thenReturn("unknown.json");
    when(testJsonResource.exists()).thenReturn(true);

    when(resourceLoader.getResources(anyString())).thenReturn(new Resource[] {testJsonResource});

    JsonDataService jsonDataService = new JsonDataService(resourceLoader);

    assertThrows(InvalidConfigurationException.class, () -> jsonDataService.loadJsonData());
  }

}
