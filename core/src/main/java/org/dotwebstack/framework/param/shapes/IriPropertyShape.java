package org.dotwebstack.framework.param.shapes;

import org.dotwebstack.framework.param.PropertyShape;
import org.dotwebstack.framework.param.types.StringTermParameter;
import org.eclipse.rdf4j.model.IRI;

public class IriPropertyShape implements PropertyShape {

  @Override
  public String getDataType() {
    return "http://www.w3.org/2001/XMLSchema#anyURI";
  }

  @Override
  public Class<?> getTermClass() {
    return StringTermParameter.class;
  }
}
