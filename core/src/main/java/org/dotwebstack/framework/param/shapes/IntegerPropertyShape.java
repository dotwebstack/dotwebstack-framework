package org.dotwebstack.framework.param.shapes;

import org.dotwebstack.framework.param.PropertyShape;
import org.dotwebstack.framework.param.types.IntTermParameter;

public class IntegerPropertyShape implements PropertyShape {

  @Override
  public String getDataType() {
    return "http://www.w3.org/2001/XMLSchema#int";
  }

  @Override
  public Class<?> getTermClass() {
    return IntTermParameter.class;
  }
}
