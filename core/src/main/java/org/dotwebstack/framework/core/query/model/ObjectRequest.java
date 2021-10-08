package org.dotwebstack.framework.core.query.model;

import graphql.language.Field;
import graphql.schema.SelectedField;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.dotwebstack.framework.core.model.ObjectType;

@SuperBuilder
@Data
@RequiredArgsConstructor
public class ObjectRequest implements Request {

  private final ObjectType<?> objectType;

  private final Field parentField;

  private final Map<String, Object> source;

  @Builder.Default
  private final Collection<SelectedField> scalarFields = new ArrayList<>();

  @Builder.Default
  private final Map<SelectedField, ObjectRequest> objectFields = new HashMap<>();

  // TODO replace collectionObjectFields
  @Builder.Default
  private final Map<SelectedField, CollectionRequest> selectedObjectListFields = new HashMap<>();

  @Builder.Default
  private final List<KeyCriteria> keyCriteria = new ArrayList<>();

  @Builder.Default
  protected final List<NestedObjectFieldConfiguration> nestedObjectFields = new ArrayList<>();

  @Builder.Default
  private final List<AggregateObjectFieldConfiguration> aggregateObjectFields = new ArrayList<>();

  @Builder.Default
  private final List<ObjectFieldConfiguration> collectionObjectFields = new ArrayList<>();

  private final ContextCriteria contextCriteria;
}
