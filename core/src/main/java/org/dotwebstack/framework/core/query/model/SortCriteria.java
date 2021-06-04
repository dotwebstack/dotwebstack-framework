package org.dotwebstack.framework.core.query.model;

import lombok.Builder;
import lombok.Data;
import org.dotwebstack.framework.core.query.model.filter.FieldPath;

@Data
@Builder
public class SortCriteria {

  private final FieldPath fieldPath;

  private final SortDirection direction;
}
