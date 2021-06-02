package org.dotwebstack.framework.backend.postgres.query.model;

import org.dotwebstack.framework.core.config.FieldConfiguration;
import org.dotwebstack.framework.core.query.model.ObjectFieldConfiguration;
import org.dotwebstack.framework.core.query.model.ObjectRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PostgresObjectRequestFactory {
  public static PostgresObjectRequest create(ObjectRequest objectRequest){
    return createPostgresObjectRequest(objectRequest);
  }

  private static PostgresObjectRequest createPostgresObjectRequest(ObjectRequest objectRequest){
    var postgresObjectFieldsByType = convertObjectFields(objectRequest.getObjectFields());
    return PostgresObjectRequest.builder()
        .typeConfiguration(objectRequest.getTypeConfiguration())
        .scalarFields(objectRequest.getScalarFields())
        .postgresObjectFieldsByType(postgresObjectFieldsByType)
        .postgresObjectFields(new ArrayList<PostgresObjectFieldConfiguration>(postgresObjectFieldsByType.values()))
        .keyCriteria(objectRequest.getKeyCriteria())
        .nestedObjectFields(objectRequest.getNestedObjectFields())
        .aggregateObjectFields(objectRequest.getAggregateObjectFields())
        .collectionObjectFields(objectRequest.getCollectionObjectFields())
        .build();
  }

  private static Map<String, PostgresObjectFieldConfiguration> convertObjectFields(List<ObjectFieldConfiguration> objectFields) {
    return objectFields.stream().map(objectField -> {
      var postgresObjectRequest = createPostgresObjectRequest(objectField.getObjectRequest());
      return PostgresObjectFieldConfiguration.builder()
          .field(objectField.getField())
          .postgresObjectRequest(postgresObjectRequest)
          .build();

    }).collect(Collectors.toMap(p -> p.getField().getType(), Function.identity()));
  }
}
