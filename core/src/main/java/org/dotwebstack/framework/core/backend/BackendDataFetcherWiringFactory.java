package org.dotwebstack.framework.core.backend;

import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.AGGREGATE_TYPE;
import static org.dotwebstack.framework.core.graphql.GraphQlConstants.CUSTOM_FIELD_VALUEFETCHER;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalStateException;
import static org.dotwebstack.framework.core.helpers.GraphQlHelper.getAdditionalData;
import static org.dotwebstack.framework.core.helpers.TypeHelper.getTypeName;

import graphql.language.TypeDefinition;
import graphql.schema.DataFetcher;
import graphql.schema.idl.FieldWiringEnvironment;
import graphql.schema.idl.WiringFactory;
import java.util.List;
import javax.annotation.Nullable;
import org.dotwebstack.framework.core.CustomValueDataFetcher;
import org.dotwebstack.framework.core.CustomValueFetcherDispatcher;
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

  private final CustomValueFetcherDispatcher customValueFetcherDispatcher;

  public BackendDataFetcherWiringFactory(BackendModule<?> backendModule, BackendRequestFactory requestFactory,
      Schema schema, BackendExecutionStepInfo backendExecutionStepInfo, List<GraphQlValidator> graphQlValidators,
      @Nullable CustomValueFetcherDispatcher customValueFetcherDispatcher) {
    this.backendModule = backendModule;
    this.requestFactory = requestFactory;
    this.schema = schema;
    this.backendExecutionStepInfo = backendExecutionStepInfo;
    this.graphQlValidators = graphQlValidators;
    this.customValueFetcherDispatcher = customValueFetcherDispatcher;
  }

  @Override
  public boolean providesDataFetcher(FieldWiringEnvironment environment) {
    var typeName = getTypeName(environment.getFieldType()).orElseThrow();

    if (typeName.isEmpty()) {
      throw illegalStateException("Unknown ObjectType: %s", typeName);
    }

    if (isCustomValueField(environment)) {
      return true;
    }

    if (isAliasedType(typeName, environment)) {
      return true;
    }

    return of(typeName).flatMap(schema::getObjectType)
        .isPresent();
  }

  @Override
  public DataFetcher<?> getDataFetcher(FieldWiringEnvironment environment) {
    var typeName = getTypeName(environment.getFieldType()).orElseThrow();

    if (typeName.isEmpty()) {
      throw illegalStateException("Unknown ObjectType: %s", typeName);
    }

    if (isCustomValueField(environment)) {
      if (customValueFetcherDispatcher == null) {
        throw illegalStateException("Missing CustomValueFetcherDispatcher bean!");
      }
      return new CustomValueDataFetcher(customValueFetcherDispatcher);
    }

    // Initialize BackendDataFetcher without BackendLoader to support aliases for Aggregates.
    if (isAliasedType(typeName, environment)) {
      return new BackendDataFetcher(null, requestFactory, backendExecutionStepInfo, graphQlValidators);
    } else {
      var objectType = of(typeName).flatMap(schema::getObjectType)
          .orElseThrow();

      var backendLoader = backendModule.getBackendLoaderFactory()
          .create(objectType);
      return new BackendDataFetcher(backendLoader, requestFactory, backendExecutionStepInfo, graphQlValidators);
    }
  }

  private boolean isCustomValueField(FieldWiringEnvironment environment) {
    return getAdditionalData(environment.getFieldDefinition(), CUSTOM_FIELD_VALUEFETCHER).isPresent();
  }

  private boolean isAliasedType(String typeName, FieldWiringEnvironment environment) {
    return AGGREGATE_TYPE.equals(typeName) || ofNullable(environment.getParentType()).map(TypeDefinition::getName)
        .filter(name -> name.equals(AGGREGATE_TYPE))
        .isPresent();
  }
}
