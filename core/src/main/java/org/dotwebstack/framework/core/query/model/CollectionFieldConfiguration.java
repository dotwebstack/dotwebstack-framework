package org.dotwebstack.framework.core.query.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class CollectionFieldConfiguration {
  private final String name;
  private final CollectionQuery collectionQuery;
}
