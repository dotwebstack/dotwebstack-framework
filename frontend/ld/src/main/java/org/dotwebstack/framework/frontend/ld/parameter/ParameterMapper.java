package org.dotwebstack.framework.frontend.ld.parameter;

import java.util.Map;
import javax.ws.rs.container.ContainerRequestContext;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.ld.parameter.source.ParameterSource;
import org.dotwebstack.framework.frontend.ld.parameter.target.Target;
import org.eclipse.rdf4j.model.IRI;

public interface ParameterMapper {

  IRI getIdentifier();

  Map<String, Object> map(@NonNull ContainerRequestContext containerRequestContext);

  static class Builder<T extends Builder<T>> {
    public Builder(@NonNull IRI identifier, @NonNull ParameterSource source,
        @NonNull Target target) {}
  }

}
