package org.dotwebstack.framework.core.query.model;

import java.util.List;
import lombok.Builder;
import lombok.Data;
import org.dotwebstack.framework.core.config.FieldConfiguration;

@Data
@Builder
public class NestedObjectFieldConfiguration {
  private final FieldConfiguration field;

  private final List<FieldConfiguration> scalarFields;
}
