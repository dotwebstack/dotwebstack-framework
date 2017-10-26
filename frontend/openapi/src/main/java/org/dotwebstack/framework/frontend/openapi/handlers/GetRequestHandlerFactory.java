package org.dotwebstack.framework.frontend.openapi.handlers;

import io.swagger.models.Operation;
import io.swagger.models.properties.Property;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import lombok.NonNull;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.springframework.stereotype.Component;

@Component
public class GetRequestHandlerFactory {

  private final FilterNameToParameterValueMapper filterNameToParameterValueMapper;

  public GetRequestHandlerFactory(
      @NonNull FilterNameToParameterValueMapper filterNameToParameterValueMapper) {
    this.filterNameToParameterValueMapper = filterNameToParameterValueMapper;

  }

  public GetRequestHandler newGetRequestHandler(@NonNull Operation operation,
      @NonNull InformationProduct informationProduct, @NonNull Map<MediaType, Property> schemaMap) {
    return new GetRequestHandler(operation, informationProduct, schemaMap,
        filterNameToParameterValueMapper);
  }

}
