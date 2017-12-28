package org.dotwebstack.framework.param.shapes;

import org.dotwebstack.framework.param.PropertyShape;

public class BooleanPropertyShape implements PropertyShape {

  @Override
  public String getDataType() {
    return "http://www.w3.org/2001/XMLSchema#boolean";
  }

  @Override
  public Class<?> getJavaType() {
    return Boolean.class;
  }
}
