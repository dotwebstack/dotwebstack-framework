package org.dotwebstack.framework.backend.rdf4j.types;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.springframework.stereotype.Component;

@Component
class StringConverter implements LiteralConverter<String> {

  @Override
  public String convert(Literal value) {
    return value.stringValue();
  }

  @Override
  public boolean supports(IRI dataType) {
    return XMLSchema.STRING.equals(dataType);
  }

}
