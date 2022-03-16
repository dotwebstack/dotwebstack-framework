package org.dotwebstack.framework.core.query.model;

import java.util.Map;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@Builder
@EqualsAndHashCode
public class FieldRequest {

  private final String name;

  private final String resultKey;

  private final boolean isList;

  private final Map<String, Object> arguments;
}
