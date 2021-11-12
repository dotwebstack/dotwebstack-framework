package org.dotwebstack.framework.core.query.model;

import java.util.Map;
import java.util.Set;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class JoinCriteria {

  private final Set<Map<String, Object>> keys;

  private final JoinCondition joinCondition;
}
