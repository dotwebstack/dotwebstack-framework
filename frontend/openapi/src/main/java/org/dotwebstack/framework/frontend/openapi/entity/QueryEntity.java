package org.dotwebstack.framework.frontend.openapi.entity;

import io.swagger.models.properties.Property;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import org.dotwebstack.framework.frontend.openapi.entity.builder.QueryResult;

public abstract class QueryEntity<Q extends org.eclipse.rdf4j.query.QueryResult<?>>
    extends AbstractEntity<Q> {

  QueryEntity(Property schemaProperty, QueryResult queryResult, String baseUri, String endpoint) {
    super(schemaProperty, queryResult, baseUri, endpoint);
  }

  QueryEntity(Map<MediaType, Property> schemaProperty, Q queryResult) {
    super(schemaProperty, queryResult);
  }


  public abstract static class Builder {
    Property schemaProperty;
    QueryResult queryResult;
    String baseUri;
    String endpoint;


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
