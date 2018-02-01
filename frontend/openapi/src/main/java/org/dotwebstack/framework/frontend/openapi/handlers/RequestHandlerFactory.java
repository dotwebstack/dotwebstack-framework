package org.dotwebstack.framework.frontend.openapi.handlers;

import com.atlassian.oai.validator.model.ApiOperation;
import io.swagger.models.Swagger;
import io.swagger.models.properties.Property;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.SwaggerUtils;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.springframework.stereotype.Service;

@Service
public class RequestHandlerFactory {

  private final RequestParameterMapper requestParameterMapper;

  private final RequestParameterExtractor requestParameterExtractor;

  public RequestHandlerFactory(@NonNull RequestParameterMapper requestParameterMapper,
      @NonNull RequestParameterExtractor requestParameterExtractor) {
    this.requestParameterMapper = requestParameterMapper;
    this.requestParameterExtractor = requestParameterExtractor;
  }

  public RequestHandler newRequestHandler(@NonNull ApiOperation apiOperation,
      @NonNull InformationProduct informationProduct, @NonNull Map<MediaType, Property> schemaMap,
      @NonNull Swagger swagger) {
    return new RequestHandler(apiOperation, informationProduct, schemaMap, requestParameterMapper,
        new ApiRequestValidator(SwaggerUtils.createValidator(swagger), requestParameterExtractor),
        swagger);
  }

}
