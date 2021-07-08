package org.dotwebstack.framework.core.query.model.filter;

import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode
@Builder
public class AndFilterCriteria implements FilterCriteria {
  @Getter
  private final List<FilterCriteria> filterCriterias;

  @Override
  public List<FieldPath> getFieldPaths() {
    return filterCriterias.stream()
        .flatMap(filterCriteria -> filterCriteria.getFieldPaths()
            .stream())
        .collect(Collectors.toList());
  }

}
