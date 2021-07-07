package org.dotwebstack.framework.core.query.model.origin;

import java.util.Map;
import org.dotwebstack.framework.core.query.model.SortCriteria;
import org.dotwebstack.framework.core.query.model.filter.FilterCriteria;

public interface Origin {

  static Requested requested() {
    return new Requested();
  }

  static Sorting sorting(SortCriteria sortCriteria, Map<String, String> fieldPathAliasMap) {
    return new Sorting(sortCriteria, fieldPathAliasMap);
  }

  static Filtering filtering(FilterCriteria filterCriteria) {
    return new Filtering(filterCriteria);
  }
}
