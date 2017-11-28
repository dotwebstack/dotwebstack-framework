package org.dotwebstack.framework.frontend.openapi.entity;

import io.swagger.models.properties.Property;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import lombok.NonNull;

abstract class AbstractEntity<Q extends org.eclipse.rdf4j.query.QueryResult<?>>
    implements Entity<Q> {

  private Q queryResultDb;

  private Map<MediaType, Property> schemaMap;


  AbstractEntity(@NonNull Map<MediaType, Property> schemaProperty) {
    this.schemaMap = schemaProperty;
  }

  AbstractEntity(@NonNull Map<MediaType, Property> schemaProperty,
      @NonNull Q queryResultDb) {
    this.schemaMap = schemaProperty;
    this.queryResultDb = queryResultDb;
  }


  public Q getQueryResultDb() {
    return queryResultDb;
  }

  @Override
  public Map<MediaType, Property> getSchemaMap() {
    return schemaMap;
  }
}
