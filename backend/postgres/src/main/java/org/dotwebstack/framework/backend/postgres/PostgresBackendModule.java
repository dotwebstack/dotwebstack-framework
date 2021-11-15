package org.dotwebstack.framework.backend.postgres;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectField;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectType;
import org.dotwebstack.framework.backend.postgres.query.JoinHelper;
import org.dotwebstack.framework.core.backend.BackendLoaderFactory;
import org.dotwebstack.framework.core.backend.BackendModule;
import org.dotwebstack.framework.core.datafetchers.aggregate.AggregateHelper;
import org.dotwebstack.framework.core.model.ObjectField;
import org.dotwebstack.framework.core.model.ObjectType;
import org.dotwebstack.framework.ext.spatial.SpatialConstants;
import org.dotwebstack.framework.ext.spatial.model.Spatial;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
class PostgresBackendModule implements BackendModule<PostgresObjectType> {

  private final Optional<Spatial> spatial;

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
    setAggredationOfType(objectTypes, allFields);
    setColumnPerSrid(allFields);
    propagateJoinTable(allFields);
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
          var targetType = (PostgresObjectType) objectTypes.get(objectField.getType());
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

          var objectType = objectTypes.get(type);

          PostgresObjectField mappedByObjectField = (PostgresObjectField) objectType.getFields()
              .get(objectField.getMappedBy());

          objectField.setMappedByObjectField(mappedByObjectField);
        });
  }

  private void setAggredationOfType(Map<String, ObjectType<? extends ObjectField>> objectTypes,
      List<PostgresObjectField> allFields) {
    allFields.stream()
        .filter(AggregateHelper::isAggregate)
        .forEach(objectField -> {
          var aggregationOfType = (PostgresObjectType) objectTypes.get(objectField.getAggregationOf());

          objectField.setAggregationOfType(aggregationOfType);
        });
  }

  private void setColumnPerSrid(List<PostgresObjectField> allFields) {
    spatial.ifPresent(value -> allFields.stream()
        .filter(this::isGeometryType)
        .map(PostgresObjectField.class::cast)
        .forEach(objectField -> objectField.setSpatialReferenceSystems(value.getReferenceSystems())));
  }

  private boolean isGeometryType(PostgresObjectField postgresObjectField) {
    return SpatialConstants.GEOMETRY.equals(postgresObjectField.getType());
  }

  private void propagateJoinTable(List<PostgresObjectField> allFields) {
    allFields.stream()
        .filter(JoinHelper::hasNestedReference)
        .forEach(field -> {
          Optional.of(field)
              .stream()
              .map(PostgresObjectField::getTargetType)
              .flatMap(objectType -> objectType.getFields()
                  .values()
                  .stream())
              .map(PostgresObjectField.class::cast)
              .filter(nestedObjectField -> !nestedObjectField.getTargetType()
                  .isNested())
              .forEach(nestedField -> {
                var targetObjectType = (PostgresObjectType) nestedField.getTargetType();
                var resolvedJoinTable = JoinHelper.resolveJoinTable(targetObjectType, field.getJoinTable());

                nestedField.setJoinTable(resolvedJoinTable);
              });

        });
  }


}
