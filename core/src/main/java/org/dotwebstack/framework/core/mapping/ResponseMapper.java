package org.dotwebstack.framework.core.mapping;

import org.springframework.util.MimeType;
import reactor.core.publisher.Mono;

public interface ResponseMapper {

  boolean supportsOutputMimeType(MimeType mimeType);

  boolean supportsInputObjectClass(Class<?> clazz);

  Mono<String> toResponse(Object input, Object context);
}
