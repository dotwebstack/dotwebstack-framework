package org.dotwebstack.framework.core.datafetchers;

import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class FieldKeyCondition implements KeyCondition {

  private final Map<String, Object> fieldValues;
}
