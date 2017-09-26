package org.dotwebstack.framework.frontend.openapi.entity;

import io.swagger.models.properties.Property;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import org.eclipse.rdf4j.query.QueryResult;

public interface Entity<R extends QueryResult<?>> {

  R getResult();

  Map<MediaType, Property> getSchemaMap();

}
