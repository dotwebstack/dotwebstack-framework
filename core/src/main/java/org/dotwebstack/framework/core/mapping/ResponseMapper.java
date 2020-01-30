package org.dotwebstack.framework.core.mapping;

import org.springframework.util.MimeType;

public interface ResponseMapper<T> {

  boolean supportsOutputMimeType(MimeType mimeType);

  boolean supportsInputObjectClass(Class clazz);

  String toResponse(T input);

}
