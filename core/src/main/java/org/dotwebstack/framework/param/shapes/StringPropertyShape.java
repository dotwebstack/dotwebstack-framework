package org.dotwebstack.framework.param.shapes;

import org.dotwebstack.framework.param.PropertyShape;
import org.dotwebstack.framework.param.types.StringTermParameter;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;

public class StringPropertyShape implements PropertyShape {

  @Override
  public IRI getDataType() {
    return XMLSchema.STRING;
  }

  @Override
  public Class<?> getTermClass() {
    return StringTermParameter.class;
  }
}
