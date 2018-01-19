package org.dotwebstack.framework.param.shapes;

import org.dotwebstack.framework.param.AbstractPropertyShape;
import org.dotwebstack.framework.param.types.IriTermParameter;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.springframework.stereotype.Component;

@Component
public class IriPropertyShape extends AbstractPropertyShape<IRI> {

  @Override
  public IRI getDataType() {
    return XMLSchema.ANYURI;
  }

  @Override
  public Class<?> getTermClass() {
    return IriTermParameter.class;
  }

}
