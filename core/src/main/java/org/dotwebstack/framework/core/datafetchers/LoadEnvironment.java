package org.dotwebstack.framework.core.datafetchers;

import graphql.execution.ExecutionStepInfo;
import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.GraphQLObjectType;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Builder
@Getter
public final class LoadEnvironment {

  @NonNull
  private final ExecutionStepInfo executionStepInfo;

  private final GraphQLObjectType objectType;

  @NonNull
  private final DataFetchingFieldSelectionSet selectionSet;

  private final String queryName;

  private Map<String, Object> source;
}
