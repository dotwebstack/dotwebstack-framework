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


  protected AbstractEntity(@NonNull Property schemaProperty, @NonNull QueryResult queryResult) {

    this.schemaProperty = schemaProperty;
    this.queryResult = queryResult;

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

  public Q getQueryResultDb() {
    return queryResultDb;
  }

  @Override
  public Map<MediaType, Property> getSchemaMap() {
    return schemaMap;
  }
}
