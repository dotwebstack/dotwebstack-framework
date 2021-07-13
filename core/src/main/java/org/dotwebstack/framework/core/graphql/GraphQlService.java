package org.dotwebstack.framework.core.graphql;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import java.util.concurrent.CompletableFuture;

public interface GraphQlService {
  ExecutionResult execute(ExecutionInput executionInput);

  CompletableFuture<ExecutionResult> executeAsync(ExecutionInput executionInput);
}
