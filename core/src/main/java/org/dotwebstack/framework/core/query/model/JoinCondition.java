package org.dotwebstack.framework.core.query.model;

import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@EqualsAndHashCode
public class JoinCondition {

  private final Map<String, Object> key;
}
