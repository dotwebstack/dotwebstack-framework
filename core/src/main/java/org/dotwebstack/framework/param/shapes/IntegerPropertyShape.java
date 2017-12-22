package org.dotwebstack.framework.param.shapes;

import org.dotwebstack.framework.param.PropertyShape;

public class IntegerPropertyShape implements PropertyShape {

  @Override
  public String getDataType() {
    return "http://www.w3.org/2001/XMLSchema#int";
  }

  @Override
  public Class<?> getJavaType() {
    return Integer.class;
  }
}
