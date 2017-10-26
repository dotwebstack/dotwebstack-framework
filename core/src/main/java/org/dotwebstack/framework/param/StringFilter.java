package org.dotwebstack.framework.param;

import lombok.NonNull;
import org.eclipse.rdf4j.model.IRI;

public class StringFilter extends AbstractParameter {

  public StringFilter(IRI identifier, String name) {
    super(identifier, name);
  }

  @Override
  public String handle(String value, @NonNull String query) {
    return query.replaceAll(String.format("\\$\\{%s}", getName()), value);
  }

}
