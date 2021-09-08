package org.dotwebstack.framework.core.query.model;

import lombok.Data;
import lombok.experimental.SuperBuilder;
import org.dotwebstack.framework.core.config.FieldConfiguration;

@Data
@SuperBuilder
public class ObjectFieldConfiguration {
  protected final FieldConfiguration field;

  private final ObjectRequest objectRequest;

  public boolean hasNestedFilteringOrigin() {
    return false;
    // List<ScalarField> scalarFields = objectRequest.getScalarFields();
    //
    // List<ScalarField> nestedScalarFields = objectRequest.getNestedObjectFields()
    // .stream()
    // .flatMap(nestedObjectFieldConfiguration -> nestedObjectFieldConfiguration.getScalarFields()
    // .stream())
    // .collect(Collectors.toList());
    //
    // boolean hasNestedFilteringOrigin = Stream.concat(scalarFields.stream(),
    // nestedScalarFields.stream())
    // .flatMap(scalarField -> scalarField.getOrigins()
    // .stream())
    // .anyMatch(Filtering.class::isInstance);
    //
    // if (!hasNestedFilteringOrigin) {
    // hasNestedFilteringOrigin = objectRequest.getObjectFields()
    // .stream()
    // .anyMatch(ObjectFieldConfiguration::hasNestedFilteringOrigin);
    // }
    //
    // return hasNestedFilteringOrigin;
  }
}
