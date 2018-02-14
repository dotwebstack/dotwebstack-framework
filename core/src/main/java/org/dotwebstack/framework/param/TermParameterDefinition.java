package org.dotwebstack.framework.param;

import static org.dotwebstack.framework.param.term.TermParameterFactory.newTermParameter;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import org.dotwebstack.framework.param.term.TermParameter;
import org.eclipse.rdf4j.model.Resource;

public final class TermParameterDefinition extends AbstractParameterDefinition<TermParameter<?>> {

  @Getter(AccessLevel.PACKAGE)
  private final ShaclShape shaclShape;

  public TermParameterDefinition(@NonNull Resource identifier, @NonNull String name,
      @NonNull ShaclShape shaclShape) {
    super(identifier, name);

    this.shaclShape = shaclShape;
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
    return newTermParameter(getIdentifier(), getName(), shaclShape, required);

  }

}
