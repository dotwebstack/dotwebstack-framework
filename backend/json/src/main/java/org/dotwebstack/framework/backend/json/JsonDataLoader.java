package org.dotwebstack.framework.backend.json;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import java.util.Set;
import org.dotwebstack.framework.backend.json.config.JsonTypeConfiguration;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.dotwebstack.framework.core.config.TypeConfiguration;
import org.dotwebstack.framework.core.datafetchers.BackendDataLoader;
import org.dotwebstack.framework.core.datafetchers.KeyCondition;
import org.dotwebstack.framework.core.datafetchers.LoadEnvironment;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.GroupedFlux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

@Component
public class JsonDataLoader implements BackendDataLoader {

  private DotWebStackConfiguration dotWebStackConfiguration;

  private final JsonDataService jsonDataService;

  public JsonDataLoader(DotWebStackConfiguration dotWebStackConfiguration, JsonDataService jsonDataService) {
    this.dotWebStackConfiguration = dotWebStackConfiguration;
    this.jsonDataService = jsonDataService;
  }

  @Override
  public boolean supports(TypeConfiguration<?> typeConfiguration) {
    return typeConfiguration instanceof JsonTypeConfiguration;
  }

  @Override
  public Mono<Map<String, Object>> loadSingle(KeyCondition filter, LoadEnvironment environment) {
    JsonTypeConfiguration typeConfiguration = dotWebStackConfiguration.getTypeConfiguration(environment);

    String jsonPathTemplate = typeConfiguration.getJsonPathTemplate(environment.getQueryName());

    JsonNode jsonData = getJsonDocumentByFile(typeConfiguration.getDataSourceFile());

    JsonQueryResult jsonQueryResult = new JsonQueryResult(jsonData, jsonPathTemplate);

    return null;
    // return jsonQueryResult.getResult(KeyCondition.flatten(filter))
    // .map(Mono::just)
    // .orElse(Mono.empty());
  }

  @Override
  public Flux<Tuple2<KeyCondition, Map<String, Object>>> batchLoadSingle(Set<KeyCondition> filters,
      LoadEnvironment environment) {
    throw new UnsupportedOperationException("This method is not yet implemented");
  }

  @Override
  public Flux<Map<String, Object>> loadMany(KeyCondition filter, LoadEnvironment environment) {
    JsonTypeConfiguration typeConfiguration = dotWebStackConfiguration.getTypeConfiguration(environment);

    String jsonPathTemplate = typeConfiguration.getJsonPathTemplate(environment.getQueryName());

    JsonNode jsonData = getJsonDocumentByFile(typeConfiguration.getDataSourceFile());

    JsonQueryResult jsonQueryResult = new JsonQueryResult(jsonData, jsonPathTemplate);

    return null;
    // return Flux.fromIterable(jsonQueryResult.getResults(KeyCondition.flatten(filter)));
  }

  @Override
  public Flux<GroupedFlux<KeyCondition, Map<String, Object>>> batchLoadMany(Set<KeyCondition> filters,
      LoadEnvironment environment) {
    throw new UnsupportedOperationException("This method is not yet implemented");
  }

  private JsonNode getJsonDocumentByFile(String fileName) {
    return jsonDataService.getJsonSourceData(fileName);
  }
}
