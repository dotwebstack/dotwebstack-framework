package org.dotwebstack.framework.core.backend.query;

public interface ScalarFieldMapper<T> extends FieldMapper<T, Object> {

  String getAlias();
}
