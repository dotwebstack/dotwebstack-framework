package org.dotwebstack.framework.core.datafetchers;

import java.util.Map;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Builder
@Getter
@EqualsAndHashCode
public class FieldKeyCondition implements KeyCondition {

  private final Map<String, Object> fieldValues;
}
