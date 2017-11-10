package org.dotwebstack.framework.frontend.openapi.entity;

import io.swagger.models.properties.Property;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import org.dotwebstack.framework.frontend.openapi.entity.builder.QueryResult;
import org.dotwebstack.framework.frontend.openapi.entity.builder.RequestParameters;


public interface Entity {

  Property getSchemaProperty();

  RequestParameters getRequestParameters();

  QueryResult getQueryResult();

  String getBaseUri();

  String getEndpoint();

}
