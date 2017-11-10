package org.dotwebstack.framework.frontend.openapi.entity;

import io.swagger.models.properties.Property;
import org.dotwebstack.framework.frontend.openapi.entity.builder.QueryResult;
import org.dotwebstack.framework.frontend.openapi.entity.builder.RequestParameters;

public abstract class QueryEntity extends AbstractEntity {

  QueryEntity(Property schemaProperty, RequestParameters requestParameters, QueryResult queryResult,
      String baseUri, String endpoint) {
    super(schemaProperty, requestParameters, queryResult, baseUri, endpoint);
  }


  public abstract static class Builder {
    Property schemaProperty;
    RequestParameters requestParameters;
    QueryResult queryResult;
    String baseUri;
    String endpoint;

    public Builder withQueryResult(QueryResult queryResult) {
      this.queryResult = queryResult;
      return this;
    }



    public Builder withSchemaProperty(Property schemaProperty) {
      this.schemaProperty = schemaProperty;
      return this;
    }

    public Builder withRequestParameters(RequestParameters requestParameters) {
      this.requestParameters = requestParameters;
      return this;
    }

    public Builder withBaseUri(String baseUri) {
      this.baseUri = baseUri;
      return this;
    }

    public Builder withPath(String path) {
      this.endpoint = path;
      return this;
    }

    public abstract Entity build();
  }



}
