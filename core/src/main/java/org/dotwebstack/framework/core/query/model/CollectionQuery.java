package org.dotwebstack.framework.core.query.model;

import java.util.List;
import org.dotwebstack.framework.core.config.FieldConfiguration;
import org.dotwebstack.framework.core.config.TypeConfiguration;

public class CollectionQuery implements Query {
  // beers (scalarFields: [name, beerType], objectFields: [brewery], collectionFields: [ingredients]
  private TypeConfiguration<?> typeConfiguration;

  private List<FieldConfiguration> scalarFields;

  private List<ObjectFieldConfiguration> objectFields;

  private List<FilterCriteria> filterCriteria;

  private List<SortCriteria> sortCriteria;

  private PagingCriteria pagingCriteria;

  // TODO: wordt nu nog niet ondersteund vanwege n+1
  // private List<CollectionFieldConfiguration> collectionFields;

}
