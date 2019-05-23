package org.dotwebstack.framework.core.datafetchers;

import com.google.common.collect.ImmutableMap;
import graphql.schema.DataFetchingEnvironment;
import java.util.Map;
import org.dotwebstack.framework.core.Constants;
import org.springframework.stereotype.Component;

@Component
public class IntegrationTestDataFetcher extends SourceDataFetcher {

  private static final Map<String, Object> DATA = ImmutableMap.of(Constants.BREWERY_IDENTIFIER_FIELD,
      Constants.BREWERY_IDENTIFIER_EXAMPLE_1, Constants.BREWERY_NAME_FIELD, Constants.BREWERY_NAME_EXAMPLE_1,
      Constants.BREWERY_FOUNDED_FIELD, Constants.BREWERY_FOUNDED_EXAMPLE_1, Constants.BREWERY_FOUNDED_AT_YEAR_FIELD,
      Constants.BREWERY_FOUNDED_EXAMPLE_1);

  @Override
  public boolean supports(DataFetchingEnvironment environment) {
    return true;
  }

  @Override
  public Object get(DataFetchingEnvironment environment) {
    if (DATA.containsKey(environment.getField()
        .getName())) {
      return DATA.get(environment.getField()
          .getName());
    }

    return new Object();
  }
}
