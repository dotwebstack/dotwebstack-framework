package org.dotwebstack.framework.core.datafetchers.paging;

import static org.dotwebstack.framework.core.backend.BackendConstants.PAGING_KEY_PREFIX;
import static org.dotwebstack.framework.core.datafetchers.paging.PagingConstants.FIRST_ARGUMENT_NAME;
import static org.dotwebstack.framework.core.datafetchers.paging.PagingConstants.FIRST_MAX_VALUE;
import static org.dotwebstack.framework.core.datafetchers.paging.PagingConstants.OFFSET_ARGUMENT_NAME;
import static org.dotwebstack.framework.core.datafetchers.paging.PagingConstants.OFFSET_MAX_VALUE;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalStateException;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.requestValidationException;

import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ConnectionDataFetcher implements DataFetcher<Object> {

  @Override
  public Object get(DataFetchingEnvironment environment) {
    int firstArgumentValue = getFirstArgumentValue(environment);
    int offsetArgumentValue = getOffsetArgumentValue(environment);

    validateArgumentValues(firstArgumentValue, offsetArgumentValue);

    Map<String, Object> data = new HashMap<>();
    data.put(PAGING_KEY_PREFIX.concat(OFFSET_ARGUMENT_NAME), offsetArgumentValue);
    data.put(PAGING_KEY_PREFIX.concat(FIRST_ARGUMENT_NAME), firstArgumentValue);
    data.put(PagingConstants.OFFSET_ARGUMENT_NAME, offsetArgumentValue);

    if (environment.getSource() != null) {
      data.putAll(environment.getSource());
    }

    return DataFetcherResult.newResult()
        .data(data)
        .build();
  }

  private int getFirstArgumentValue(DataFetchingEnvironment environment) {
    var fieldDefinition = environment.getFieldDefinition();
    var firstArgument = fieldDefinition.getArgument(FIRST_ARGUMENT_NAME);

    return getArgumentValue(environment, firstArgument);
  }

  private int getOffsetArgumentValue(DataFetchingEnvironment environment) {
    var fieldDefinition = environment.getFieldDefinition();
    var offsetArgument = fieldDefinition.getArgument(OFFSET_ARGUMENT_NAME);

    return getArgumentValue(environment, offsetArgument);
  }

  private int getArgumentValue(DataFetchingEnvironment environment, GraphQLArgument graphQlArgument) {
    return (int) Optional.of(graphQlArgument)
        .map(argument -> environment.getArguments()
            .get(argument.getName()))
        .or(() -> Optional.ofNullable(graphQlArgument.getArgumentDefaultValue()
            .getValue()))
        .orElseThrow(() -> illegalStateException("No argument value found for {}.", graphQlArgument.getName()));
  }

  private void validateArgumentValues(int firstArgumentValue, int offsetArgumentValue) {
    if (firstArgumentValue > FIRST_MAX_VALUE) {
      throw requestValidationException("Argument 'first' is not allowed to be higher than {}.", FIRST_MAX_VALUE);
    }
    if (offsetArgumentValue > OFFSET_MAX_VALUE) {
      throw requestValidationException("Argument 'offset' is not allowed to be higher than {}.", OFFSET_MAX_VALUE);
    }
  }
}
