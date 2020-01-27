package org.dotwebstack.framework.service.openapi.mapping;

import org.springframework.http.MediaType;

public interface responseMapper<T> {

  boolean accept(MediaType mediaType);

  String toResponse(T input);

}
