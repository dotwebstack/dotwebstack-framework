package org.dotwebstack.framework.backend.json;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.dotwebstack.framework.core.datafetchers.filters.FieldFilter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JsonQueryResultTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  private JsonQueryResult jsonQueryResult;

  void setup(String jsonPathTemplate, String data) throws JsonProcessingException {
    JsonNode jsonNode = objectMapper.readTree(data);
    jsonQueryResult = new JsonQueryResult(jsonNode, jsonPathTemplate);
  }

  @Test
  void getBeersReturnsArrayListTest() throws Exception {
    // Arrange
    setup("$.beers", getSingleBreweryAsJson());

    // Act
    List<Map<String, Object>> result = jsonQueryResult.getResults(List.of());

    // Assert
    assertThat(result.size(), equalTo(2));
  }

  @Test
  void getBeersFromBreweryWithoutBeersShouldReturnEmptyListTest() throws Exception {
    // Arrange
    setup("$.beers", getSingleBreweryWithoutBeersAsJson());

    // Act
    List<Map<String, Object>> result = jsonQueryResult.getResults(List.of());

    // Assert
    assertThat(result.size(), equalTo(0));
  }


  @Test
  void getBeerByIdShouldReturnOneBeerTest() throws Exception {
    // Arrange
    setup("$.beers[?]", getSingleBreweryAsJson());

    FieldFilter fieldFilter = FieldFilter.builder()
        .field("identifier")
        .value("1")
        .build();

    // Act
    Optional<Map<String, Object>> result = jsonQueryResult.getResult(List.of(fieldFilter));

    // Assert
    assertThat((int) result.stream()
        .count(), equalTo(1));
  }

  @Test
  void getBeerByNotExistingIdShouldReturnEmptyListTest() throws Exception {
    // Arrange
    setup("$.beers[?]", getSingleBreweryAsJson());

    FieldFilter fieldFilter = FieldFilter.builder()
        .field("identifier")
        .value("3")
        .build();

    // Act
    Optional<Map<String, Object>> result = jsonQueryResult.getResult(List.of(fieldFilter));

    // Assert
    assertThat((int) result.stream()
        .count(), equalTo(0));
  }

  @Test
  void getBeerByIdShouldThrowExceptionIfSizeIsGreaterThanOneTest() throws Exception {
    // Arrange
    setup("$.beers[?]", getBreweryWithBeersAsJson());

    FieldFilter fieldFilter = FieldFilter.builder()
        .field("identifier")
        .value("1")
        .build();

    // Act & Assert
    assertThrows(IllegalStateException.class, () -> jsonQueryResult.getResult(List.of(fieldFilter)));
  }

  private String getSingleBreweryAsJson() {
    return "{\n" + "    \"identifier\": 1,\n" + "    \"brewmasters\": \"Jeroen van Hees\",\n"
        + "    \"founded\": \"2014-05-03\",\n" + "    \"name\": \"De Brouwerij\",\n" + "    \"beerCount\": 3,\n"
        + "    \"group\": \"Onafhankelijk\",\n" + "    \"beers\": [\n" + "      {\n" + "        \"identifier\": 1,\n"
        + "        \"brewery\": 1,\n" + "        \"name\": \"Alfa Edel Pils\"\n" + "      },\n" + "      {\n"
        + "        \"identifier\": 2,\n" + "        \"brewery\": 1,\n" + "        \"name\": \"Alfa Radler\"\n"
        + "      }\n" + "    ],\n" + "    \"owners\": [\n" + "      \"J.v.Hees\",\n" + "      \"I.Verhoef\",\n"
        + "      \"L.du Clou\",\n" + "      \"M.Kuijpers\"\n" + "    ]\n" + "  }\n";
  }

  private String getSingleBreweryWithoutBeersAsJson() {
    return "{\n" + "    \"identifier\": 1,\n" + "    \"brewmasters\": \"Jeroen van Hees\",\n"
        + "    \"founded\": \"2014-05-03\",\n" + "    \"name\": \"De Brouwerij\",\n" + "    \"beerCount\": 3,\n"
        + "    \"group\": \"Onafhankelijk\",\n" + "    \"beers\": [\n" + "    ],\n" + "    \"owners\": [\n"
        + "      \"J.v.Hees\",\n" + "      \"I.Verhoef\",\n" + "      \"L.du Clou\",\n" + "      \"M.Kuijpers\"\n"
        + "    ]\n" + "  }\n";
  }

  private String getBreweryWithBeersAsJson() {
    return "{\n" + "    \"identifier\": 1,\n" + "    \"brewmasters\": \"Jeroen van Hees\",\n"
        + "    \"founded\": \"2014-05-03\",\n" + "    \"name\": \"De Brouwerij\",\n" + "    \"beerCount\": 3,\n"
        + "    \"group\": \"Onafhankelijk\",\n" + "    \"beers\": [\n" + "      {\n" + "        \"identifier\": 1,\n"
        + "        \"brewery\": 1,\n" + "        \"name\": \"Alfa Edel Pils\"\n" + "      },\n" + "      {\n"
        + "        \"identifier\": 1,\n" + "        \"brewery\": 1,\n" + "        \"name\": \"Alfa Radler\"\n"
        + "      }\n" + "    ],\n" + "    \"owners\": [\n" + "      \"J.v.Hees\",\n" + "      \"I.Verhoef\",\n"
        + "      \"L.du Clou\",\n" + "      \"M.Kuijpers\"\n" + "    ]\n" + "  }\n";
  }
}
