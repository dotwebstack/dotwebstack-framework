package org.dotwebstack.framework.core.backend.query;

import java.util.Map;

public interface ObjectFieldMapper<T> extends FieldMapper<T, Map<String, Object>> {

  void register(String name, FieldMapper<T, ?> fieldMapper);

  FieldMapper<T, ?> getFieldMapper(String name);
}
