package org.dotwebstack.framework.frontend.openapi.mappers;

import com.atlassian.oai.validator.model.ApiOperation;
import io.swagger.v3.oas.models.OpenAPI;
import java.util.Map;
import org.glassfish.jersey.server.model.Resource;

public interface RequestMapper {

  Boolean supportsVendorExtension(Map<String, Object> vendorExtensions);

  void map(Resource.Builder resourceBuilder, OpenAPI openApi, ApiOperation apiOperation,
           String absolutePath);
}
