package org.dotwebstack.framework.backend.rdf4j.types;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;

public interface LiteralConverter<T> {

  T convert(Literal value);

  boolean supports(IRI dataType);

}
