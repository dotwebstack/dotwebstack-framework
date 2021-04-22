package org.dotwebstack.framework.core.datafetchers;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Builder
@Getter
@EqualsAndHashCode
public class MappedByKeyCondition implements KeyCondition {

  private final String fieldName;
}
