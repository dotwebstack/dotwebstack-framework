package org.dotwebstack.framework.param.shapes;

import org.dotwebstack.framework.param.AbstractPropertyShape;
import org.dotwebstack.framework.param.types.BooleanTermParameter;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.springframework.stereotype.Component;

@Component
public class BooleanPropertyShape extends AbstractPropertyShape<Boolean> {

  @Override
  public IRI getDataType() {
    return XMLSchema.BOOLEAN;
  }

  @Override
  public Class<?> getTermClass() {
    return BooleanTermParameter.class;
  }
}
