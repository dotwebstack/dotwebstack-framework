package org.dotwebstack.framework.param.shapes;

import org.dotwebstack.framework.param.PropertyShape;
import org.dotwebstack.framework.param.types.StringTermParameter;

public class StringPropertyShape implements PropertyShape {

  @Override
  public String getDataType() {
    return "http://www.w3.org/2001/XMLSchema#string";
  }

  @Override
  public Class<?> getTermClass() {
    return StringTermParameter.class;
  }
}
