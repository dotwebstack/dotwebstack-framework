package org.dotwebstack.framework.core.query.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.List;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class ObjectQuery implements Query {
  // Brewery (keyCriteria: [identifier], scalarFields: [name, state, foundedAt], objectFields: [privateAddress, voorkomen(aparte tabel)], collectionFields: [teksten]
  // String zou FieldConfiguration kunnen zijn?
  private List<String> scalarFields;
  private List<ObjectQuery> objectFields;

  private List<FilterCriteria> filterCriteria;
  // TODO: kan je sorteren op een enums
  private List<SortCriteria> sortCriteria;

  // TODO: wordt nu nog niet ondersteund vanwege n+1
  // private List<CollectionQuery> collectionFields;

}
