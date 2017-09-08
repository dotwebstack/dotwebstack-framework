package org.dotwebstack.framework.informationproduct;

import java.util.Objects;
import org.dotwebstack.framework.backend.ResultType;
import org.dotwebstack.framework.config.ConfigurationException;
import org.eclipse.rdf4j.model.IRI;

public class InformationProduct {

  private IRI identifier;

  private String label;

  protected InformationProduct() {

  }

  private InformationProduct(Builder builder) {
    identifier = builder.identifier;
    label = builder.label;
  }

  public IRI getIdentifier() {
    return identifier;
  }

  public String getLabel() {
    return label;
  }

  public Object getResult() {
    throw new ConfigurationException("Result cannot be determined.");
  }

  public ResultType getResultType() {
    throw new ConfigurationException("Result type cannot be determined.");
  }

  public static class Builder {

    private IRI identifier;

    private String label;

    public Builder(IRI identifier) {
      this.identifier = Objects.requireNonNull(identifier);
    }

    public Builder label(String label) {
      this.label = label;
      return this;
    }

    public InformationProduct build() {
      return new InformationProduct(this);
    }

  }

}
