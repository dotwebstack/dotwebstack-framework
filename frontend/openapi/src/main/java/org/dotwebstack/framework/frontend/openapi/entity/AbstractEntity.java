package org.dotwebstack.framework.frontend.openapi.entity;

import io.swagger.models.properties.Property;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.entity.builder.QueryResult;
import org.dotwebstack.framework.frontend.openapi.entity.builder.RequestParameters;

abstract class AbstractEntity implements Entity {

  private final RequestParameters requestParameters;
  private QueryResult queryResult;

  private Property schemaProperty;

  private String baseUri;

  private String endpoint;

  protected AbstractEntity(@NonNull Property schemaProperty,
      @NonNull RequestParameters requestParameters, @NonNull QueryResult queryResult,
      String baseUri, String endpoint) {

    this.schemaProperty = schemaProperty;
    this.requestParameters = requestParameters;
    this.queryResult = queryResult;
    this.baseUri = baseUri;
    this.endpoint = endpoint;
  }

  @Override
  public Property getSchemaProperty() {
    return schemaProperty;
  }

  @Override
  public RequestParameters getRequestParameters() {
    return requestParameters;
  }


  @Override
  public QueryResult getQueryResult() {
    return queryResult;
  }

  @Override
  public String getEndpoint() {
    return endpoint;
  }

  @Override
  public String getBaseUri() {
    return baseUri;
  }
}
