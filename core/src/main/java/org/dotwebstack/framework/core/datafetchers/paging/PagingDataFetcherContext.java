package org.dotwebstack.framework.core.datafetchers.paging;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PagingDataFetcherContext {

  private final int first;

  private final int offset;
}
