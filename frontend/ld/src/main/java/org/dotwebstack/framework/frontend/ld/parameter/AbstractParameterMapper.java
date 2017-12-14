package org.dotwebstack.framework.frontend.ld.parameter;

import java.util.Map;
import javax.ws.rs.container.ContainerRequestContext;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.ld.parameter.source.ParameterSource;
import org.dotwebstack.framework.frontend.ld.parameter.target.Target;
import org.eclipse.rdf4j.model.IRI;

public abstract class AbstractParameterMapper implements ParameterMapper {

  private IRI identifier;

  private ParameterSource source;

  private Target target;

  protected AbstractParameterMapper(Builder<?> builder) {
    identifier = builder.identifier;
    source = builder.source;
    target = builder.target;
  }

  public IRI getIdentifier() {
    return identifier;
  }

  public Map<String, String> map(@NonNull ContainerRequestContext containerRequestContext) {
    String input = source.getValue(containerRequestContext);

    String output = parse(input);

    return target.set(output);
  }

  protected String parse(String input) {
    return input;
  }

  protected static class Builder<T extends Builder<T>> {
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

}
