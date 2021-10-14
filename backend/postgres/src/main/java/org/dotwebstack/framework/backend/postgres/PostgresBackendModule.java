package org.dotwebstack.framework.backend.postgres;

import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectField;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectType;
import org.dotwebstack.framework.core.backend.BackendLoaderFactory;
import org.dotwebstack.framework.core.backend.BackendModule;
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
    objectTypes.values()
        .stream()
        .map(PostgresObjectType.class::cast)
        .flatMap(objectType -> objectType.getFields()
            .values()
            .stream())
        .filter(objectField -> StringUtils.isNotEmpty(objectField.getMappedBy()))
        .filter(objectField -> StringUtils.isNotEmpty(objectField.getType())) // TODO: Aggregatie heeft geen type, moet
                                                                              // nog bekeken worden
        .forEach(objectField -> {
          var mappedByType = objectTypes.get(objectField.getType());

          PostgresObjectField mappedByObjectField = (PostgresObjectField) mappedByType.getFields()
              .get(objectField.getMappedBy());

          objectField.setMappedByObjectField(mappedByObjectField);
        });
  }
}
