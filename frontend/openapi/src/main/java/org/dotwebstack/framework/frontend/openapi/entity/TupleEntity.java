package org.dotwebstack.framework.frontend.openapi.entity;

import io.swagger.models.properties.Property;
import org.dotwebstack.framework.frontend.openapi.entity.builder.QueryResult;
import org.dotwebstack.framework.frontend.openapi.entity.builder.RequestParameters;

public final class TupleEntity extends QueryEntity {


  TupleEntity(Property schemaProperty, RequestParameters requestParameters, QueryResult queryResult,
      String baseUri, String endpoint) {
    super(schemaProperty, requestParameters, queryResult, baseUri, endpoint);
  }

}
