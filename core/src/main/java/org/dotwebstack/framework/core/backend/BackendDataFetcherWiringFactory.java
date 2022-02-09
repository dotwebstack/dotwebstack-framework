package org.dotwebstack.framework.core.backend;

import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.AGGREGATE_TYPE;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalStateException;
import static org.dotwebstack.framework.core.helpers.TypeHelper.getTypeName;

import graphql.schema.DataFetcher;
import graphql.schema.idl.FieldWiringEnvironment;
import graphql.schema.idl.WiringFactory;
import java.util.List;
import java.util.Optional;
import org.dotwebstack.framework.core.OnLocalSchema;
import org.dotwebstack.framework.core.backend.validator.GraphQlValidator;
import org.dotwebstack.framework.core.model.Schema;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

@Component
@Conditional(OnLocalSchema.class)
class BackendDataFetcherWiringFactory implements WiringFactory {

  private final BackendModule<?> backendModule;

  private final BackendRequestFactory requestFactory;

  private final Schema schema;

  private final BackendExecutionStepInfo backendExecutionStepInfo;

  private final List<GraphQlValidator> graphQlValidators;

  public BackendDataFetcherWiringFactory(BackendModule<?> backendModule, BackendRequestFactory requestFactory,
      Schema schema, BackendExecutionStepInfo backendExecutionStepInfo, List<GraphQlValidator> graphQlValidators) {
    this.backendModule = backendModule;
    this.requestFactory = requestFactory;
    this.schema = schema;
    this.backendExecutionStepInfo = backendExecutionStepInfo;
    this.graphQlValidators = graphQlValidators;
  }

  @Override
  public boolean providesDataFetcher(FieldWiringEnvironment environment) {
    var typeName = getTypeName(environment.getFieldType()).orElseThrow();

    if (typeName.isEmpty()) {
      throw illegalStateException("Unknown ObjectType: %s", typeName);
    }

    if (AGGREGATE_TYPE.equals(typeName)) {
      return true;
    }

    return Optional.of(typeName)
        .flatMap(schema::getObjectType)
        .isPresent();
  }

  @Override
  public DataFetcher<?> getDataFetcher(FieldWiringEnvironment environment) {
    var typeName = getTypeName(environment.getFieldType()).orElseThrow();

    if (typeName.isEmpty()) {
      throw illegalStateException("Unknown ObjectType: %s", typeName);
    }

    // Initialize BackendDataFetcher without BackendLoader to support aliases for Aggregates.
    if (AGGREGATE_TYPE.equals(typeName)) {
      return new BackendDataFetcher(null, requestFactory, backendExecutionStepInfo, graphQlValidators);
    } else {
      var objectType = Optional.of(typeName)
          .flatMap(schema::getObjectType)
          .orElseThrow();

      var backendLoader = backendModule.getBackendLoaderFactory()
          .create(objectType);
      return new BackendDataFetcher(backendLoader, requestFactory, backendExecutionStepInfo, graphQlValidators);
    }
  }
}
