package org.dotwebstack.framework.frontend.openapi.entity;

import io.swagger.models.properties.Property;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import org.dotwebstack.framework.frontend.openapi.entity.builder.QueryResult;
import org.dotwebstack.framework.frontend.openapi.entity.builder.RequestParameters;

public class QueryEntity extends AbstractEntity {

  QueryEntity(Map<MediaType, Property> schemaMap, Property schemaProperty,
      RequestParameters requestParameters, QueryResult queryResult, String baseUri,
      String endpoint) {
    super(schemaMap, schemaProperty, requestParameters, queryResult, baseUri, endpoint);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private Map<MediaType, Property> schemaMap;
    private Property schemaProperty;
    private RequestParameters requestParameters;
    private QueryResult queryResult;
    private String baseUri;
    private String endpoint;

    public Builder() {

    }


    public Builder withSchemaMap(Map<MediaType, Property> schemaMap) {
      this.schemaMap = schemaMap;
      return this;
    }

    public Builder withQueryResult(QueryResult queryResult) {
      this.queryResult = queryResult;
      return this;
    }


    public QueryEntity build() {
      return new QueryEntity(schemaMap, schemaProperty, requestParameters, queryResult, baseUri,
          endpoint);
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
  }



}
