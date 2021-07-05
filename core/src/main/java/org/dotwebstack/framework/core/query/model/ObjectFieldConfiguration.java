package org.dotwebstack.framework.core.query.model;

import lombok.Data;
import lombok.experimental.SuperBuilder;
import org.dotwebstack.framework.core.config.FieldConfiguration;
import org.dotwebstack.framework.core.query.model.origin.Filtering;

@Data
@SuperBuilder
public class ObjectFieldConfiguration {
  protected final FieldConfiguration field;

  private final ObjectRequest objectRequest;

  public boolean hasNestedFilteringOrigin() {
    boolean hasNestedFilteringOrigin = objectRequest.getScalarFields()
        .stream()
        .flatMap(scalarField -> scalarField.getOrigins()
            .stream())
        .anyMatch(Filtering.class::isInstance);

    if (!hasNestedFilteringOrigin) {
      hasNestedFilteringOrigin = objectRequest.getObjectFields()
          .stream()
          .anyMatch(ObjectFieldConfiguration::hasNestedFilteringOrigin);
    }

    return hasNestedFilteringOrigin;
  }
}
