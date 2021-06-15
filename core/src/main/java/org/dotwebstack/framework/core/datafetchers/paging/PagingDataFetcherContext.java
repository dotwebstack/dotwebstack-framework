package org.dotwebstack.framework.core.datafetchers.paging;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@Builder
@EqualsAndHashCode
public class PagingDataFetcherContext {

  private final int first;

  private final int offset;
}
