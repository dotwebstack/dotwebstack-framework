package org.dotwebstack.framework.frontend.openapi.entity;

import io.swagger.models.properties.Property;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.entity.builder.QueryResult;
import org.dotwebstack.framework.frontend.openapi.entity.builder.RequestParameters;
import org.eclipse.rdf4j.query.TupleQueryResult;

public final class TupleEntity extends AbstractEntity {


  public TupleEntity(Map<MediaType, Property> schemaMap,
      Property schemaProperty, RequestParameters requestParameters,
      QueryResult queryResult, String baseUri, String endpoint) {
    super(schemaMap, schemaProperty, requestParameters,
        queryResult, baseUri, endpoint);



  }

}
