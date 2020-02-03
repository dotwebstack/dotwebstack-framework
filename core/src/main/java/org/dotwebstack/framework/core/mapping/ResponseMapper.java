package org.dotwebstack.framework.core.mapping;

import org.springframework.util.MimeType;

public interface ResponseMapper {

  boolean supportsOutputMimeType(MimeType mimeType);

  boolean supportsInputObjectClass(Class<?> clazz);

  String toResponse(Object input);

}
