package org.dotwebstack.framework.backend.postgres;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectField;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectType;
import org.dotwebstack.framework.core.backend.BackendLoaderFactory;
import org.dotwebstack.framework.core.backend.BackendModule;
import org.dotwebstack.framework.core.datafetchers.aggregate.AggregateHelper;
import org.dotwebstack.framework.core.model.ObjectField;
import org.dotwebstack.framework.core.model.ObjectType;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
class PostgresBackendModule implements BackendModule<PostgresObjectType> {

  private final PostgresBackendLoaderFactory backendLoaderFactory;

  @Override
  public Class<PostgresObjectType> getObjectTypeClass() {
    return PostgresObjectType.class;
  }

  @Override
  public BackendLoaderFactory getBackendLoaderFactory() {
    return backendLoaderFactory;
  }

  @Override
  public void init(Map<String, ObjectType<? extends ObjectField>> objectTypes) {
    List<PostgresObjectField> allFields = getAllFields(objectTypes);

    setTargetType(objectTypes, allFields);
    setMappedByObjectField(objectTypes, allFields);
    setAggregationOfType(objectTypes, allFields);
    initColumns(objectTypes.values());
  }

  private void initColumns(Collection<ObjectType<? extends ObjectField>> objectTypes) {
    objectTypes.stream()
        .filter(PostgresObjectType.class::isInstance)
        .map(PostgresObjectType.class::cast)
        .filter(Predicate.not(PostgresObjectType::isNested))
        .forEach(objectType -> {
          // Initialize column for scalar field

          objectType.getFields()
              .values()
              .forEach(PostgresObjectField::initColumns);

          // Initialize nested columns
          initColumns(List.of(), objectType);
        });
  }

  private List<PostgresObjectType> initColumns(List<PostgresObjectField> ancestors, PostgresObjectType objectType) {
    return objectType.getFields()
        .values()
        .stream()
        .filter(PostgresObjectField::hasNestedFields)
        // Filter out the ref node(s).
        .filter(field -> !field.getTargetType()
            .getFields()
            .containsKey("nodes")
            && !field.getTargetType()
                .getFields()
                .containsKey("node"))
        .flatMap(objectField -> {
          var targetType = (PostgresObjectType) objectField.getTargetType();

          var fieldAncestors = new ArrayList<>(ancestors);
          fieldAncestors.add(objectField);

          var nestedObjectType = initColumns(fieldAncestors, targetType);
          var resultObjectType = new PostgresObjectType(targetType, fieldAncestors);

          objectField.setTargetType(resultObjectType);

          return Stream.concat(Stream.of(resultObjectType), nestedObjectType.stream());
        })
        .toList();
  }

  private List<PostgresObjectField> getAllFields(Map<String, ObjectType<? extends ObjectField>> objectTypes) {
    var postgresObjectTypes = objectTypes.values()
        .stream()
        .map(PostgresObjectType.class::cast)
        .toList();

    return postgresObjectTypes.stream()
        .flatMap(objectType -> objectType.getFields()
            .values()
            .stream())
        .toList();
  }

  private void setTargetType(Map<String, ObjectType<? extends ObjectField>> objectTypes,
      List<PostgresObjectField> allFields) {
    allFields.stream()
        .filter(objectField -> objectTypes.containsKey(objectField.getType()))
        .forEach(objectField -> {
          var targetType = getObjectType(objectTypes, objectField.getType());
          objectField.setTargetType(targetType);
        });
  }

  private void setMappedByObjectField(Map<String, ObjectType<? extends ObjectField>> objectTypes,
      List<PostgresObjectField> allFields) {
    allFields.stream()
        .filter(objectField -> isNotEmpty(objectField.getMappedBy()))
        .forEach(objectField -> {
          var type = StringUtils.isNotEmpty(objectField.getAggregationOf()) ? objectField.getAggregationOf()
              : objectField.getType();

          var mappedByObjectField = getObjectType(objectTypes, type).getField(objectField.getMappedBy());

          objectField.setMappedByObjectField(mappedByObjectField);
        });
  }

  private void setAggregationOfType(Map<String, ObjectType<? extends ObjectField>> objectTypes,
      List<PostgresObjectField> allFields) {
    allFields.stream()
        .filter(AggregateHelper::isAggregate)
        .forEach(objectField -> {
          var aggregationOfType = getObjectType(objectTypes, objectField.getAggregationOf());
          objectField.setTargetType(aggregationOfType);
        });
  }

  private static PostgresObjectType getObjectType(Map<String, ObjectType<? extends ObjectField>> objectTypes,
      String name) {
    return (PostgresObjectType) Optional.ofNullable(objectTypes.get(name))
        .orElseThrow(() -> invalidConfigurationException("Object type '{}' not found.", name));
  }
}
