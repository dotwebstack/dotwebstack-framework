package org.dotwebstack.framework.backend.postgres.query.model;

import java.util.List;
import org.dotwebstack.framework.backend.postgres.query.ObjectQueryContext;
import org.dotwebstack.framework.core.query.model.ObjectRequest;
import org.dotwebstack.framework.core.query.model.SortCriteria;

public class PostgresObjectRequestFactory {

  private PostgresObjectRequestFactory() {}

  public static PostgresObjectRequest create(ObjectRequest objectRequest, List<SortCriteria> sortCriterias,
      ObjectQueryContext objectQueryContext) {
    return createPostgresObjectRequest(objectRequest, sortCriterias, objectQueryContext);
  }

  private static PostgresObjectRequest createPostgresObjectRequest(ObjectRequest objectRequest,
      List<SortCriteria> sortCriterias, ObjectQueryContext objectQueryContext) {
    var postgresObjectRequest = PostgresObjectRequest.builder()
        .typeConfiguration(objectRequest.getTypeConfiguration())
        .scalarFields(objectRequest.getScalarFields())
        .objectFields(objectRequest.getObjectFields())
        .keyCriteria(objectRequest.getKeyCriteria())
        .nestedObjectFields(objectRequest.getNestedObjectFields())
        .aggregateObjectFields(objectRequest.getAggregateObjectFields())
        .collectionObjectFields(objectRequest.getCollectionObjectFields())
        .contextCriterias(objectRequest.getContextCriterias())
        .build();

    sortCriterias.forEach(
        sortCriteria -> postgresObjectRequest.addFields(sortCriteria, objectQueryContext.getFieldPathAliasMap()));

    return postgresObjectRequest;
  }


}
