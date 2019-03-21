package org.dotwebstack.framework.backend.rdf4j.types;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.springframework.stereotype.Component;

@Component
class IntConverter implements LiteralConverter<Integer> {

  @Override
  public Integer convert(Literal value) {
    return value.intValue();
  }

  @Override
  public boolean supports(IRI dataType) {
    return XMLSchema.INT.equals(dataType);
  }

}
