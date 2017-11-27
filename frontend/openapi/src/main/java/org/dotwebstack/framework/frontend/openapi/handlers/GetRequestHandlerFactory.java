package org.dotwebstack.framework.frontend.openapi.handlers;

import io.swagger.models.Operation;
import io.swagger.models.Swagger;
import io.swagger.models.properties.Property;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import lombok.NonNull;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.springframework.stereotype.Service;

@Service
public class GetRequestHandlerFactory {

  private final RequestParameterMapper requestParameterMapper;

  public GetRequestHandlerFactory(@NonNull RequestParameterMapper requestParameterMapper) {
    this.requestParameterMapper = requestParameterMapper;

  }

  public GetRequestHandler newGetRequestHandler(@NonNull Operation operation,
      @NonNull InformationProduct informationProduct, @NonNull Map<MediaType, Property> schemaMap,
      @NonNull Swagger swagger) {
    return new GetRequestHandler(operation, informationProduct, schemaMap, requestParameterMapper,
        swagger);
  }

}
