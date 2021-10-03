package org.dotwebstack.framework.core.backend;

import graphql.schema.DataFetcher;
import graphql.schema.GraphQLNamedOutputType;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.idl.FieldWiringEnvironment;
import graphql.schema.idl.WiringFactory;
import java.util.Optional;
import org.dotwebstack.framework.core.model.Schema;
import org.springframework.stereotype.Component;

@Component
class BackendDataFetcherWiringFactory implements WiringFactory {

  private final BackendModule<?> backendModule;

  private final Schema schema;

  public BackendDataFetcherWiringFactory(BackendModule<?> backendModule, Schema schema) {
    this.backendModule = backendModule;
    this.schema = schema;
  }

  @Override
  public boolean providesDataFetcher(FieldWiringEnvironment environment) {
    return getTypeName(environment.getFieldType()).flatMap(schema::getObjectType)
        .isPresent();
  }

  @Override
  public DataFetcher getDataFetcher(FieldWiringEnvironment environment) {
    var objectType = getTypeName(environment.getFieldType()).flatMap(schema::getObjectType)
        .orElseThrow();

    var backendLoader = backendModule.getBackendLoaderFactory()
        .create(objectType);

    return new BackendDataFetcher(backendLoader, schema);
  }

  // TODO: move to util class?
  private static Optional<String> getTypeName(GraphQLType outputType) {
    GraphQLType rawType = outputType;

    while (!GraphQLTypeUtil.isNotWrapped(rawType)) {
      rawType = GraphQLTypeUtil.unwrapOne(rawType);
    }

    if (rawType instanceof GraphQLNamedOutputType) {
      return Optional.of(((GraphQLNamedOutputType) rawType).getName());
    }

    return Optional.empty();
  }
}
