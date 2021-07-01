package org.dotwebstack.framework.core.query.model.origin;

import org.dotwebstack.framework.core.query.model.filter.FilterCriteria;

public interface Origin {

  static Requested requested() {
    return new Requested();
  }

  static Sorting sorting() {
    return new Sorting();
  }

  static Filtering filtering(FilterCriteria filterCriteria) {
    return new Filtering(filterCriteria);
  }
}
