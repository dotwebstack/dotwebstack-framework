package org.dotwebstack.framework.core.query.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import org.dotwebstack.framework.core.model.ObjectType;

@Getter
@Builder
public class ObjectRequest implements Request {

  private final ObjectType<?> objectType;

  @Builder.Default
  private final Collection<FieldRequest> scalarFields = new ArrayList<>();

  @Builder.Default
  private final Map<FieldRequest, ObjectRequest> objectFields = new HashMap<>();

  @Builder.Default
  private final Map<FieldRequest, CollectionRequest> objectListFields = new HashMap<>();

  @Builder.Default
  private final List<AggregateObjectRequest> aggregateObjectFields = new ArrayList<>();

  @Builder.Default
  private final List<KeyCriteria> keyCriteria = new ArrayList<>();

  // @Builder.Default
  // protected final Map<FieldRequest, Collection<FieldRequest>> nestedObjectFields = new HashMap<>();

  private final ContextCriteria contextCriteria;
}
