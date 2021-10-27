package org.dotwebstack.framework.core.backend.query;

import java.util.List;
import java.util.Map;
import org.dotwebstack.framework.core.model.ObjectField;

public interface ObjectFieldMapper<T> extends FieldMapper<T, Map<String, Object>> {

  void register(String name, FieldMapper<T, ?> fieldMapper);

  FieldMapper<T, ?> getFieldMapper(String name);

  ScalarFieldMapper<T> getLeafFieldMapper(List<ObjectField> fieldPath);
}
