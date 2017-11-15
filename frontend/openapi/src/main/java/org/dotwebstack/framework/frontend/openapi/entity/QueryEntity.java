package org.dotwebstack.framework.frontend.openapi.entity;

import io.swagger.models.properties.Property;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import org.dotwebstack.framework.frontend.openapi.entity.builder.QueryResult;

public abstract class QueryEntity<Q extends org.eclipse.rdf4j.query.QueryResult<?>>
    extends AbstractEntity<Q> {

  QueryEntity(Property schemaProperty, QueryResult queryResult) {
    super(schemaProperty, queryResult);
  }

  QueryEntity(Map<MediaType, Property> schemaProperty, Q queryResult) {
    super(schemaProperty, queryResult);
  }

  public abstract static class Builder {
    Property schemaProperty;
    QueryResult queryResult;

    public abstract Entity build();

  }
}
