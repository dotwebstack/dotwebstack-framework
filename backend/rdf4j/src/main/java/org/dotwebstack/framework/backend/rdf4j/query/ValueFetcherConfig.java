package org.dotwebstack.framework.backend.rdf4j.query;

import lombok.NonNull;
import org.dotwebstack.framework.backend.rdf4j.converters.Rdf4jConverterRouter;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShapeRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ValueFetcherConfig {

  @Bean
  public ValueFetcher getValueFetcher(@NonNull NodeShapeRegistry nodeShapeRegistry,
      @NonNull Rdf4jConverterRouter router) {
    return new ValueFetcher(nodeShapeRegistry, router);
  }
}
