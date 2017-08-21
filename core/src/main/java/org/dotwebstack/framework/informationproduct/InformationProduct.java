package org.dotwebstack.framework.informationproduct;

import java.util.Objects;
import org.dotwebstack.framework.backend.BackendSource;
import org.eclipse.rdf4j.model.IRI;

public class InformationProduct {

  private IRI identifier;

  private String label;

  private BackendSource backendSource;

  private InformationProduct(Builder builder) {
    identifier = builder.identifier;
    label = builder.label;
    backendSource = builder.backendSource;
  }

  public IRI getIdentifier() {
    return identifier;
  }

  public String getLabel() {
    return label;
  }

  public BackendSource getBackendSource() {
    return backendSource;
  }

  public static class Builder {

    private IRI identifier;

    private BackendSource backendSource;

    private String label;

    public Builder(IRI identifier, BackendSource backendSource) {
      this.identifier = Objects.requireNonNull(identifier);
      this.backendSource = Objects.requireNonNull(backendSource);
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
