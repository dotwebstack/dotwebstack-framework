package org.dotwebstack.framework.core.query.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Getter
@Builder
public class CollectionBatchRequest {

  @NonNull
  private final CollectionRequest collectionRequest;

  @NonNull
  private final JoinCriteria joinCriteria;
}
