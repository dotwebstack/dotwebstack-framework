package org.dotwebstack.framework.core.query.model;

import java.util.HashSet;
import java.util.Set;
import lombok.Builder;
import lombok.Data;
import org.dotwebstack.framework.core.config.FieldConfiguration;
import org.dotwebstack.framework.core.query.model.origin.Origin;

@Data
@Builder
public class ScalarField {
  private FieldConfiguration field;

  @Builder.Default
  private Set<Origin> origins = new HashSet<>();

  public String getName() {
    return field.getName();
  }

  public void addOrigin(Origin origin) {
    origins.add(origin);
  }

  public boolean hasOrigin(Class<? extends Origin> origin) {
    return origins.stream()
        .anyMatch(origin::isInstance);
  }

}
