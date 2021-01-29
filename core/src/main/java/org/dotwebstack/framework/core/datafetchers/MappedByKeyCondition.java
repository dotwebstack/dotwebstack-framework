package org.dotwebstack.framework.core.datafetchers;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class MappedByKeyCondition implements KeyCondition {

  private final String fieldName;
}
