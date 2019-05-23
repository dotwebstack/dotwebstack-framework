package org.dotwebstack.framework.core;

import com.google.common.collect.ImmutableMap;
import graphql.schema.DataFetcher;
import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.idl.RuntimeWiring;
import lombok.NonNull;
import org.springframework.stereotype.Component;

@Component
class GraphqlIntegrationConfigurer implements GraphqlConfigurer {

  @Override
  public void configureRuntimeWiring(@NonNull RuntimeWiring.Builder builder) {
    // Register data fetcher for test data
    builder.codeRegistry(GraphQLCodeRegistry.newCodeRegistry()
        .dataFetcher(FieldCoordinates.coordinates("Query", Constants.BREWERY_FIELD),
            (DataFetcher<Object>) dataFetchingEnvironment -> ImmutableMap.of(Constants.BREWERY_IDENTIFIER_FIELD,
                Constants.BREWERY_IDENTIFIER_EXAMPLE_1, Constants.BREWERY_NAME_FIELD, Constants.BREWERY_NAME_EXAMPLE_1,
                Constants.BREWERY_FOUNDED_FIELD, Constants.BREWERY_FOUNDED_EXAMPLE_1,
                Constants.BREWERY_FOUNDED_AT_YEAR_FIELD, Constants.BREWERY_FOUNDED_EXAMPLE_1)));
  }

}
