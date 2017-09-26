package org.dotwebstack.framework.frontend.openapi.entity;

import com.google.common.collect.ImmutableMap;
import io.swagger.models.properties.Property;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import lombok.NonNull;
import org.eclipse.rdf4j.query.QueryResult;

abstract class AbstractEntity<R extends QueryResult<?>> implements Entity<R> {

  protected ImmutableMap<MediaType, Property> schemaMap;

  protected AbstractEntity(@NonNull Map<MediaType, Property> schemaMap) {
    this.schemaMap = ImmutableMap.copyOf(schemaMap);
  }

  public Map<MediaType, Property> getSchemaMap() {
    return schemaMap;
  }

}
