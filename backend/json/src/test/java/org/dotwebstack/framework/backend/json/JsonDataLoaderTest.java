package org.dotwebstack.framework.backend.json;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.execution.ExecutionStepInfo;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingFieldSelectionSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.dotwebstack.framework.backend.json.config.JsonFieldConfiguration;
import org.dotwebstack.framework.backend.json.config.JsonTypeConfiguration;
import org.dotwebstack.framework.core.config.AbstractFieldConfiguration;
import org.dotwebstack.framework.core.config.AbstractTypeConfiguration;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.dotwebstack.framework.core.datafetchers.FieldKeyCondition;
import org.dotwebstack.framework.core.datafetchers.KeyCondition;
import org.dotwebstack.framework.core.datafetchers.LoadEnvironment;
import org.dotwebstack.framework.core.datafetchers.MappedByKeyCondition;
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

  @Mock
  private DotWebStackConfiguration dotWebStackConfiguration;

  private JsonDataLoader jsonDataLoader;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  void init() {
    jsonDataLoader = new JsonDataLoader(dotWebStackConfiguration, jsonDataService);
  }

  @Test
  void loadSingle_Beer1_ForKey() throws JsonProcessingException {
    FieldKeyCondition fieldKeyCondition = FieldKeyCondition.builder()
        .fieldValues(Map.of("identifier", "1"))
        .build();

    JsonNode jsonNode = getDataAsJsonNode();

    JsonTypeConfiguration jsonTypeConfiguration = createJsonTypeConfiguration("beers", "$.beers[?]");

    LoadEnvironment loadEnvironment = createLoadEnvironment();

    when(dotWebStackConfiguration.getTypeConfiguration(loadEnvironment)).thenReturn(jsonTypeConfiguration);

    when(jsonDataService.getJsonSourceData(jsonTypeConfiguration.getDataSourceFile())).thenReturn(jsonNode);

    Mono<Map<String, Object>> result = jsonDataLoader.loadSingle(fieldKeyCondition, loadEnvironment);

    assertThat(result.hasElement()
        .block(), is(true));
    Map<String, Object> data = result.block();
    assertThat(Objects.requireNonNull(data)
        .size(), is(3));
    assertThat(data.get(FIELD_IDENTIFIER), is(1));
    assertThat(data.get(FIELD_NAME), is("Alfa Edel Pils"));
  }

  @Test
  void loadSingle_Empty_ForNonExistingKey() throws JsonProcessingException {
    FieldKeyCondition fieldKeyCondition = FieldKeyCondition.builder()
        .fieldValues(Map.of("identifier", "not-existing-identifier"))
        .build();

    JsonNode jsonNode = getDataAsJsonNode();
    JsonTypeConfiguration jsonTypeConfiguration = createJsonTypeConfiguration("beers", "$.beers[?]");

    LoadEnvironment loadEnvironment = createLoadEnvironment();

    when(dotWebStackConfiguration.getTypeConfiguration(loadEnvironment)).thenReturn(jsonTypeConfiguration);

    when(jsonDataService.getJsonSourceData(jsonTypeConfiguration.getDataSourceFile())).thenReturn(jsonNode);

    Mono<Map<String, Object>> result = jsonDataLoader.loadSingle(fieldKeyCondition, loadEnvironment);

    assertThat(result.hasElement()
        .block(), is(false));
  }

  @Test
  void loadMany_Beers_WhenNoKeyProvided() throws Exception {
    JsonNode jsonNode = getDataAsJsonNode();
    JsonTypeConfiguration jsonTypeConfiguration = createJsonTypeConfiguration("beers", "$.beers");

    LoadEnvironment loadEnvironment = createLoadEnvironment();

    when(dotWebStackConfiguration.getTypeConfiguration(loadEnvironment)).thenReturn(jsonTypeConfiguration);

    when(jsonDataService.getJsonSourceData(jsonTypeConfiguration.getDataSourceFile())).thenReturn(jsonNode);

    Flux<Map<String, Object>> result = jsonDataLoader.loadMany(null, loadEnvironment);

    List<Map<String, Object>> resultList = new ArrayList<>(result.collectList()
        .toFuture()
        .get());

    assertThat(resultList.size(), is(2));
    assertThat(resultList.get(0)
        .get("name"), is("Alfa Edel Pils"));
    assertThat(resultList.get(1)
        .get("name"), is("Alfa Radler"));
  }

  @Test
  void batchLoadSingle_ThrowsException() {
    LoadEnvironment loadEnvironment = createLoadEnvironment();

    assertThrows(UnsupportedOperationException.class, () -> jsonDataLoader.batchLoadSingle(null, loadEnvironment));
  }

  @Test
  void batchLoadMany_ThrowsException() {
    LoadEnvironment loadEnvironment = createLoadEnvironment();

    assertThrows(UnsupportedOperationException.class, () -> jsonDataLoader.batchLoadMany(null, loadEnvironment));
  }

  private JsonTypeConfiguration createJsonTypeConfiguration(String name, String pathExpression) {
    Map<String, String> map = new HashMap<>();
    map.put(name, pathExpression);

    JsonTypeConfiguration jsonTypeConfiguration = new JsonTypeConfiguration();
    jsonTypeConfiguration.setQueryPaths(map);
    jsonTypeConfiguration.setFile(DATAFILE);
    jsonTypeConfiguration.setKeys(List.of(FIELD_IDENTIFIER));
    jsonTypeConfiguration.setFields(Map.of(FIELD_IDENTIFIER, new JsonFieldConfiguration()));
    return jsonTypeConfiguration;
  }

  private LoadEnvironment createLoadEnvironment() {
    DataFetchingFieldSelectionSet selectionSet = mock(DataFetchingFieldSelectionSet.class);

    return LoadEnvironment.builder()
        .executionStepInfo(mock(ExecutionStepInfo.class))
        .queryName(BEERS_QUERY_NAME)
        .selectionSet(selectionSet)
        .build();
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
    @Override
    public void init(DotWebStackConfiguration dotWebStackConfiguration) {}

    @Override
    public KeyCondition getKeyCondition(DataFetchingEnvironment environment) {
      return null;
    }

    @Override
    public KeyCondition getKeyCondition(String fieldName, Map<String, Object> source) {
      return null;
    }

    @Override
    public KeyCondition invertKeyCondition(MappedByKeyCondition mappedByKeyCondition, Map<String, Object> source) {
      return null;
    }


  }

  private static class UnsupportedFieldConfiguration extends AbstractFieldConfiguration {
    @Override
    public boolean isScalarField() {
      return false;
    }

    @Override
    public boolean isObjectField() {
      return false;
    }

    @Override
    public boolean isNestedObjectField() {
      return false;
    }

    @Override
    public boolean isAggregateField() {
      return false;
    }
  }

}
