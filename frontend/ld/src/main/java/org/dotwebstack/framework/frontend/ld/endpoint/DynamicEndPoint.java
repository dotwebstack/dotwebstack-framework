package org.dotwebstack.framework.frontend.ld.endpoint;

import lombok.NonNull;
import org.dotwebstack.framework.frontend.ld.parameter.ParameterMapper;
import org.eclipse.rdf4j.model.Resource;

public class DynamicEndPoint extends EndPoint {

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

    public Builder(@NonNull Resource identifier, @NonNull String pathPattern,
        @NonNull ParameterMapper parameterMapper) {
      super(identifier, pathPattern);
      this.parameterMapper = parameterMapper;
    }

    public DynamicEndPoint build() {
      return new DynamicEndPoint(this);
    }
  }
}
