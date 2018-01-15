package org.dotwebstack.framework.param.types;

import java.util.Map;
import org.dotwebstack.framework.backend.BackendException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class IriTermParameter extends TermParameter<IRI> {

  public IriTermParameter(IRI identifier, String name, boolean required) {
    super(identifier, name, required);
  }

  @Override
  protected IRI parseValue(Map<String, String> parameterValues) {
    return SimpleValueFactory.getInstance().createIRI(parameterValues.get(getName()));
  }

  @Override
  public Literal getValue(IRI value) {
    return SimpleValueFactory.getInstance().createLiteral(value.stringValue());
  }

  @Override
  protected void validateRequired(Map<String, String> parameterValues) {
    try {
      parseValue(parameterValues);
    } catch (NumberFormatException | NullPointerException e) {
      throw new BackendException(
          String.format("No value found for required parameter '%s'. Supplied parameterValues: %s",
              getIdentifier(), parameterValues));
    }
  }

}
