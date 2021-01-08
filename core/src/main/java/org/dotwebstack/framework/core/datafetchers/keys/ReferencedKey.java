package org.dotwebstack.framework.core.datafetchers.keys;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ReferencedKey implements Key {

  private final Object value;
}
