package org.dotwebstack.framework.core.datafetchers.filter;

import java.util.Map;
import lombok.NonNull;
import org.dotwebstack.framework.core.GraphqlConfigurer;

public interface FilterConfigurer extends GraphqlConfigurer {
  void configureFieldFilterMapping(@NonNull Map<String, String> fieldFilterMap);
}
