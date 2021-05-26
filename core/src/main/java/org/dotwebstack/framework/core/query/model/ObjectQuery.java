package org.dotwebstack.framework.core.query.model;

import java.util.List;
import lombok.Builder;
import lombok.Data;
import org.dotwebstack.framework.core.config.FieldConfiguration;
import org.dotwebstack.framework.core.config.TypeConfiguration;

@Builder
@Data
public class ObjectQuery implements Query {

  private List<KeyCriteria> keyCriteria;

  private TypeConfiguration<?> typeConfiguration;

  private List<FieldConfiguration> scalarFields;

  private List<ObjectFieldConfiguration> objectFields;

  private List<NestedObjectFieldConfiguration> nestedObjectFields;

  private List<AggregateObjectFieldConfiguration> aggregateObjectFields;
}
