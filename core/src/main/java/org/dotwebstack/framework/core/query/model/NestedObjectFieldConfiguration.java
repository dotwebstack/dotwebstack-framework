package org.dotwebstack.framework.core.query.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import org.dotwebstack.framework.core.config.FieldConfiguration;

@Data
@Builder
public class NestedObjectFieldConfiguration {
  private final FieldConfiguration field;

  @Builder.Default
  private final List<ScalarField> scalarFields = new ArrayList<>();
}
