package org.dotwebstack.framework.core.query.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.dotwebstack.framework.core.config.AbstractFieldConfiguration;
import org.dotwebstack.framework.core.config.FieldConfiguration;
import org.dotwebstack.framework.core.config.TypeConfiguration;

import java.util.List;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class ObjectQuery<T extends AbstractFieldConfiguration> implements Query {
  // Brewery (keyCriteria: [identifier], scalarFields: [name, state, foundedAt], objectFields: [privateAddress, voorkomen(aparte tabel)], collectionFields: [teksten]
  private TypeConfiguration<T> typeConfiguration;

  private List<FieldConfiguration> scalarFields;

  private List<ObjectFieldConfiguration> objectFields;

  private List<FilterCriteria> filterCriteria;

  private List<SortCriteria> sortCriteria;

  // TODO: wordt nu nog niet ondersteund vanwege n+1
  // private List<CollectionQuery> collectionFields;

}
