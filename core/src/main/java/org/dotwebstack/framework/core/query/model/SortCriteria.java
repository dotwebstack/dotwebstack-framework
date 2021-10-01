package org.dotwebstack.framework.core.query.model;

import java.util.List;
import lombok.Builder;
import lombok.Data;
import org.dotwebstack.framework.core.model.ObjectField;
import org.dotwebstack.framework.core.query.model.filter.FieldPath;

@Data
@Builder
public class SortCriteria {

  private final FieldPath fieldPath;

  // TODO rename to fieldPath
  private final List<ObjectField> fields;

  private final SortDirection direction;
}
