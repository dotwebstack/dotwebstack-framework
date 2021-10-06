package org.dotwebstack.framework.example.graphqlproxy;

import graphql.GraphQL;
import java.io.IOException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GraphQLConfiguration {

  @Bean
  public GraphQL graphQL(SchemaFactory schemaFactory) throws IOException {
    var schema = schemaFactory.create();

    return GraphQL.newGraphQL(schema)
        .build();
  }
}
