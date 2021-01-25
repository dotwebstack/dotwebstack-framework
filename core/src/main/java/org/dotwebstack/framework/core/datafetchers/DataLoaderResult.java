package org.dotwebstack.framework.core.datafetchers;

import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DataLoaderResult {

  private final Map<String, Object> data;
}
