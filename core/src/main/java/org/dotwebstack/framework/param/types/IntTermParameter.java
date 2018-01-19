package org.dotwebstack.framework.param.types;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class IntTermParameter extends TermParameter<Integer> {

  public IntTermParameter(IRI identifier, String name, boolean required, String defaultValue) {
    super(identifier, name, required, defaultValue);
  }

  @Override
  public Literal getValue(Integer value) {
    return SimpleValueFactory.getInstance().createLiteral(value);
  }

  @Override
  protected Integer handleInner(String value) {
    if (value != null) {
      return Integer.parseInt(value);
    } else {
      return null;
    }
  }
}
