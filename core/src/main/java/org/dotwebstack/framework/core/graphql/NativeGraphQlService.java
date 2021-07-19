package org.dotwebstack.framework.core.graphql;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import java.util.concurrent.CompletableFuture;
import lombok.NonNull;
import org.dotwebstack.framework.core.condition.GraphQlNativeEnabled;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

@Conditional(GraphQlNativeEnabled.class)
@Service
public class NativeGraphQlService implements GraphQlService {
  private final GraphQL graphQL;

  public NativeGraphQlService(@NonNull GraphQL graphQL) {
    this.graphQL = graphQL;
  }

  @Override
  public ExecutionResult execute(@NonNull ExecutionInput executionInput) {
    return graphQL.execute(executionInput);
  }

  @Override
  public CompletableFuture<ExecutionResult> executeAsync(@NonNull ExecutionInput executionInput) {
    return graphQL.executeAsync(executionInput);
  }
}
