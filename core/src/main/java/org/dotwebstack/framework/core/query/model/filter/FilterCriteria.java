package org.dotwebstack.framework.core.query.model.filter;

import org.apache.commons.lang3.StringUtils;
import org.dotwebstack.framework.core.config.FieldConfiguration;

public interface FilterCriteria {

  default boolean isCompositeFilter() {
    return getFieldName().contains(".");
  }

  // TODO: implement getFieldName per subtype
  default String getFieldName() {
    return "visitAddress.city";
  }

  FieldConfiguration getField();

  default String[] getFilterFields() {
    return StringUtils.split(getFieldName(), '.');
  }

}
