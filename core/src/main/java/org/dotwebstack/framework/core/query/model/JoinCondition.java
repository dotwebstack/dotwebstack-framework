package org.dotwebstack.framework.core.query.model;

import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class JoinCondition {

  private final Map<String, Object> key;

  private final Object joinTable;
}
