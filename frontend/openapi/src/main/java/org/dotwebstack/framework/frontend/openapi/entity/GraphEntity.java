package org.dotwebstack.framework.frontend.openapi.entity;

import io.swagger.models.properties.Property;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import lombok.NonNull;
import org.eclipse.rdf4j.query.GraphQueryResult;

public final class GraphEntity extends AbstractEntity<GraphQueryResult> {

  private GraphQueryResult result;

  public GraphEntity(Map<MediaType, Property> schemaMap, @NonNull GraphQueryResult result) {
    super(schemaMap);
    this.result = result;
  }

  public GraphQueryResult getResult() {
    return result;
  }

}
