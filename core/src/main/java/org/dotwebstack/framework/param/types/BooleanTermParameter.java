package org.dotwebstack.framework.param.types;

import java.util.Map;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class BooleanTermParameter extends TermParameter<Boolean> {

  public BooleanTermParameter(IRI identifier, String name, boolean required) {
    super(identifier, name, required);
  }

  @Override
  protected Boolean parseValue(Map<String, String> parameterValues) {
    return Boolean.valueOf(parameterValues.get(getName()));
  }

  @Override
  public Literal getValue(Boolean value) {
    return SimpleValueFactory.getInstance().createLiteral(value);
  }

}
