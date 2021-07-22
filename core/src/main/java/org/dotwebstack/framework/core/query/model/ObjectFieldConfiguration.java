package org.dotwebstack.framework.core.query.model;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import org.dotwebstack.framework.core.config.AbstractFieldConfiguration;
import org.dotwebstack.framework.core.config.FieldConfiguration;
import org.dotwebstack.framework.core.config.TypeConfiguration;
import org.dotwebstack.framework.core.query.model.origin.Filtering;

@Data
@SuperBuilder
public class ObjectFieldConfiguration {
  protected final FieldConfiguration field;

  private final ObjectRequest objectRequest;

  public boolean hasNestedFilteringOrigin() {
    List<ScalarField> scalarFields = objectRequest.getScalarFields();

    List<ScalarField> nestedScalarFields = objectRequest.getNestedObjectFields()
        .stream()
        .flatMap(nestedObjectFieldConfiguration -> nestedObjectFieldConfiguration.getScalarFields()
            .stream())
        .collect(Collectors.toList());

    boolean hasNestedFilteringOrigin = Stream.concat(scalarFields.stream(), nestedScalarFields.stream())
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

  public static ObjectFieldConfiguration createObjectFieldConfiguration(
      TypeConfiguration<? extends FieldConfiguration> typeConfiguration,
      AbstractFieldConfiguration fieldConfiguration) {
    var objectRequest = ObjectRequest.builder()
        .typeConfiguration(typeConfiguration)
        .build();

    return ObjectFieldConfiguration.builder()
        .field(fieldConfiguration)
        .objectRequest(objectRequest)
        .build();
  }
}
