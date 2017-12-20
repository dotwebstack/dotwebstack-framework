package org.dotwebstack.framework.frontend.ld.parameter;

import java.util.Map;
import javax.ws.rs.container.ContainerRequestContext;
import lombok.NonNull;
import org.eclipse.rdf4j.model.IRI;

public interface ParameterMapper {

  IRI getIdentifier();

  Map<String, String> map(@NonNull ContainerRequestContext containerRequestContext);

}
