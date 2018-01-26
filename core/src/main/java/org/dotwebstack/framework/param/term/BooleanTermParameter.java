package org.dotwebstack.framework.param.term;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;

public class BooleanTermParameter extends TermParameter<Boolean> {

  BooleanTermParameter(IRI identifier, String name, boolean required) {
    this(identifier, name, required, null);
  }

  BooleanTermParameter(IRI identifier, String name, boolean required, Boolean defaultValue) {
    super(identifier, name, required, defaultValue);
  }

  @Override
  public Literal getValue(Boolean value) {
    return FACTORY.createLiteral(value);
  }

  @Override
  protected Boolean handleInner(String value) {
    return Boolean.valueOf(value);
  }
}
