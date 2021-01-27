package org.dotwebstack.framework.core.datafetchers.filters;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.unsupportedOperationException;

import java.util.List;

public interface Filter {

  default List<Filter> flatten() {
    if (this instanceof CompositeFilter) {
      return ((CompositeFilter) this).getFilters();
    }

    if (this instanceof FieldFilter) {
      return List.of(this);
    }

    throw unsupportedOperationException("Unsupported filter of type '{}'", this.getClass()
        .getSimpleName());
  }

  static List<Filter> flatten(Filter filter) {
    if (filter != null) {
      return filter.flatten();
    }

    return List.of();
  }

  static Filter wrap(List<Filter> filters) {
    if (filters.size() > 1) {
      return CompositeFilter.builder()
          .filters(filters)
          .build();
    }

    return filters.get(0);
  }
}
