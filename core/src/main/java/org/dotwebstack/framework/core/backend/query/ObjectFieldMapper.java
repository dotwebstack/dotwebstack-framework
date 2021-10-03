package org.dotwebstack.framework.core.backend.query;

public interface ObjectFieldMapper<T> extends FieldMapper<T> {

  void register(String name, FieldMapper<T> fieldMapper);

  FieldMapper<T> getFieldMapper(String name);
}
