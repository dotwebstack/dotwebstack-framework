package org.dotwebstack.framework.frontend.openapi.entity;

import io.swagger.models.properties.Property;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import org.dotwebstack.framework.frontend.openapi.entity.builder.QueryResult;

public interface Entity<R extends org.eclipse.rdf4j.query.QueryResult<?>> {

  R getResult();

  Map<MediaType, Property> getSchemaMap();

  Property getSchemaProperty();

  QueryResult getQueryResult();

  String getBaseUri();

  String getEndpoint();

}
