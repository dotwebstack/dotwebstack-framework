package org.dotwebstack.framework.filter;

import lombok.NonNull;
import org.eclipse.rdf4j.model.IRI;

public class StringFilter extends AbstractFilter {

  public StringFilter(IRI identifier, String name) {
    super(identifier, name);
  }

  @Override
  public String filter(String value, @NonNull String query) {
    return query.replaceAll(String.format("\\$\\{%s}", getName()), value);
  }

}
