package org.dotwebstack.framework.param;

public interface PropertyShape {

  String getDataType();

  Class<?> getJavaType();

  Class<?> getTermClass();
}
