package org.dotwebstack.framework.backend.postgres;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectField;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectType;
import org.dotwebstack.framework.core.backend.BackendLoaderFactory;
import org.dotwebstack.framework.core.backend.BackendModule;
import org.dotwebstack.framework.core.datafetchers.aggregate.AggregateHelper;
import org.dotwebstack.framework.core.model.ObjectType;
import org.springframework.stereotype.Component;

@Component
class PostgresBackendModule implements BackendModule<PostgresObjectType> {

  private final PostgresBackendLoaderFactory backendLoaderFactory;

  public PostgresBackendModule(PostgresBackendLoaderFactory backendLoaderFactory) {
    this.backendLoaderFactory = backendLoaderFactory;
  }

  @Override
  public Class<PostgresObjectType> getObjectTypeClass() {
    return PostgresObjectType.class;
  }

  @Override
  public BackendLoaderFactory getBackendLoaderFactory() {
    return backendLoaderFactory;
  }

  @Override
  public void init(Map<String, ObjectType<?>> objectTypes) {
    var postgresObjectTypes = objectTypes.values()
        .stream()
        .map(PostgresObjectType.class::cast)
        .collect(Collectors.toList());

    var allFields = postgresObjectTypes.stream()
        .flatMap(objectType -> objectType.getFields()
            .values()
            .stream())
        .collect(Collectors.toList());

    allFields.stream()
        .filter(objectField -> objectTypes.containsKey(objectField.getType()))
        .forEach(objectField -> {
          var targetType = (PostgresObjectType) objectTypes.get(objectField.getType());
          objectField.setTargetType(targetType);
        });

    // mapped By
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

    // aggregation
    allFields.stream()
        .filter(AggregateHelper::isAggregate)
        .forEach(objectField -> {
          var aggregationOfType = (PostgresObjectType) objectTypes.get(objectField.getAggregationOf());

          objectField.setAggregationOfType(aggregationOfType);
        });
  }
}
