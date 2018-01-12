package org.dotwebstack.framework.param.shapes;

import org.dotwebstack.framework.param.PropertyShape;
import org.dotwebstack.framework.param.types.IriTermParameter;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.springframework.stereotype.Component;

@Component
public class IriPropertyShape implements PropertyShape {

  @Override
  public IRI getDataType() {
    return XMLSchema.ANYURI;
  }

  @Override
  public Class<?> getTermClass() {
    return IriTermParameter.class;
  }

}
