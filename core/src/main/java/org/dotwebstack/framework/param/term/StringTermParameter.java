package org.dotwebstack.framework.param.term;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;

public class StringTermParameter extends TermParameter<String> {

  public StringTermParameter(IRI identifier, String name, boolean required) {
    this(identifier, name, required, null);
  }

  StringTermParameter(IRI identifier, String name, boolean required, String defaultValue) {
    super(identifier, name, required, defaultValue);
  }

  @Override
  public Literal getValue(String value) {
    return FACTORY.createLiteral(value);
  }

  @Override
  protected String handleInner(String value) {
    return value;
  }

}
