package org.dotwebstack.framework.core.query.model;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import org.dotwebstack.framework.core.backend.filter.FilterCriteria;

@Getter
@Builder
public class CollectionRequest implements Request {

  @NonNull
  private final ObjectRequest objectRequest;

  @Builder.Default
  private final List<SortCriteria> sortCriterias = List.of();

  @Builder.Default
  private final List<FilterCriteria> filterCriterias = List.of();
}
