package org.dotwebstack.framework.core.datafetchers;

import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import org.dotwebstack.framework.core.datafetchers.filters.Filter;

@Builder
@Getter
public class LocalDataFetcherContext {

  private final Map<String, Filter> fieldFilters;
}
