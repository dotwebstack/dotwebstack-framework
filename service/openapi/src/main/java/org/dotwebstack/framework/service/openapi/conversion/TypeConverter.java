package org.dotwebstack.framework.service.openapi.conversion;

import java.util.Map;

public interface TypeConverter<S, T> {

  boolean supports(Object object);

  T convert(S source, Map<String, Object> context);

}
