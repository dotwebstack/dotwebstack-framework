package org.dotwebstack.framework.frontend.openapi.handlers;

import com.atlassian.oai.validator.model.ApiOperation;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.models.Swagger;
import io.swagger.models.properties.Property;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.SwaggerUtils;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.springframework.stereotype.Component;

@Component
public class GetRequestHandlerFactory {

  private final RequestParameterMapper requestParameterMapper;

  public GetRequestHandlerFactory(@NonNull RequestParameterMapper requestParameterMapper) {
    this.requestParameterMapper = requestParameterMapper;

  }

  public GetRequestHandler newGetRequestHandler(@NonNull ApiOperation apiOperation,
      @NonNull InformationProduct informationProduct, @NonNull Map<MediaType, Property> schemaMap,
      @NonNull Swagger swagger) {
    // XX (PvH) Waarom laat je het creëren van de ApiRequestValidator en de ObjectMapper niet aan
    // Spring over?
    return new GetRequestHandler(apiOperation, informationProduct, schemaMap,
        requestParameterMapper,
        new ApiRequestValidator(SwaggerUtils.createValidator(swagger), new ObjectMapper()),
        swagger);
  }

}
