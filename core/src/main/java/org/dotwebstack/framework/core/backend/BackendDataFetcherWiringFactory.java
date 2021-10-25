package org.dotwebstack.framework.core.backend;

import static org.dotwebstack.framework.core.helpers.TypeHelper.getTypeName;

import graphql.schema.DataFetcher;
import graphql.schema.idl.FieldWiringEnvironment;
import graphql.schema.idl.WiringFactory;
import lombok.AllArgsConstructor;
import org.dotwebstack.framework.core.model.Schema;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
class BackendDataFetcherWiringFactory implements WiringFactory {

  private final BackendModule<?> backendModule;

  private final BackendRequestFactory requestFactory;

  private final Schema schema;

  private final BackendExecutionStepInfo backendExecutionStepInfo;

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

    return new BackendDataFetcher(backendLoader, requestFactory, backendExecutionStepInfo);
  }
}
