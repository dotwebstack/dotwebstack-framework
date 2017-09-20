package org.dotwebstack.framework.informationproduct;

import java.util.Objects;
import org.dotwebstack.framework.backend.ResultType;
import org.eclipse.rdf4j.model.IRI;

public abstract class AbstractInformationProduct implements InformationProduct {

  protected final IRI identifier;

  protected final String label;

  protected final ResultType resultType;

  protected AbstractInformationProduct(IRI identifier, String label, ResultType resultType) {
    this.identifier = Objects.requireNonNull(identifier);
    this.resultType = Objects.requireNonNull(resultType);
    this.label = label;
  }

  @Override
  public IRI getIdentifier() {
    return identifier;
  }

  @Override
  public String getLabel() {
    return label;
  }

  @Override
  public ResultType getResultType() {
    return resultType;
  }
}
