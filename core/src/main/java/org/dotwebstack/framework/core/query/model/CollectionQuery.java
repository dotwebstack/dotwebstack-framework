package org.dotwebstack.framework.core.query.model;

import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import org.dotwebstack.framework.core.query.model.filter.FilterCriteria;

@Builder
@Data
public class CollectionQuery implements Query {

  @NonNull
  private final ObjectQuery objectQuery;

  @Builder.Default
  private final List<SortCriteria> sortCriteria = List.of();

  @Builder.Default
  private final List<FilterCriteria> filterCriterias = List.of();

  private final PagingCriteria pagingCriteria;
}
