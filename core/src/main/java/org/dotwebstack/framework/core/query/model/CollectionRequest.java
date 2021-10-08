package org.dotwebstack.framework.core.query.model;

import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import org.dotwebstack.framework.core.backend.filter.BackendFilterCriteria;

@Builder
@Data
public class CollectionRequest implements Request {

  @NonNull
  private final ObjectRequest objectRequest;

  @Builder.Default
  private final List<SortCriteria> sortCriterias = List.of();

  @Builder.Default
  private final List<BackendFilterCriteria> backendFilterCriteria = List.of();

  @Deprecated
  private final PagingCriteria pagingCriteria;

  public boolean hasSortCriterias() {
    return !sortCriterias.isEmpty();
  }
}
