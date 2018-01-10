package org.dotwebstack.framework.param;

import lombok.NonNull;
import org.eclipse.rdf4j.model.IRI;

abstract class AbstractParameterDefinition<T extends Parameter> implements ParameterDefinition<T> {

  private final IRI identifier;

  private final String name;

  protected AbstractParameterDefinition(@NonNull IRI identifier, @NonNull String name) {
    this.identifier = identifier;
    this.name = name;
  }

  public IRI getIdentifier() {
    return identifier;
  }

  public String getName() {
    return name;
  }

}
