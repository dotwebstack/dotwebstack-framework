package org.dotwebstack.framework.core.query.model;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class CollectionQuery implements Query {

  private ObjectQuery objectQuery;

  private List<SortCriteria> sortCriteria;

  private PagingCriteria pagingCriteria;
}
