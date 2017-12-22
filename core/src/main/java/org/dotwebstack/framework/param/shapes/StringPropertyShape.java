package org.dotwebstack.framework.param.shapes;

import org.dotwebstack.framework.param.PropertyShape;

public class StringPropertyShape implements PropertyShape {

  @Override
  public String getDataType() {
    return "http://www.w3.org/2001/XMLSchema#string";
  }

  @Override
  public Class<String> getJavaType() {
    return String.class;
  }
}
