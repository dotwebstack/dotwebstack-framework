package org.dotwebstack.framework.param;

import java.util.Map;
import lombok.NonNull;
import org.eclipse.rdf4j.model.IRI;

public class TermParameter extends AbstractParameter<String> {

  public TermParameter(IRI identifier, String name) {
    super(identifier, name);
  }

  // TODO validate() should check if return value is of type String
  @Override
  public String handle(@NonNull Map<String, Object> parameterValues) {
    return (String) parameterValues.get(getName());
  }

}
