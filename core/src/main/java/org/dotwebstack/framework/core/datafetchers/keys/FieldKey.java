package org.dotwebstack.framework.core.datafetchers.keys;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class FieldKey implements Key {

  private final String name;

  private final Object value;
}
