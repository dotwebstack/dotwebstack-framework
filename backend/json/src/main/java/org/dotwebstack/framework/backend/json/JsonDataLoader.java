package org.dotwebstack.framework.backend.json;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Set;
import org.dotwebstack.framework.backend.json.config.JsonTypeConfiguration;
import org.dotwebstack.framework.core.config.TypeConfiguration;
import org.dotwebstack.framework.core.datafetchers.BackendDataLoader;
import org.dotwebstack.framework.core.datafetchers.DataLoaderResult;
import org.dotwebstack.framework.core.datafetchers.LoadEnvironment;
import org.dotwebstack.framework.core.datafetchers.filters.Filter;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

@Component
public class JsonDataLoader implements BackendDataLoader {

  private final JsonDataService jsonDataService;

  public JsonDataLoader(JsonDataService jsonDataService) {
    this.jsonDataService = jsonDataService;
  }

  @Override
  public boolean supports(TypeConfiguration<?> typeConfiguration) {
    return typeConfiguration instanceof JsonTypeConfiguration;
  }

  @Override
  public Mono<DataLoaderResult> loadSingle(Filter filter, LoadEnvironment environment) {
    JsonTypeConfiguration typeConfiguration = (JsonTypeConfiguration) environment.getTypeConfiguration();

    String jsonPathTemplate = typeConfiguration.getJsonPathTemplate(environment.getQueryName());

    JsonNode jsonData = getJsonDocumentByFile(typeConfiguration.getDataSourceFile());

    JsonQueryResult jsonQueryResult = new JsonQueryResult(jsonData, jsonPathTemplate);

    return jsonQueryResult.getResult(Filter.flatten(filter))
        .map(map -> DataLoaderResult.builder()
            .data(map)
            .build())
        .map(Mono::just)
        .orElse(Mono.empty());
  }

  @Override
  public Flux<Tuple2<Filter, DataLoaderResult>> batchLoadSingle(Set<Filter> filters, LoadEnvironment environment) {
    throw new UnsupportedOperationException("This method is not yet implemented");
  }

  @Override
  public Flux<DataLoaderResult> loadMany(Filter filter, LoadEnvironment environment) {
    JsonTypeConfiguration typeConfiguration = (JsonTypeConfiguration) environment.getTypeConfiguration();

    String jsonPathTemplate = typeConfiguration.getJsonPathTemplate(environment.getQueryName());

    JsonNode jsonData = getJsonDocumentByFile(typeConfiguration.getDataSourceFile());

    JsonQueryResult jsonQueryResult = new JsonQueryResult(jsonData, jsonPathTemplate);

    return Flux.fromStream(jsonQueryResult.getResults(Filter.flatten(filter))
        .stream()
        .map(map -> DataLoaderResult.builder()
            .data(map)
            .build()));
  }

  @Override
  public Flux<Flux<DataLoaderResult>> batchLoadMany(List<Filter> filters, LoadEnvironment environment) {
    throw new UnsupportedOperationException("This method is not yet implemented");
  }

  private JsonNode getJsonDocumentByFile(String fileName) {
    return jsonDataService.getJsonSourceData(fileName);
  }
}
