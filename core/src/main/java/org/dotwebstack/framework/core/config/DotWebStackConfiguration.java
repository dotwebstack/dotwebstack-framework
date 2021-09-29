package org.dotwebstack.framework.core.config;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalStateException;

import graphql.execution.ExecutionStepInfo;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLOutputType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Data;
import org.dotwebstack.framework.core.datafetchers.LoadEnvironment;
import org.dotwebstack.framework.core.helpers.TypeHelper;

@Data
public class DotWebStackConfiguration<T extends AbstractTypeConfiguration<?>> {

  @Valid
  private SettingsConfiguration settings;

  @NotNull
  private List<Feature> features = List.of();

  @Valid
  private ContextConfiguration context;

  @Valid
  private Map<String, T> objectTypes;

  @Valid
  private Map<String, QueryConfiguration> queries = new HashMap<>();

  @Valid
  private Map<String, SubscriptionConfiguration> subscriptions = new HashMap<>();

  @Valid
  private Map<String, EnumerationConfiguration> enumerations = new HashMap<>();

  public void setObjectTypes(Map<String, T> objectTypes) {
    objectTypes.forEach((key, value) -> value.setName(key));
    this.objectTypes = objectTypes;
  }

  public T getTypeConfiguration(LoadEnvironment loadEnvironment) {
    return getTypeConfiguration(loadEnvironment.getExecutionStepInfo());
  }

  public T getTypeConfiguration(ExecutionStepInfo executionStepInfo) {
    return getTypeConfiguration(executionStepInfo.getFieldDefinition());
  }

  public T getTypeConfiguration(GraphQLFieldDefinition fieldDefinition) {
    return getTypeConfiguration(fieldDefinition.getType());
  }

  public T getTypeConfiguration(GraphQLOutputType outputType) {
    return getTypeConfiguration(TypeHelper.getTypeName(outputType));
  }

  public T getTypeConfiguration(String typeName) {
    return Optional.ofNullable(objectTypes.get(typeName))
        .orElseThrow(() -> illegalStateException("Unknown type configuration: {}", typeName));
  }

  public boolean isFeatureEnabled(Feature feature) {
    return features.contains(feature);
  }
}
