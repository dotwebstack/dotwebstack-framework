package org.dotwebstack.framework.core.datafetchers.filter;

import java.util.Map;
import lombok.NonNull;

public interface FilterConfigurer {
  void configureFieldFilterMapping(@NonNull Map<String, String> fieldFilterMap);
}
