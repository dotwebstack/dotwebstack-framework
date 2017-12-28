package org.dotwebstack.framework.frontend.http.layout;

import lombok.NonNull;
import org.eclipse.rdf4j.model.IRI;
import org.springframework.core.io.Resource;

public class Layout {

  private IRI identifier;

  private Resource cssResource;

  private String label;

  private Layout(Builder builder) {
    this.identifier = builder.identifier;
    this.label = builder.label;
    this.cssResource = builder.cssResource;
  }

  public static class Builder {

    private IRI identifier;

    private Resource cssResource;

    private String label;

    public Builder(@NonNull IRI identifier, @NonNull Resource cssResource) {
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
