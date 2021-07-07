package org.dotwebstack.framework.core.datafetchers.paging;

import static org.dotwebstack.framework.core.datafetchers.paging.PagingConstants.FIRST_ARGUMENT_NAME;
import static org.dotwebstack.framework.core.datafetchers.paging.PagingConstants.FIRST_MAX_VALUE;
import static org.dotwebstack.framework.core.datafetchers.paging.PagingConstants.OFFSET_ARGUMENT_NAME;
import static org.dotwebstack.framework.core.datafetchers.paging.PagingConstants.OFFSET_MAX_VALUE;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;

import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import java.util.Map;
import java.util.Optional;

public class ConnectionDataFetcher implements DataFetcher<Object> {

  @Override
  public Object get(DataFetchingEnvironment environment) throws Exception {
    int firstArgumentValue = getFirstArgumentValue(environment);
    int offsetArgumentValue = getOffsetArgumentValue(environment);

    validateArgumentValues(firstArgumentValue, offsetArgumentValue);

    PagingDataFetcherContext localContext = PagingDataFetcherContext.builder()
        .first(firstArgumentValue)
        .offset(offsetArgumentValue)
        .parentLocalContext(environment.getLocalContext())
        .parentSource(environment.getSource())
        .build();

    return DataFetcherResult.newResult()
        .data(Map.of(PagingConstants.OFFSET_ARGUMENT_NAME, offsetArgumentValue))
        .localContext(localContext)
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
        .orElse(graphQlArgument.getDefaultValue());
  }

  private void validateArgumentValues(int firstArgumentValue, int offsetArgumentValue) {
    if (firstArgumentValue > FIRST_MAX_VALUE) {
      throw illegalArgumentException("Argument first can't be bigger then {}.", FIRST_MAX_VALUE);
    }
    if (offsetArgumentValue > OFFSET_MAX_VALUE) {
      throw illegalArgumentException("Argument offset can't be bigger then {}.", OFFSET_MAX_VALUE);
    }
  }
}
