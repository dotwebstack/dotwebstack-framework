package org.dotwebstack.framework.core.datafetchers.paging;

import static org.dotwebstack.framework.core.datafetchers.paging.PagingConstants.FIRST_MAX_VALUE;
import static org.dotwebstack.framework.core.datafetchers.paging.PagingConstants.OFFSET_MAX_VALUE;

import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import java.util.Map;
import java.util.Optional;

public class ConnectionDataFetcher implements DataFetcher<Object> {

  @Override
  public Object get(DataFetchingEnvironment environment) throws Exception {
    int firstArgumentValue = (int) Optional.ofNullable(environment.getArguments()
        .get(PagingConstants.FIRST_ARGUMENT_NAME))
        .orElseThrow();
    int offsetArgumentValue = (int) Optional.ofNullable(environment.getArguments()
        .get(PagingConstants.OFFSET_ARGUMENT_NAME))
        .orElseThrow();

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
