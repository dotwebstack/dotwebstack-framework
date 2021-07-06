package org.dotwebstack.framework.core.datafetchers.paging;

import java.util.Map;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.dotwebstack.framework.core.datafetchers.LocalDataFetcherContext;

@Getter
@Builder
@EqualsAndHashCode
public class PagingDataFetcherContext {

  private final int first;

  private final int offset;

  private final LocalDataFetcherContext parentLocalContext;

  private final Map<String, Object> parentSource;
}
