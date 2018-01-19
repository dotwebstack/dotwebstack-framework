package org.dotwebstack.framework.param.shapes;

import org.dotwebstack.framework.param.AbstractPropertyShape;
import org.dotwebstack.framework.param.types.StringTermParameter;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.springframework.stereotype.Component;

@Component
public class StringPropertyShape extends AbstractPropertyShape<String> {

  @Override
  public IRI getDataType() {
    return XMLSchema.STRING;
  }

  @Override
  public Class<?> getTermClass() {
    return StringTermParameter.class;
  }
}
