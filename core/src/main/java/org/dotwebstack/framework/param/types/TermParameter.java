package org.dotwebstack.framework.param.types;

import java.util.Map;
import org.dotwebstack.framework.backend.BackendException;
import org.dotwebstack.framework.param.AbstractParameter;
import org.dotwebstack.framework.param.BindableParameter;
import org.eclipse.rdf4j.model.IRI;

public abstract class TermParameter<T> extends AbstractParameter<T>
    implements BindableParameter<T> {

  protected TermParameter(IRI identifier, String name, boolean required) {
    super(identifier, name, required);
  }

  @Override
  protected T handleInner(Map<String, String> parameterValues) {
    return parseValue(parameterValues);
  }

  @Override
  protected void validateRequired(Map<String, String> parameterValues) {
    if (parseValue(parameterValues) == null) {
      throw new BackendException(
          String.format("No value found for required parameter '%s'. Supplied parameterValues: %s",
              getIdentifier(), parameterValues));
    }
  }

}
