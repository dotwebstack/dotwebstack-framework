package org.dotwebstack.framework.core.query.model;

import graphql.schema.SelectedField;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.dotwebstack.framework.core.config.FieldConfiguration;
import org.dotwebstack.framework.core.config.TypeConfiguration;
import org.dotwebstack.framework.core.model.ObjectType;

@SuperBuilder
@Data
@RequiredArgsConstructor
public class ObjectRequest implements Request {

  // @NonNull
  private final TypeConfiguration<?> typeConfiguration;

  private final ObjectType<?> objectType;

  @Builder.Default
  private final List<ScalarField> scalarFields = new ArrayList<>();

  // TODO replace scalarFields
  @Builder.Default
  private final Collection<SelectedField> selectedScalarFields = new ArrayList<>();

  // TODO replace objectFields
  @Builder.Default
  private final Map<SelectedField, ObjectRequest> selectedObjectFields = new HashMap<>();

  @Builder.Default
  private final List<KeyCriteria> keyCriteria = new ArrayList<>();

  @Builder.Default
  protected final List<ObjectFieldConfiguration> objectFields = new ArrayList<>();

  @Builder.Default
  protected final List<NestedObjectFieldConfiguration> nestedObjectFields = new ArrayList<>();

  @Builder.Default
  private final List<AggregateObjectFieldConfiguration> aggregateObjectFields = new ArrayList<>();

  @Builder.Default
  private final List<ObjectFieldConfiguration> collectionObjectFields = new ArrayList<>();

  @Builder.Default
  private final List<ContextCriteria> contextCriterias = List.of();

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

  public Optional<ObjectFieldConfiguration> getCollectionObjectField(FieldConfiguration field) {
    return collectionObjectFields.stream()
        .filter(objectField -> objectField.getField()
            .getName()
            .equals(field.getName()))
        .findFirst();
  }
}
