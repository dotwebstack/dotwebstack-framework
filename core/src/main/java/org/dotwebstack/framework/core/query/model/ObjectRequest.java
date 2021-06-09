package org.dotwebstack.framework.core.query.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.dotwebstack.framework.core.config.FieldConfiguration;
import org.dotwebstack.framework.core.config.TypeConfiguration;

@SuperBuilder
@Data
@RequiredArgsConstructor
public class ObjectRequest implements Request {

  @NonNull
  private final TypeConfiguration<?> typeConfiguration;

  @Builder.Default
  private final List<ScalarField> scalarFields = new ArrayList<>();

  @Builder.Default
  private final List<KeyCriteria> keyCriteria = List.of();

  @Builder.Default
  protected final List<ObjectFieldConfiguration> objectFields = new ArrayList<>();

  @Builder.Default
  protected final List<NestedObjectFieldConfiguration> nestedObjectFields = new ArrayList<>();

  @Builder.Default
  private final List<AggregateObjectFieldConfiguration> aggregateObjectFields = List.of();

  @Builder.Default
  private final List<ObjectFieldConfiguration> collectionObjectFields = List.of();

  public void addScalarField(ScalarField scalar) {
    if (!scalarFields.contains(scalar)) {
      scalarFields.add(scalar);
    }
  }

  public Optional<ObjectFieldConfiguration> getObjectField(FieldConfiguration field) {
    return objectFields.stream()
        .filter(objectField -> objectField.getField()
            .getName()
            .equals(field.getName()))
        .findFirst();
  }

  public Optional<ScalarField> getScalarField(FieldConfiguration field) {
    return scalarFields.stream()
        .filter(scalarField -> scalarField.getField()
            .getName()
            .equals(field.getName()))
        .findFirst();
  }
}
