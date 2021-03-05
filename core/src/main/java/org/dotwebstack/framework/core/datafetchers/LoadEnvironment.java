package org.dotwebstack.framework.core.datafetchers;

import graphql.execution.ExecutionStepInfo;
import graphql.schema.DataFetchingFieldSelectionSet;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Builder
@Getter
public final class LoadEnvironment {

  @NonNull
  private final ExecutionStepInfo executionStepInfo;

  @NonNull
  private final DataFetchingFieldSelectionSet selectionSet;

  private final String queryName;

  private final boolean subscription;

  private final Map<String, Object> source;
}
