package org.dotwebstack.framework.param.shapes;

import org.dotwebstack.framework.param.AbstractPropertyShape;
import org.dotwebstack.framework.param.types.IntTermParameter;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.springframework.stereotype.Component;

@Component
public class IntegerPropertyShape extends AbstractPropertyShape<Integer> {

  @Override
  public IRI getDataType() {
    return XMLSchema.INTEGER;
  }

  @Override
  public Class<?> getTermClass() {
    return IntTermParameter.class;
  }
}
