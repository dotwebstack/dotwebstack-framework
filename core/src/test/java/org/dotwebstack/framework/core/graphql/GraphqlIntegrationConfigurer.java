package org.dotwebstack.framework.core.graphql;

import static org.dotwebstack.framework.core.graphql.Constants.BUILDING_BUILT_AT_EXAMPLE_1;
import static org.dotwebstack.framework.core.graphql.Constants.BUILDING_BUILT_AT_FIELD;
import static org.dotwebstack.framework.core.graphql.Constants.BUILDING_BUILT_AT_YEAR_FIELD;
import static org.dotwebstack.framework.core.graphql.Constants.BUILDING_FIELD;
import static org.dotwebstack.framework.core.graphql.Constants.BUILDING_HEIGHT_EXAMPLE_1;
import static org.dotwebstack.framework.core.graphql.Constants.BUILDING_HEIGHT_FIELD;
import static org.dotwebstack.framework.core.graphql.Constants.BUILDING_IDENTIFIER_EXAMPLE_1;
import static org.dotwebstack.framework.core.graphql.Constants.BUILDING_IDENTIFIER_FIELD;

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
        .dataFetcher(FieldCoordinates.coordinates("Query", BUILDING_FIELD),
            (DataFetcher<Object>) dataFetchingEnvironment -> ImmutableMap.of(
                BUILDING_IDENTIFIER_FIELD, BUILDING_IDENTIFIER_EXAMPLE_1, BUILDING_HEIGHT_FIELD,
                BUILDING_HEIGHT_EXAMPLE_1, BUILDING_BUILT_AT_FIELD, BUILDING_BUILT_AT_EXAMPLE_1,
                BUILDING_BUILT_AT_YEAR_FIELD, BUILDING_BUILT_AT_EXAMPLE_1)));
  }

}
