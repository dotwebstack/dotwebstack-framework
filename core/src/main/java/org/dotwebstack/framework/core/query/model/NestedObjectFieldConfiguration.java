package org.dotwebstack.framework.core.query.model;

import java.util.List;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.dotwebstack.framework.core.config.FieldConfiguration;

@Data
@RequiredArgsConstructor
public class NestedObjectFieldConfiguration {
  private final FieldConfiguration field;

  private final List<FieldConfiguration> scalarFields;
}
