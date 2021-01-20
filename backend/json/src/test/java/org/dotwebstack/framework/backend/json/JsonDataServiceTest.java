package org.dotwebstack.framework.backend.json;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

@ExtendWith(MockitoExtension.class)
class JsonDataServiceTest {

  @Mock
  private ResourcePatternResolver resourceLoader;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void loadJsonDataFilesTest() throws IOException {
    Resource testJsonResource = mock(Resource.class);
    when(testJsonResource.getFilename()).thenReturn("test.json");
    when(testJsonResource.exists()).thenReturn(true);

    when(resourceLoader.getResources(anyString())).thenReturn(new Resource[] {testJsonResource});

    JsonDataService jsonDataService = new JsonDataService(resourceLoader);

    assertDoesNotThrow(jsonDataService::loadJsonData);
  }

  @Test
  void getJsonSourceDataShouldReturnJsonNode() throws IOException {
    JsonNode source = objectMapper.readTree(getSingleBreweryAsJson());

    JsonDataService jsonDataService = new JsonDataService(resourceLoader);

    JsonNode result = jsonDataService.getJsonSourceData("test.json");

    assertThat(result, equalTo(source));
  }


  @Test
  void loadUnknownFileThrowsInvalidConfigurationExceptionTest() throws IOException {
    Resource testJsonResource = mock(Resource.class);
    when(testJsonResource.getFilename()).thenReturn("unknown.json");
    when(testJsonResource.exists()).thenReturn(true);

    when(resourceLoader.getResources(anyString())).thenReturn(new Resource[] {testJsonResource});

    JsonDataService jsonDataService = new JsonDataService(resourceLoader);

    assertThrows(InvalidConfigurationException.class, jsonDataService::loadJsonData);
  }

  private String getSingleBreweryAsJson() {
    return "{\n" + " \"breweries\":\n" + " [{\n" + "    \"identifier\": 1,\n"
        + "    \"brewmasters\": \"Jeroen van Hees\",\n" + "    \"founded\": \"2014-05-03\",\n"
        + "    \"name\": \"De Brouwerij\",\n" + "    \"beerCount\": 3,\n" + "    \"group\": \"Onafhankelijk\",\n"
        + "    \"beers\": [\n" + "      {\n" + "        \"identifier\": 1,\n" + "        \"brewery\": 1,\n"
        + "        \"name\": \"Alfa Edel Pils\",\n" + "        \"_metadata\": \"metadata\"\n" + "      },\n"
        + "      {\n" + "        \"identifier\": 2,\n" + "        \"brewery\": 1,\n"
        + "        \"name\": \"Alfa Radler\"\n" + "      }\n" + "    ],\n" + "    \"owners\": [\n"
        + "      \"J.v.Hees\",\n" + "      \"I.Verhoef\",\n" + "      \"L.du Clou\",\n" + "      \"M.Kuijpers\"\n"
        + "    ]\n" + "  }]\n" + "}\n";
  }

}
