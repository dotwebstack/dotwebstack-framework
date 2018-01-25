package org.dotwebstack.framework.param.term;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class IntegerTermParameter extends TermParameter<Integer> {

  public IntegerTermParameter(IRI identifier, String name, boolean required) {
    super(identifier, name, required, null);
  }

  public IntegerTermParameter(IRI identifier, String name, boolean required, Integer defaultValue) {
    super(identifier, name, required, defaultValue);
  }

  @Override
  public Literal getValue(Integer value) {
    return SimpleValueFactory.getInstance().createLiteral(value);
  }

  @Override
  protected Integer handleInner(String value) {

    // XXX (PvH) Volgens mij is de null check niet meer noodzakelijk toch?
    if (value != null) {
      return Integer.parseInt(value);
    } else {
      return null;
    }
  }
}
