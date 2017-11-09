package org.dotwebstack.framework.frontend.openapi.entity;

import io.swagger.models.properties.Property;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import org.dotwebstack.framework.frontend.openapi.entity.builder.QueryResult;
import org.dotwebstack.framework.frontend.openapi.entity.builder.RequestParameters;

public final class TupleEntity extends QueryEntity {


  public TupleEntity(Map<MediaType, Property> schemaMap, Property schemaProperty,
      RequestParameters requestParameters, QueryResult queryResult, String baseUri,
      String endpoint) {
    super(schemaMap, schemaProperty, requestParameters, queryResult, baseUri, endpoint);
  }

}
