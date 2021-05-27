package org.dotwebstack.framework.core.query.model;

import java.util.List;
import lombok.Builder;
import lombok.Data;
import org.dotwebstack.framework.core.config.FieldConfiguration;
import org.dotwebstack.framework.core.config.TypeConfiguration;

@Builder
@Data
public class ObjectQuery implements Query {
  // @NonNull
  private final TypeConfiguration<?> typeConfiguration;

  private final List<FieldConfiguration> scalarFields;

  @Builder.Default
  private final List<KeyCriteria> keyCriteria = List.of();

  @Builder.Default
  private final List<ObjectFieldConfiguration> objectFields = List.of();

  @Builder.Default
  private final List<NestedObjectFieldConfiguration> nestedObjectFields = List.of();

  @Builder.Default
  private final List<AggregateObjectFieldConfiguration> aggregateObjectFields = List.of();

  @Builder.Default
  private final List<ObjectFieldConfiguration> collectionObjectFields = List.of();
}
