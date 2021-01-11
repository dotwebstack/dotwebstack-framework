package org.dotwebstack.framework.core.datafetchers.keys;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class FilterKey implements Key {

  private final List<String> path;

  private final Object value;
}
