package org.dotwebstack.framework.core.datafetchers.paging;

import static org.dotwebstack.framework.core.datafetchers.paging.PagingConstants.FIRST_ARGUMENT_NAME;
import static org.dotwebstack.framework.core.datafetchers.paging.PagingConstants.FIRST_MAX_VALUE;
import static org.dotwebstack.framework.core.datafetchers.paging.PagingConstants.OFFSET_ARGUMENT_NAME;
import static org.dotwebstack.framework.core.datafetchers.paging.PagingConstants.OFFSET_MAX_VALUE;

import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import java.util.Map;
import java.util.Optional;

public class ConnectionDataFetcher implements DataFetcher<Object> {

  @Override
  public Object get(DataFetchingEnvironment environment) throws Exception {
    var fieldDefinition = environment.getFieldDefinition();

    var firstArgument = fieldDefinition.getArgument(FIRST_ARGUMENT_NAME);
    var offsetArgument = fieldDefinition.getArgument(OFFSET_ARGUMENT_NAME);

    int firstArgumentValue = (int) Optional.of(firstArgument)
        .map(argument -> environment.getArguments()
            .get(argument.getName()))
        .orElse(firstArgument.getDefaultValue());

    int offsetArgumentValue = (int) Optional.of(offsetArgument)
        .map(argument -> environment.getArguments()
            .get(argument.getName()))
        .orElse(offsetArgument.getDefaultValue());

    // TODO: willen in deze situatie niet liever een foutmelding gooien?
    int first = Math.min(firstArgumentValue, FIRST_MAX_VALUE);
    int offset = Math.min(offsetArgumentValue, OFFSET_MAX_VALUE);

    PagingDataFetcherContext localContext = PagingDataFetcherContext.builder()
        .first(first)
        .offset(offset)
        .build();

    return DataFetcherResult.newResult()
        .data(Map.of(PagingConstants.OFFSET_ARGUMENT_NAME, offset))
        .localContext(localContext)
        .build();
  }
}
