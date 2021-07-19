package org.dotwebstack.framework.core.graphql;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import graphql.ExecutionInput;
import graphql.GraphQL;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NativeGraphQlServiceTest {
  @Mock
  private GraphQL graphQL;

  @InjectMocks
  private NativeGraphQlService service;

  @Test
  void execute() {
    ExecutionInput input = mock(ExecutionInput.class);

    service.execute(input);

    verify(graphQL).execute(input);
  }

  @Test
  void executeAsync() {
    ExecutionInput input = mock(ExecutionInput.class);

    service.executeAsync(input);

    verify(graphQL).executeAsync(input);
  }
}
