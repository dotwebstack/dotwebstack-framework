package org.dotwebstack.framework.backend.json;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.schema.GraphQLObjectType;
import graphql.schema.SelectedField;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.dotwebstack.framework.backend.json.config.JsonFieldConfiguration;
import org.dotwebstack.framework.backend.json.config.JsonTypeConfiguration;
import org.dotwebstack.framework.core.config.AbstractFieldConfiguration;
import org.dotwebstack.framework.core.config.AbstractTypeConfiguration;
import org.dotwebstack.framework.core.config.KeyConfiguration;
import org.dotwebstack.framework.core.datafetchers.LoadEnvironment;
import org.dotwebstack.framework.core.datafetchers.filters.FieldFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class JsonDataLoaderTest {

  private static final String FIELD_IDENTIFIER = "identifier";

  private static final String FIELD_NAME = "name";

  private static final String NODE_BEER = "Beer";

  private static final String BEERS_QUERY_NAME = "beers";

  private static final String DATAFILE = "test.json";

  @Mock
  private JsonDataService jsonDataService;

  private JsonDataLoader jsonDataLoader;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  void init() {
    jsonDataLoader = new JsonDataLoader(jsonDataService);
  }

  @Test
  void supports_True_ForJsonTypeConfiguration() {
    // Arrange
    JsonTypeConfiguration jsonTypeConfiguration = new JsonTypeConfiguration();

    // Act / Assert
    assertThat(jsonDataLoader.supports(jsonTypeConfiguration), is(true));
  }

  @Test
  void supports_False_ForUnsupportedConfiguration() {
    // Arrange
    UnsupportedTypeConfiguration unsupportedTypeConfiguration = new UnsupportedTypeConfiguration();

    // Act / Assert
    assertThat(jsonDataLoader.supports(unsupportedTypeConfiguration), is(false));
  }

  @Test
  void loadSingle_Beer1_ForKey() throws JsonProcessingException {
    // Arrange
    FieldFilter fieldFilter = FieldFilter.builder()
        .field("identifier")
        .value("1")
        .build();

    JsonNode jsonNode = getDataAsJsonNode();

    JsonTypeConfiguration jsonTypeConfiguration = createJsonTypeConfiguration("beers", "$.beers[?]");

    LoadEnvironment environment = createLoadEnvironment(jsonTypeConfiguration);

    when(jsonDataService.getJsonSourceData(jsonTypeConfiguration.getDataSourceFile())).thenReturn(jsonNode);

    // Act
    Mono<Map<String, Object>> result = jsonDataLoader.loadSingle(fieldFilter, environment);

    // Assert
    assertThat(result.hasElement()
        .block(), is(true));
    Map<String, Object> resultMap = result.block();
    assertThat(Objects.requireNonNull(resultMap)
        .size(), is(3));
    assertThat(resultMap.get(FIELD_IDENTIFIER), is(1));
    assertThat(resultMap.get(FIELD_NAME), is("Alfa Edel Pils"));
  }

  @Test
  void loadSingle_Empty_ForNonExistingKey() throws JsonProcessingException {
    // Arrange
    FieldFilter fieldFilter = FieldFilter.builder()
        .field("identifier")
        .value("not-existing-identifier")
        .build();

    JsonNode jsonNode = getDataAsJsonNode();
    JsonTypeConfiguration jsonTypeConfiguration = createJsonTypeConfiguration("beers", "$.beers[?]");

    LoadEnvironment loadEnvironment = createLoadEnvironment(jsonTypeConfiguration);
    when(jsonDataService.getJsonSourceData(jsonTypeConfiguration.getDataSourceFile())).thenReturn(jsonNode);

    // Act
    Mono<Map<String, Object>> result = jsonDataLoader.loadSingle(fieldFilter, loadEnvironment);

    // Assert
    assertThat(result.hasElement()
        .block(), is(false));
  }

  @Test
  void loadMany_Beers_WhenNoKeyProvided() throws Exception {
    // Arrange
    JsonNode jsonNode = getDataAsJsonNode();
    JsonTypeConfiguration jsonTypeConfiguration = createJsonTypeConfiguration("beers", "$.beers");

    LoadEnvironment loadEnvironment = createLoadEnvironment(jsonTypeConfiguration);
    when(jsonDataService.getJsonSourceData(jsonTypeConfiguration.getDataSourceFile())).thenReturn(jsonNode);

    // Act
    Flux<Map<String, Object>> result = jsonDataLoader.loadMany(null, loadEnvironment);

    // Assert
    List<Map<String, Object>> resultList = new ArrayList<>(result.collectList()
        .toFuture()
        .get());

    assertThat(result.collectList()
        .toFuture()
        .get()
        .size(), is(2));
    assertThat(resultList.get(0)
        .get("name"), is("Alfa Edel Pils"));
    assertThat(resultList.get(1)
        .get("name"), is("Alfa Radler"));
  }

  @Test
  void batchLoadSingle_ThrowsException() {
    // Arrange
    JsonTypeConfiguration jsonTypeConfiguration = createJsonTypeConfiguration("beers", "$.beers");

    LoadEnvironment loadEnvironment = createLoadEnvironment(jsonTypeConfiguration);

    // Act & Assert
    assertThrows(UnsupportedOperationException.class, () -> jsonDataLoader.batchLoadSingle(null, loadEnvironment));
  }

  @Test
  void batchLoadMany_ThrowsException() {
    // Arrange
    JsonTypeConfiguration jsonTypeConfiguration = createJsonTypeConfiguration("beers", "$.beers");

    LoadEnvironment loadEnvironment = createLoadEnvironment(jsonTypeConfiguration);

    // Act & Assert
    assertThrows(UnsupportedOperationException.class, () -> jsonDataLoader.batchLoadMany(null, loadEnvironment));
  }

  private JsonTypeConfiguration createJsonTypeConfiguration(String name, String pathExpression) {
    Map<String, String> map = new HashMap<>();
    map.put(name, pathExpression);

    JsonTypeConfiguration jsonTypeConfiguration = new JsonTypeConfiguration();
    KeyConfiguration keyConfiguration = new KeyConfiguration();
    keyConfiguration.setField(FIELD_IDENTIFIER);
    jsonTypeConfiguration.setQueryPaths(map);
    jsonTypeConfiguration.setFile(DATAFILE);
    jsonTypeConfiguration.setKeys(List.of(keyConfiguration));
    jsonTypeConfiguration.setFields(Map.of(FIELD_IDENTIFIER, new JsonFieldConfiguration()));
    return jsonTypeConfiguration;
  }

  private LoadEnvironment createLoadEnvironment(JsonTypeConfiguration jsonTypeConfiguration) {
    GraphQLObjectType graphQlObjectType = GraphQLObjectType.newObject()
        .name(NODE_BEER)
        .build();

    return LoadEnvironment.builder()
        .objectType(graphQlObjectType)
        .typeConfiguration(jsonTypeConfiguration)
        .queryName(BEERS_QUERY_NAME)
        .selectedFields(List.of(createSelectedField(FIELD_IDENTIFIER), createSelectedField(FIELD_NAME)))
        .build();
  }


  private SelectedField createSelectedField(String name) {
    return mock(SelectedField.class);
  }

  private JsonNode getDataAsJsonNode() throws JsonProcessingException {
    return objectMapper.readTree(getSingleBreweryAsJson());
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

  private static class UnsupportedTypeConfiguration extends AbstractTypeConfiguration<UnsupportedFieldConfiguration> {
  }

  private static class UnsupportedFieldConfiguration extends AbstractFieldConfiguration {
  }

}
