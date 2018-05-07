package org.dotwebstack.framework.rml;

import lombok.NonNull;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;

public class RmlMapping {

  private Resource identifier;

  private Model model;

  private String streamName;

  public RmlMapping(@NonNull Builder builder) {
    this.identifier = builder.identifier;
    this.model = builder.model;
    this.streamName = builder.streamName;
  }

  public Resource getIdentifier() {
    return identifier;
  }

  public Model getModel() {
    return model;
  }

  public String getStreamName() {
    return streamName;
  }

  public static final class Builder {

    private Resource identifier;

    private Model model;

    private String streamName;

    public Builder(@NonNull Resource identifier) {
      this.identifier = identifier;
    }

    public Builder model(@NonNull Model model) {
      this.model = model;
      return this;
    }

    public Builder streamName(@NonNull String streamName) {
      this.streamName = streamName;
      return this;
    }

    public RmlMapping build() {
      return new RmlMapping(this);
    }
  }

}
