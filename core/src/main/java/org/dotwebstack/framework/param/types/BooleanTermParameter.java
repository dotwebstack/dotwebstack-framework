package org.dotwebstack.framework.param.types;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class BooleanTermParameter extends TermParameter<Boolean> {

  public BooleanTermParameter(IRI identifier, String name, boolean required) {
    super(identifier, name, required);
  }

  public BooleanTermParameter(IRI identifier, String name, boolean required, boolean defaultValue) {
    super(identifier, name, required, defaultValue);
  }

  @Override
  public Literal getValue(Boolean value) {
    return SimpleValueFactory.getInstance().createLiteral(value);
  }

  @Override
  protected Boolean handleInner(String value) {
    return Boolean.valueOf(value);
  }

}
