package org.dotwebstack.framework.core.datafetchers.filters;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class CompositeFilter implements Filter {

  private final List<Filter> filters;
}
