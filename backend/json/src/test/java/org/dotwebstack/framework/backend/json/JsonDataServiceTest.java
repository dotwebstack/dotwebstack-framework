package org.dotwebstack.framework.backend.json;

import org.dotwebstack.framework.backend.json.query.JsonDataService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;

@ExtendWith(MockitoExtension.class)
public class JsonDataServiceTest {

  @Mock
  private ResourceLoader resourceLoader;

  private JsonDataService jsonDataService = new JsonDataService(resourceLoader);

  @Test
  void testCacheMapSize() throws IOException {

    jsonDataService.getJsonSourceData("test.json");
  }
}
