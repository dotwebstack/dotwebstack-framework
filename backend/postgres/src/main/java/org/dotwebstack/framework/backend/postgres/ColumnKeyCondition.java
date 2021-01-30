package org.dotwebstack.framework.backend.postgres;

import java.util.Map;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.dotwebstack.framework.core.datafetchers.KeyCondition;

@Builder
@Getter
@EqualsAndHashCode
public class ColumnKeyCondition implements KeyCondition {

  private final Map<String, Object> valueMap;
}
