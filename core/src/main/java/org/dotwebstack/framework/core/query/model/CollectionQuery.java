package org.dotwebstack.framework.core.query.model;

import java.util.List;

public class CollectionQuery implements Query {
  // beers (scalarFields: [name, beerType], objectFields: [brewery], collectionFields: [ingredients]

  // String zou FieldConfiguration kunnen zijn?
  private List<String> scalarFields;
  private List<ObjectQuery> objectFields;

  private List<FilterCriteria> filterCriteria;
  private List<SortCriteria> sortCriteria;
  private PagingCriteria pagingCriteria;

  private List<CollectionQuery> collectionFields;

}
