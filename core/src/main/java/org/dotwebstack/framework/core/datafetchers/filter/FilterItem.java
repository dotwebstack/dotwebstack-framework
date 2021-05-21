package org.dotwebstack.framework.core.datafetchers.filter;

import java.util.List;
import lombok.Builder;
import lombok.Data;
import org.dotwebstack.framework.core.config.FieldConfiguration;

@Data
@Builder
class FilterItem {
  private FieldConfiguration fieldConfiguration;

  private FilterOperator operator;

  private Object value;

  private List<FilterItem> children;
}
