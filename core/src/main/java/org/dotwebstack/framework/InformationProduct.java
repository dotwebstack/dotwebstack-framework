package org.dotwebstack.framework;

import java.util.Objects;
import java.util.Optional;
import org.eclipse.rdf4j.model.IRI;

class InformationProduct {

  private IRI identifier;

  private String label;

  private InformationProduct(Builder builder) {
    this.identifier = builder.identifier;
    this.label = builder.label;
  }

  public IRI getIdentifier() {
    return identifier;
  }

  public String getLabel() {
    return label;
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

    public Builder label(Optional<String> label) {
      if (label.isPresent()) {
        this.label = label.get();
      }

      return this;
    }

    public InformationProduct build() {
      return new InformationProduct(this);
    }

  }

}
