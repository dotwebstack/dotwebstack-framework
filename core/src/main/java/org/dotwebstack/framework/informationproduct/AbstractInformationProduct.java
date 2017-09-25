package org.dotwebstack.framework.informationproduct;

import lombok.NonNull;
import org.dotwebstack.framework.backend.ResultType;
import org.eclipse.rdf4j.model.IRI;

public abstract class AbstractInformationProduct implements InformationProduct {

  protected final IRI identifier;

  protected final String label;

  protected final ResultType resultType;

  protected AbstractInformationProduct(@NonNull IRI identifier, String label,
      @NonNull ResultType resultType) {
    this.identifier = identifier;
    this.resultType = resultType;
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
