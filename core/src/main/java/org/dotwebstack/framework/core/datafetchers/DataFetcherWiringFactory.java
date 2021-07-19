package org.dotwebstack.framework.core.datafetchers;

import graphql.schema.DataFetcher;
import graphql.schema.GraphQLNamedOutputType;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeReference;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.idl.FieldWiringEnvironment;
import graphql.schema.idl.WiringFactory;
import java.util.Optional;
import org.dotwebstack.framework.core.condition.GraphQlNativeEnabled;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

@Conditional(GraphQlNativeEnabled.class)
@Component
public final class DataFetcherWiringFactory implements WiringFactory {

  private final DotWebStackConfiguration dotWebStackConfiguration;

  private final GenericDataFetcher dataFetcher;

  public DataFetcherWiringFactory(DotWebStackConfiguration dotWebStackConfiguration, GenericDataFetcher dataFetcher) {
    this.dotWebStackConfiguration = dotWebStackConfiguration;
    this.dataFetcher = dataFetcher;
  }

  @Override
  public boolean providesDataFetcher(FieldWiringEnvironment environment) {
    return getObjectType(environment.getFieldType()).map(objectType -> dotWebStackConfiguration.getObjectTypes()
        .containsKey(objectType.getName()))
        .orElse(false);
  }

  @Override
  public DataFetcher<?> getDataFetcher(FieldWiringEnvironment environment) {
    return dataFetcher;
  }

  private Optional<GraphQLNamedOutputType> getObjectType(GraphQLType outputType) {
    GraphQLType rawType = outputType;

    while (!GraphQLTypeUtil.isNotWrapped(rawType)) {
      rawType = GraphQLTypeUtil.unwrapOne(rawType);
    }

    if (rawType instanceof GraphQLObjectType || rawType instanceof GraphQLTypeReference) {
      return Optional.of((GraphQLNamedOutputType) rawType);
    }

    return Optional.empty();
  }
}
