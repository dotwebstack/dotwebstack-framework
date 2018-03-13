package org.dotwebstack.framework.frontend.ld.endpoint;

import lombok.NonNull;
import org.dotwebstack.framework.frontend.ld.parameter.ParameterMapper;
import org.eclipse.rdf4j.model.Resource;

public class DynamicEndPoint extends AbstractEndPoint {

  private ParameterMapper parameterMapper;

  private DynamicEndPoint(Builder builder) {
    super(builder);
    this.parameterMapper = builder.parameterMapper;
  }

  public ParameterMapper getParameterMapper() {
    return parameterMapper;
  }

  public static class Builder extends EndPointBuilder<Builder> {

    private ParameterMapper parameterMapper;

    public Builder(@NonNull Resource identifier, @NonNull String pathPattern) {
      super(identifier, pathPattern);
    }

    public Builder parameterMapper(@NonNull ParameterMapper parameterMapper) {
      this.parameterMapper = parameterMapper;
      return this;
    }

    public DynamicEndPoint build() {
      return new DynamicEndPoint(this);
    }
  }

}
