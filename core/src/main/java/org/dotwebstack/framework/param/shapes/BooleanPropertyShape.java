package org.dotwebstack.framework.param.shapes;

import org.dotwebstack.framework.param.PropertyShape;
import org.dotwebstack.framework.param.types.BooleanTermParameter;

public class BooleanPropertyShape implements PropertyShape {

  @Override
  public String getDataType() {
    return "http://www.w3.org/2001/XMLSchema#boolean";
  }

  @Override
  public Class<?> getTermClass() {
    return BooleanTermParameter.class;
  }
}
