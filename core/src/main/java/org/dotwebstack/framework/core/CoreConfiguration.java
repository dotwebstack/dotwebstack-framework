package org.dotwebstack.framework.core;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.dotwebstack.framework.core.graphql.GraphQlService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class CoreConfiguration {

  @Bean
  public GraphQlService graphQlService(List<GraphQlService> beans) {
    return beans.get(0);
  }
}
