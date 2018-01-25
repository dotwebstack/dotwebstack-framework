package org.dotwebstack.framework.param;

import static org.dotwebstack.framework.param.term.TermParameterFactory.getTermParameter;

import lombok.NonNull;
import org.dotwebstack.framework.param.term.TermParameter;
import org.eclipse.rdf4j.model.IRI;

public final class TermParameterDefinition<T> extends
    AbstractParameterDefinition<TermParameter<?>> {

  private final PropertyShape shapeType;

  public TermParameterDefinition(@NonNull IRI identifier, @NonNull String name,
      @NonNull PropertyShape shapeType) {
    super(identifier, name);

    this.shapeType = shapeType;
  }

  @Override
  public TermParameter<?> createOptionalParameter() {
    return createParameter(false);
  }

  @Override
  public TermParameter<?> createRequiredParameter() {
    return createParameter(true);
  }

  private TermParameter<?> createParameter(boolean required) {
    return getTermParameter(getIdentifier(), getName(), shapeType, required);
  }

}
