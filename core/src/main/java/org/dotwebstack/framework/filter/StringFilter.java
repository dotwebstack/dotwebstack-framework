package org.dotwebstack.framework.filter;

import lombok.NonNull;
import org.eclipse.rdf4j.model.IRI;

public class StringFilter implements Filter {

  private final IRI identifier;

  private final String name;

  StringFilter(@NonNull IRI identifier, @NonNull String name) {
    this.identifier = identifier;
    this.name = name;
  }

  @Override
  public IRI getIdentifier() {
    return identifier;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String filter(String value, @NonNull String query) {
    return query.replaceAll(String.format("\\$\\{\\Q%s\\E\\}", name), value);
  }

}
