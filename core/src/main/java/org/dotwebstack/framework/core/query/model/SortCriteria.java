package org.dotwebstack.framework.core.query.model;

import java.util.List;
import lombok.Builder;
import lombok.Data;
import org.dotwebstack.framework.core.model.ObjectField;

@Data
@Builder
public class SortCriteria {

  private final List<ObjectField> fieldPath;

  private final SortDirection direction;
}
