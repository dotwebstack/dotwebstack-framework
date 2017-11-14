package org.dotwebstack.framework.frontend.openapi.entity;

import io.swagger.models.properties.Property;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.entity.builder.QueryResult;

abstract class AbstractEntity<Q extends org.eclipse.rdf4j.query.QueryResult<?>>
    implements Entity<Q> {

  private QueryResult queryResult;
  private Q queryResultDb;

  private Property schemaProperty;

  private Map<MediaType, Property> schemaMap;

  private String baseUri;

  private String endpoint;

  protected AbstractEntity(@NonNull Property schemaProperty, @NonNull QueryResult queryResult,
      String baseUri, String endpoint) {

    this.schemaProperty = schemaProperty;
    this.queryResult = queryResult;
    this.baseUri = baseUri;
    this.endpoint = endpoint;
  }

  protected AbstractEntity(@NonNull Map<MediaType, Property> schemaProperty,
      @NonNull Q queryResultDb) {

    this.schemaMap = schemaProperty;
    this.queryResultDb = queryResultDb;
  }

  @Override
  public Property getSchemaProperty() {
    return schemaProperty;
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

  public Q getQueryResultDb() {
    return queryResultDb;
  }

  @Override
  public Map<MediaType, Property> getSchemaMap() {
    return schemaMap;
  }
}
