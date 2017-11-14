package org.dotwebstack.framework.frontend.openapi.entity;

import io.swagger.models.properties.Property;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import org.dotwebstack.framework.frontend.openapi.entity.builder.QueryResult;
import org.eclipse.rdf4j.query.GraphQueryResult;

public final class GraphEntity extends QueryEntity<GraphQueryResult> {

  GraphEntity(Property schemaProperty, QueryResult queryResult, String baseUri, String endpoint) {
    super(schemaProperty, queryResult, baseUri, endpoint);
  }


  public static Builder builder() {
    return new Builder();
  }

  @Override
  public GraphQueryResult getResult() {
    return null;
  }

  @Override
  public Map<MediaType, Property> getSchemaMap() {
    return null;
  }

  public static class Builder extends QueryEntity.Builder {

    public Builder withSchemaProperty(Property schemaProperty) {
      this.schemaProperty = schemaProperty;
      return this;
    }

    public Builder withQueryResult(QueryResult queryResult) {
      this.queryResult = queryResult;
      return this;
    }

    public Entity build() {
      return new GraphEntity(schemaProperty, queryResult, baseUri, endpoint);
    }
  }

}
