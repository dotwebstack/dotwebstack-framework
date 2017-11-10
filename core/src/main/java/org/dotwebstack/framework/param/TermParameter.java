package org.dotwebstack.framework.param;

import java.util.Map;
import lombok.NonNull;
import org.eclipse.rdf4j.model.IRI;

public class TermParameter extends AbstractParameter<String> {

  private TermParameter(IRI identifier, String name, boolean required) {
    super(identifier, name, required);
  }

  public static TermParameter requiredTermParameter(IRI identifier, String name) {
    return new TermParameter(identifier, name, true);
  }

  public static TermParameter optionalTermParameter(IRI identifier, String name) {
    return new TermParameter(identifier, name, false);
  }

  // TODO validate() should check if return value is of type String
  @Override
  public String handle(@NonNull Map<String, Object> parameterValues) {
    return (String) parameterValues.get(getName());
  }

}
