package org.dotwebstack.framework.param;

import java.util.Optional;
import lombok.NonNull;
import org.eclipse.rdf4j.model.IRI;

public final class ParameterDefinition {

  private final IRI identifier;

  private final String name;

  private Optional<PropertyShape> shapeType;

  public ParameterDefinition(@NonNull IRI identifier, @NonNull String name,
      @NonNull Optional<PropertyShape> shapeType) {
    this.identifier = identifier;
    this.name = name;
    this.shapeType = shapeType;
  }

  public IRI getIdentifier() {
    return identifier;
  }

  public String getName() {
    return name;
  }

  public Optional<PropertyShape> getShapeTypes() {
    return this.shapeType;
  }
}
