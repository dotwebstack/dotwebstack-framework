package org.dotwebstack.framework.backend.postgres.query;

import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
class JoinCondition {

  private final Map<String, Object> fields;
}
