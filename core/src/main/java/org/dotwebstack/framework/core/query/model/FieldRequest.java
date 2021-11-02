package org.dotwebstack.framework.core.query.model;

import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FieldRequest {

  private final String name;

  private final boolean isList;

  private final Map<String, Object> arguments;
}
