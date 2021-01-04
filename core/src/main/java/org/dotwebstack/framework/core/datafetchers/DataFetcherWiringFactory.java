package org.dotwebstack.framework.core.datafetchers;

import graphql.schema.DataFetcher;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeReference;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.idl.FieldWiringEnvironment;
import graphql.schema.idl.WiringFactory;
import org.springframework.stereotype.Component;

@Component
public final class DataFetcherWiringFactory implements WiringFactory {

  private final GenericDataFetcher dataFetcher;

  public DataFetcherWiringFactory(GenericDataFetcher dataFetcher) {
    this.dataFetcher = dataFetcher;
  }

  @Override
  public boolean providesDataFetcher(FieldWiringEnvironment environment) {
    return isObjectType(environment.getFieldType());
  }

  @Override
  public DataFetcher<?> getDataFetcher(FieldWiringEnvironment environment) {
    return dataFetcher;
  }

  private boolean isObjectType(GraphQLType outputType) {
    GraphQLType rawType = outputType;

    while (!GraphQLTypeUtil.isNotWrapped(rawType)) {
      rawType = GraphQLTypeUtil.unwrapOne(rawType);
    }

    return rawType instanceof GraphQLObjectType
        || rawType instanceof GraphQLTypeReference;
  }
}
