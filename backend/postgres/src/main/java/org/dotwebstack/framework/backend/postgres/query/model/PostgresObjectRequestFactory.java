package org.dotwebstack.framework.backend.postgres.query.model;

import org.dotwebstack.framework.core.query.model.ObjectRequest;

public class PostgresObjectRequestFactory {

  private PostgresObjectRequestFactory() {}

  public static PostgresObjectRequest create(ObjectRequest objectRequest) {
    return createPostgresObjectRequest(objectRequest);
  }

  private static PostgresObjectRequest createPostgresObjectRequest(ObjectRequest objectRequest) {
    return PostgresObjectRequest.builder()
        .typeConfiguration(objectRequest.getTypeConfiguration())
        .scalarFields(objectRequest.getScalarFields())
        .objectFields(objectRequest.getObjectFields())
        .keyCriteria(objectRequest.getKeyCriteria())
        .nestedObjectFields(objectRequest.getNestedObjectFields())
        .aggregateObjectFields(objectRequest.getAggregateObjectFields())
        .collectionObjectFields(objectRequest.getCollectionObjectFields())
        .contextCriteria(objectRequest.getContextCriteria())
        .build();
  }


}
