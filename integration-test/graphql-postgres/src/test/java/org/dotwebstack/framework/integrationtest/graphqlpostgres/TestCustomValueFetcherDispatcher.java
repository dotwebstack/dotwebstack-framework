package org.dotwebstack.framework.integrationtest.graphqlpostgres;

import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.dotwebstack.framework.core.CustomValueFetcherDispatcher;
import org.springframework.stereotype.Component;

@Component
public class TestCustomValueFetcherDispatcher implements CustomValueFetcherDispatcher {

  private static final String SHORT_NAME_VALUEFETCHER = "shortname-valuefetcher";

  @Override
  public Object fetch(String customValueFetcher, Map<String, Object> sourceFields) {
    var fullName = (String) sourceFields.get("name");

    return StringUtils.substring(fullName, 0, Math.min(3, fullName.length()));
  }

  @Override
  public Class<?> getResultType(String customValueFetcher) {
    return String.class;
  }

  @Override
  public Set<String> getSourceFieldNames(String customValueFetcher) {
    return Set.of("name");
  }

  @Override
  public boolean supports(String customValueFetcher) {
    return SHORT_NAME_VALUEFETCHER.equals(customValueFetcher);
  }
}
