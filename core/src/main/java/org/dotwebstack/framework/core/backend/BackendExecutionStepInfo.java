package org.dotwebstack.framework.core.backend;

import static graphql.schema.GraphQLTypeUtil.isList;
import static graphql.schema.GraphQLTypeUtil.unwrapNonNull;

import graphql.execution.ExecutionStepInfo;
import graphql.schema.DataFetchingEnvironment;
import org.dotwebstack.framework.core.OnLocalSchema;
import org.dotwebstack.framework.core.model.Schema;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

@Component
@Conditional(OnLocalSchema.class)
public class BackendExecutionStepInfo {

  private final boolean usePaging;

  public BackendExecutionStepInfo(Schema schema) {
    usePaging = schema.usePaging();
  }

  public ExecutionStepInfo getExecutionStepInfo(DataFetchingEnvironment environment) {
    ExecutionStepInfo executionStepInfo;

    var isList = isList(unwrapNonNull(environment.getFieldType()));

    if (usePaging && isList) {
      executionStepInfo = environment.getExecutionStepInfo()
          .getParent();
    } else {
      executionStepInfo = environment.getExecutionStepInfo();
    }
    return executionStepInfo;
  }
}
