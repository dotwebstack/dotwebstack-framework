package org.dotwebstack.framework.frontend.http.layout;

import java.util.HashMap;
import java.util.Map;
import lombok.NonNull;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;

public class Layout {

  private IRI identifier;

  private Map<IRI, Value> options;

  private String label;

  private Layout(Builder builder) {
    this.identifier = builder.identifier;
    this.label = builder.label;
    this.options = builder.options;
  }

  public IRI getIdentifier() {
    return identifier;
  }

  public String getLabel() {
    return label;
  }

  public Map<IRI, Value> getOptions() {
    return options;
  }

  public void addOption(@NonNull IRI key, @NonNull Value value) {
    options.put(key, value);
  }

  public static class Builder {

    private IRI identifier;

    private String label;

    private Map<IRI, Value> options;

    public Builder(@NonNull IRI identifier) {
      this.identifier = identifier;
      this.options = new HashMap<>();
    }

    public Builder options(@NonNull Map<IRI, Value> options) {
      this.options = options;
      return this;
    }

    public Builder label(@NonNull String label) {
      this.label = label;
      return this;
    }

    public Layout build() {
      return new Layout(this);
    }

    public Builder option(@NonNull IRI key, @NonNull Value value) {
      this.options.put(key, value);
      return this;
    }
  }
}
