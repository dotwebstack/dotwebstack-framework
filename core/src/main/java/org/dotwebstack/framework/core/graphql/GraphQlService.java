package org.dotwebstack.framework.core.graphql;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import java.util.concurrent.CompletableFuture;
import lombok.NonNull;

public interface GraphQlService {
  ExecutionResult execute(@NonNull ExecutionInput executionInput);

  CompletableFuture<ExecutionResult> executeAsync(@NonNull ExecutionInput executionInput);
}
