package org.dotwebstack.framework.core.query.model;

import java.util.Set;
import lombok.Builder;
import lombok.Data;
import org.dotwebstack.framework.core.config.FieldConfiguration;

@Data
@Builder
public class ScalarField {
  private FieldConfiguration field;

  private Set<Origin> origins;

  public String getName() {
    return field.getName();
  }

  public void addOrigin(Origin origin) {
    origins.add(origin);
  }

  public boolean hasOrigin(Origin origin) {
    return origins.contains(origin);
  }

}
