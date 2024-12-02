package org.dotwebstack.framework.core.query.model;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Getter
@Builder
public class CollectionRequest implements Request {

  @NonNull
  private final ObjectRequest objectRequest;

  @Builder.Default
  private final List<SortCriteria> sortCriterias = List.of();
}
