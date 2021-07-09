package org.dotwebstack.framework.core.graphql;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import java.util.concurrent.CompletableFuture;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

@ConditionalOnMissingBean(NativeGraphqlService.class)
@Service
public class ProxyGraphqlService implements GraphqlService {

  @Override
  public ExecutionResult execute(ExecutionInput executionInput) {
    // TODO:post query to proxy
    return null;
  }

  @Override
  public CompletableFuture<ExecutionResult> executeAsync(ExecutionInput executionInput) {
    // TODO: post query to proxy async
    return null;
  }
}
