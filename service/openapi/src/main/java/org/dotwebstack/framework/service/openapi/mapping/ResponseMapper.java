package org.dotwebstack.framework.service.openapi.mapping;

import org.dotwebstack.framework.service.openapi.response.ResponseWriteContext;
import org.springframework.http.MediaType;

public interface ResponseMapper {

  boolean accept(MediaType mediaType);

  String toResponse(ResponseWriteContext responseWriteContext);

}
