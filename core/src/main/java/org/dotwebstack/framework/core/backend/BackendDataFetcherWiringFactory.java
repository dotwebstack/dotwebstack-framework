package org.dotwebstack.framework.core.backend;

import static org.dotwebstack.framework.core.helpers.TypeHelper.getTypeName;

import graphql.schema.DataFetcher;
import graphql.schema.idl.FieldWiringEnvironment;
import graphql.schema.idl.WiringFactory;
import org.dotwebstack.framework.core.model.Schema;
import org.springframework.stereotype.Component;

@Component
class BackendDataFetcherWiringFactory implements WiringFactory {

  private final BackendModule<?> backendModule;

  private final BackendRequestFactory requestFactory;

  private final Schema schema;

  public BackendDataFetcherWiringFactory(BackendModule<?> backendModule, BackendRequestFactory requestFactory,
      Schema schema) {
    this.backendModule = backendModule;
    this.requestFactory = requestFactory;
    this.schema = schema;
  }

  @Override
  public boolean providesDataFetcher(FieldWiringEnvironment environment) {
    return getTypeName(environment.getFieldType()).flatMap(schema::getObjectType)
        .isPresent();
  }

  @Override
  public DataFetcher<?> getDataFetcher(FieldWiringEnvironment environment) {
    var objectType = getTypeName(environment.getFieldType()).flatMap(schema::getObjectType)
        .orElseThrow();

    var backendLoader = backendModule.getBackendLoaderFactory()
        .create(objectType);

    return new BackendDataFetcher(backendLoader, requestFactory);
  }
}
