package org.dotwebstack.framework.core.datafetchers;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class KeyArgument {

  private final String name;

  private final Object value;
}
