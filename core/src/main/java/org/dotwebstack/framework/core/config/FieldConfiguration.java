package org.dotwebstack.framework.core.config;

import java.util.Map;
import org.dotwebstack.framework.core.datafetchers.filters.Filter;

public interface FieldConfiguration {

  String getMappedBy();

  Filter createMappedByFilter(Map<String, Object> referenceData);
}
