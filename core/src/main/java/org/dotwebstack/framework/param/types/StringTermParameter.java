package org.dotwebstack.framework.param.types;

import java.util.Map;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class StringTermParameter extends TermParameter<String> {

  public StringTermParameter(IRI identifier, String name, boolean required) {
    super(identifier, name, required);
  }

  @Override
  protected String parseValue(Map<String, String> parameterValues) {
    return parameterValues.get(getName());
  }

  @Override
  public Literal getValue(String value) {
    return SimpleValueFactory.getInstance().createLiteral(value);
  }

}
