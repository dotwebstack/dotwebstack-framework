package org.dotwebstack.framework.core.config;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalStateException;

import graphql.execution.ExecutionStepInfo;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLOutputType;
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
  private Map<String, AbstractTypeConfiguration<?>> typeMapping;

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
    return Optional.ofNullable(typeMapping.get(TypeHelper.getTypeName(outputType)))
        .map(type -> (T) type)
        .orElseThrow(() -> illegalStateException(""));
  }
}
