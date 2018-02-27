package org.dotwebstack.framework.frontend.ld.endpoint;

import lombok.NonNull;
import org.dotwebstack.framework.frontend.http.stage.Stage;
import org.dotwebstack.framework.frontend.ld.parameter.ParameterMapper;
import org.eclipse.rdf4j.model.Resource;

public class EndPoint {

  private Resource identifier;

  private String pathPattern;

  private String label;

  private Stage stage;

  private ParameterMapper parameterMapper;

  private EndPoint(Builder builder) {
    identifier = builder.identifier;
    pathPattern = builder.pathPattern;
    label = builder.label;
    stage = builder.stage;
    parameterMapper = builder.parameterMapper;
  }

  public Resource getIdentifier() {
    return identifier;
  }

  public String getLabel() {
    return label;
  }

  public Stage getStage() {
    return stage;
  }

  public ParameterMapper getParameterMapper() {
    return parameterMapper;
  }

  public String getPathPattern() {
    return pathPattern;
  }

  public static class Builder {

    private Resource identifier;

    private String pathPattern;

    private String label;

    private Stage stage;

    private ParameterMapper parameterMapper;

    public Builder(@NonNull Resource identifier, @NonNull String pathPattern) {
      this.identifier = identifier;
      this.pathPattern = pathPattern;
    }

    public Builder label(@NonNull String label) {
      this.label = label;
      return this;
    }

    public Builder stage(@NonNull Stage stage) {
      this.stage = stage;
      return this;
    }

    public Builder parameterMapper(@NonNull ParameterMapper parameterMapper) {
      this.parameterMapper = parameterMapper;
      return this;
    }

    public EndPoint build() {
      return new EndPoint(this);
    }

  }

}
