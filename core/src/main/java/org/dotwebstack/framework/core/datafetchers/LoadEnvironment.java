package org.dotwebstack.framework.core.datafetchers;

import graphql.execution.ExecutionStepInfo;
import graphql.schema.GraphQLObjectType;
import graphql.schema.SelectedField;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import org.dotwebstack.framework.core.config.TypeConfiguration;

@Builder
@Getter
public final class LoadEnvironment {

  @NonNull
  private final TypeConfiguration<?> typeConfiguration;

  @NonNull
  private final ExecutionStepInfo executionStepInfo;

  private final GraphQLObjectType objectType;

  @NonNull
  private final List<SelectedField> selectedFields;

  private final String queryName;
}
