package org.dotwebstack.framework.frontend.openapi.mappers;

import com.atlassian.oai.validator.model.ApiOperation;
import io.swagger.models.Operation;
import io.swagger.models.Swagger;
import java.util.Map;
import org.glassfish.jersey.server.model.Resource;

public interface RequestMapper {

  Boolean supportsVendorExtension(Map<String, Object> vendorExtensions);

  void map(Resource.Builder resourceBuilder, Swagger swagger, ApiOperation apiOperation,
      Operation getOperation, String absolutePath);

}
