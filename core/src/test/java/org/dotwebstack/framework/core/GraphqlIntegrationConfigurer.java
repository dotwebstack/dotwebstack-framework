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
        .dataFetcher(FieldCoordinates.coordinates("Query", Constants.BUILDING_FIELD),
            (DataFetcher<Object>) dataFetchingEnvironment -> ImmutableMap.of(
                Constants.BUILDING_IDENTIFIER_FIELD, Constants.BUILDING_IDENTIFIER_EXAMPLE_1,
                Constants.BUILDING_HEIGHT_FIELD,
                Constants.BUILDING_HEIGHT_EXAMPLE_1, Constants.BUILDING_BUILT_AT_FIELD,
                Constants.BUILDING_BUILT_AT_EXAMPLE_1,
                Constants.BUILDING_BUILT_AT_YEAR_FIELD, Constants.BUILDING_BUILT_AT_EXAMPLE_1)));
  }

}
