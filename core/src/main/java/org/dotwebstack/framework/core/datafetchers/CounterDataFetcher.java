package org.dotwebstack.framework.core.datafetchers;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import java.util.Map;
import org.dotwebstack.framework.core.backend.BackendExecutionStepInfo;
import org.dotwebstack.framework.core.backend.BackendLoader;
import org.dotwebstack.framework.core.backend.BackendRequestFactory;
import reactor.core.publisher.Mono;

public class CounterDataFetcher implements DataFetcher<Object> {


  @Override
  public Object get(DataFetchingEnvironment environment) throws Exception {
    

    return Mono.just(Map.of("total", 5))
        .toFuture();
  }
}
