package org.dotwebstack.framework.core.query.model;

import lombok.Data;
import lombok.experimental.SuperBuilder;
import org.dotwebstack.framework.core.config.FieldConfiguration;

@Data
@SuperBuilder
public class ObjectFieldConfiguration {
  protected final FieldConfiguration field;

  private final ObjectRequest objectRequest;
}
