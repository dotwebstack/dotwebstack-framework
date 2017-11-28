package org.dotwebstack.framework.frontend.openapi.entity;

import io.swagger.models.properties.Property;
import java.util.Map;
import javax.ws.rs.core.MediaType;

public interface Entity {

  Map<MediaType, Property> getSchemaMap();

}
