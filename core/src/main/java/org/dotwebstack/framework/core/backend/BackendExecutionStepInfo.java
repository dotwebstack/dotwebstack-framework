package org.dotwebstack.framework.core.backend;

import static graphql.schema.GraphQLTypeUtil.isList;
import static graphql.schema.GraphQLTypeUtil.unwrapNonNull;

import graphql.execution.ExecutionStepInfo;
import graphql.schema.DataFetchingEnvironment;
import org.dotwebstack.framework.core.OnLocalSchema;
import org.dotwebstack.framework.core.graphql.GraphQlConstants;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

@Component
@Conditional(OnLocalSchema.class)
public class BackendExecutionStepInfo {

  public ExecutionStepInfo getExecutionStepInfo(DataFetchingEnvironment environment) {
    ExecutionStepInfo executionStepInfo;

    var type = unwrapNonNull(environment.getFieldType());

    var isList = isList(type);

    var usePaging = environment.getFieldDefinition()
        .getDefinition()
        .getAdditionalData()
        .containsKey(GraphQlConstants.IS_PAGING_NODE);

    if (usePaging && isList) {
      executionStepInfo = environment.getExecutionStepInfo()
          .getParent();
    } else {
      executionStepInfo = environment.getExecutionStepInfo();
    }
    return executionStepInfo;
  }
}
