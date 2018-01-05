package org.dotwebstack.framework.param;

import lombok.NonNull;
import org.eclipse.rdf4j.model.IRI;

public class ParameterDefinition {

  private final IRI identifier;

  private final String name;


  public ParameterDefinition(@NonNull IRI identifier, @NonNull String name) {
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
