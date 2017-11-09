package org.dotwebstack.framework.param;

import java.util.Map;
import lombok.NonNull;
import org.eclipse.rdf4j.model.IRI;

public class StringFilter extends AbstractParameter {

  public StringFilter(IRI identifier, String name) {
    super(identifier, name);
  }

  @Override
  public Object handle(@NonNull Map<String, Object> parameterValues) {
    return parameterValues.get(getName());
  }

}
