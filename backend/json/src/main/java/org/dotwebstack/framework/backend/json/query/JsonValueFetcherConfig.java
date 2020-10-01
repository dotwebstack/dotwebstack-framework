package org.dotwebstack.framework.backend.json.query;

import lombok.NonNull;
import org.dotwebstack.framework.backend.json.converters.JsonConverterRouter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JsonValueFetcherConfig {
  @Bean
  public JsonValueFetcher getValueFetcher(@NonNull JsonConverterRouter router) {
    return new JsonValueFetcher(router);
  }
}
