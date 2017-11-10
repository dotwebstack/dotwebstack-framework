package org.dotwebstack.framework.frontend.openapi.entity;

import io.swagger.models.properties.Property;
import org.dotwebstack.framework.frontend.openapi.entity.builder.QueryResult;
import org.dotwebstack.framework.frontend.openapi.entity.builder.RequestParameters;

public final class GraphEntity extends QueryEntity {

  public GraphEntity(Property schemaProperty, RequestParameters requestParameters,
      QueryResult queryResult, String baseUri, String endpoint) {
    super(schemaProperty, requestParameters, queryResult, baseUri, endpoint);
  }

  public static GraphEntity.Builder builder() {
    return new GraphEntity.Builder();
  }

  public static class Builder extends QueryEntity.Builder {
    public Entity build() {
      return new GraphEntity(schemaProperty, requestParameters, queryResult, baseUri, endpoint);
    }
  }

}
