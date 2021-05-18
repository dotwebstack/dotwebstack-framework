package org.dotwebstack.framework.core.query.model;

import lombok.Builder;
import lombok.Data;
import org.dotwebstack.framework.core.config.FieldConfiguration;

@Data
@Builder
public class ObjectFieldConfiguration {
  private final FieldConfiguration field;

  private final ObjectQuery objectQuery;
}
