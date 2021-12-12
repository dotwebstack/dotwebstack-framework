package org.dotwebstack.framework.backend.postgres;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
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

    propagateNestedColumnPrefix(allFields);
    initColumns(allFields);
  }

  private List<PostgresObjectField> getAllFields(Map<String, ObjectType<? extends ObjectField>> objectTypes) {
    var postgresObjectTypes = objectTypes.values()
        .stream()
        .map(PostgresObjectType.class::cast)
        .collect(Collectors.toList());

    return postgresObjectTypes.stream()
        .flatMap(objectType -> objectType.getFields()
            .values()
            .stream())
        .collect(Collectors.toList());
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

  private void propagateNestedColumnPrefix(List<PostgresObjectField> allFields) {
    allFields.stream()
        .filter(this::qualifiesForPropagationColumnPrefix)
        .forEach(this::setNewTargetObjectTypeWithColumnPrefix);
  }

  private boolean qualifiesForPropagationColumnPrefix(PostgresObjectField field) {
    return isNotBlank(field.getColumnPrefix()) && hasNestedTargetType(field) && !field.getObjectType()
        .isNested() && hasOnlyScalarFields(field.getTargetType());
  }

  private boolean hasNestedTargetType(PostgresObjectField field) {
    return field.getTargetType() != null && field.getTargetType()
        .isNested();
  }

  private boolean hasOnlyScalarFields(ObjectType<? extends ObjectField> objectType) {
    return objectType.getFields()
        .values()
        .stream()
        .map(PostgresObjectField.class::cast)
        .filter(this::hasNestedTargetType)
        .findFirst()
        .isEmpty();
  }

  private void setNewTargetObjectTypeWithColumnPrefix(PostgresObjectField parentField) {
    ObjectType<? extends ObjectField> objectType = parentField.getTargetType();

    PostgresObjectType columnPrefixedObjectType = new PostgresObjectType();
    columnPrefixedObjectType.setName(objectType.getName());
    columnPrefixedObjectType
        .setFields(createScalarFieldsWithColumnPrefix(parentField.getColumnPrefix(), objectType.getFields()));
    parentField.setTargetType(columnPrefixedObjectType);
  }

  private Map<String, PostgresObjectField> createScalarFieldsWithColumnPrefix(String columnPrefix,
      Map<String, ? extends ObjectField> fields) {
    return fields.values()
        .stream()
        .map(PostgresObjectField.class::cast)
        .map(field -> new AbstractMap.SimpleEntry<>(field.getName(),
            createScalarFieldWithColumnPrefix(columnPrefix, field)))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  private PostgresObjectField createScalarFieldWithColumnPrefix(String columnPrefix, PostgresObjectField field) {
    PostgresObjectField postgresObjectField = new PostgresObjectField();
    postgresObjectField.setName(field.getName());
    postgresObjectField.setType(field.getType());
    postgresObjectField.setColumn(field.getColumn());
    postgresObjectField.setTsvColumn(field.getTsvColumn());
    postgresObjectField.setColumnPrefix(field.getColumn() == null ? columnPrefix : null);
    postgresObjectField.setObjectType(field.getObjectType());

    postgresObjectField.initColumns();

    return postgresObjectField;
  }

  private void initColumns(List<PostgresObjectField> allFields) {
    allFields.forEach(PostgresObjectField::initColumns);
  }
}
