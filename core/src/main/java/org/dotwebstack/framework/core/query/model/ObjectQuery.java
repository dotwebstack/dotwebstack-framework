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

  // Brewery (keyCriteria: [identifier], scalarFields: [name, state, foundedAt], objectFields:
  // [privateAddress, voorkomen(aparte tabel)], collectionFields: [teksten]
  private TypeConfiguration<?> typeConfiguration;

  private List<FieldConfiguration> scalarFields;

  private List<ObjectFieldConfiguration> objectFields;

  private List<NestedObjectFieldConfiguration> nestedObjectFields;

  private List<AggregateObjectFieldConfiguration> aggregateObjectFields;

  private List<FilterCriteria> filterCriteria;

  // je wilt naar mijn idee enkel sorteren bij een CollectionQuery
  // private List<SortCriteria> sortCriteria;

  // TODO: wordt nu nog niet ondersteund vanwege n+1
  // TODO: of moet dir zijn: List<CollectionQuery> collectionFields
  private List<ObjectFieldConfiguration> collectionObjectFields;

}
