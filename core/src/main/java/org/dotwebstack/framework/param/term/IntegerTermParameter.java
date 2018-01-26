package org.dotwebstack.framework.param.term;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;

public class IntegerTermParameter extends TermParameter<Integer> {

  IntegerTermParameter(IRI identifier, String name, boolean required) {
    this(identifier, name, required, null);
  }

  IntegerTermParameter(IRI identifier, String name, boolean required, Integer defaultValue) {
    super(identifier, name, required, defaultValue);
  }

  @Override
  public Literal getValue(Integer value) {
    return FACTORY.createLiteral(value);
  }

  @Override
  protected Integer handleInner(String value) {
    return Integer.parseInt(value);
  }
}
