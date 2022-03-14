package org.dotwebstack.framework.core;

import java.util.Map;
import java.util.Set;

public interface CustomValueFetcherDispatcher {

  Object fetch(String customValueFetcher, Map<String, Object> sourceFields);

  Class<?> getResultType(String customValueFetcher);

  Set<String> getSourceFieldNames(String customValueFetcher);

  boolean supports(String customValueFetcher);
}
