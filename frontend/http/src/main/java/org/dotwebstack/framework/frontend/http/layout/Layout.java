package org.dotwebstack.framework.frontend.http.layout;

import lombok.NonNull;
import org.eclipse.rdf4j.model.IRI;

public class Layout {

  private IRI identifier;

  private String cssResource;

  private String label;

  private Layout(Builder builder) {
    this.identifier = builder.identifier;
    this.label = builder.label;
    this.cssResource = builder.cssResource;
  }

  public IRI getIdentifier() {
    return identifier;
  }

  public String getCssResource() {
    return cssResource;
  }

  public String getLabel() {
    return label;
  }

  public static class Builder {

    private IRI identifier;

    private String cssResource;

    private String label;

    public Builder(@NonNull IRI identifier, @NonNull String cssResource) {
      this.identifier = identifier;
      this.cssResource = cssResource;
    }

    public Builder label(@NonNull String label) {
      this.label = label;
      return this;
    }

    public Layout build() {
      return new Layout(this);
    }
  }
}
