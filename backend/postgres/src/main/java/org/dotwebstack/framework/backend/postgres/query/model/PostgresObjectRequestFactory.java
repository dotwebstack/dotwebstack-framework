package org.dotwebstack.framework.backend.postgres.query.model;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.dotwebstack.framework.core.query.model.ObjectFieldConfiguration;
import org.dotwebstack.framework.core.query.model.ObjectRequest;

public class PostgresObjectRequestFactory {

  private PostgresObjectRequestFactory() {}

  public static PostgresObjectRequest create(ObjectRequest objectRequest) {
    return createPostgresObjectRequest(objectRequest);
  }

  private static PostgresObjectRequest createPostgresObjectRequest(ObjectRequest objectRequest) {
    var objectFieldsByType = mapObjectFieldsByName(objectRequest.getObjectFields());
    return PostgresObjectRequest.builder()
        .typeConfiguration(objectRequest.getTypeConfiguration())
        .scalarFields(objectRequest.getScalarFields())
        .objectFieldsByFieldName(objectFieldsByType)
        .objectFields(objectRequest.getObjectFields())
        .keyCriteria(objectRequest.getKeyCriteria())
        .nestedObjectFields(objectRequest.getNestedObjectFields())
        .aggregateObjectFields(objectRequest.getAggregateObjectFields())
        .collectionObjectFields(objectRequest.getCollectionObjectFields())
        .build();
  }

  private static Map<String, ObjectFieldConfiguration> mapObjectFieldsByName(
      List<ObjectFieldConfiguration> objectFields) {
    return objectFields.stream()
        .collect(Collectors.toMap(objectField -> objectField.getField()
            .getName(), Function.identity()));
  }
}
