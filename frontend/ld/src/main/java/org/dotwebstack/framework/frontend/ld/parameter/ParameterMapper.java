package org.dotwebstack.framework.frontend.ld.parameter;

import java.util.Map;
import javax.ws.rs.container.ContainerRequestContext;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.ld.parameter.source.ParameterSource;
import org.dotwebstack.framework.frontend.ld.parameter.target.Target;
import org.eclipse.rdf4j.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ParameterMapper {

  private static final Logger LOG = LoggerFactory.getLogger(ParameterMapper.class);

  private IRI identifier;

  private ParameterSource source;

  private Target target;

  public IRI getIdentifier() {
    return identifier;
  }

  public Map<String, Object> map(@NonNull ContainerRequestContext containerRequestContext) {

    String input = source.getValue(containerRequestContext);

    String output = parse(input);

    Map<String, Object> result = target.set(output);

    return result;
  }

  public String parse(String input) {
    return input;
  }

  public static class Builder<T extends Builder<T>> {

    private IRI identifier;
    private ParameterSource source;
    private Target target;

    public Builder(@NonNull IRI identifier, @NonNull ParameterSource source,
        @NonNull Target target) {
      this.identifier = identifier;
      this.source = source;
      this.target = target;
    }
  }

  protected ParameterMapper(Builder<?> builder) {
    identifier = builder.identifier;
    source = builder.source;
    target = builder.target;
  }

}
