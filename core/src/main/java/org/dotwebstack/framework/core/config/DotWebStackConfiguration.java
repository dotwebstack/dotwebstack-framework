package org.dotwebstack.framework.core.config;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalStateException;

import graphql.execution.ExecutionStepInfo;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLOutputType;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Data;
import org.dotwebstack.framework.core.datafetchers.LoadEnvironment;
import org.dotwebstack.framework.core.helpers.TypeHelper;

@Data
public class DotWebStackConfiguration {

  @NotNull
  @Valid
  private Map<String, AbstractTypeConfiguration<?>> objectTypes;

  @Valid
  private Map<String, QueryConfiguration> queries = new HashMap<>();

  @Valid
  private Map<String, SubscriptionConfiguration> subscriptions = new HashMap<>();

  @Valid
  private Map<String, EnumerationConfiguration> enumerations = new HashMap<>();

  public <T extends AbstractTypeConfiguration<?>> T getTypeConfiguration(LoadEnvironment loadEnvironment) {
    return getTypeConfiguration(loadEnvironment.getExecutionStepInfo());
  }

  public <T extends AbstractTypeConfiguration<?>> T getTypeConfiguration(ExecutionStepInfo executionStepInfo) {
    return getTypeConfiguration(executionStepInfo.getFieldDefinition());
  }

  public <T extends AbstractTypeConfiguration<?>> T getTypeConfiguration(GraphQLFieldDefinition fieldDefinition) {
    return getTypeConfiguration(fieldDefinition.getType());
  }

  @SuppressWarnings("unchecked")
  public <T extends AbstractTypeConfiguration<?>> T getTypeConfiguration(GraphQLOutputType outputType) {
    return getTypeConfiguration(TypeHelper.getTypeName(outputType));
  }

  @SuppressWarnings("unchecked")
  public <T extends AbstractTypeConfiguration<?>> T getTypeConfiguration(String typeName) {
    return Optional.ofNullable(objectTypes.get(typeName))
        .map(type -> {
          type.setName(typeName);
          return type;
        })
        .map(type -> (T) type)
        .orElseThrow(() -> illegalStateException("Unknown type configuration: {}", typeName));
  }
}
