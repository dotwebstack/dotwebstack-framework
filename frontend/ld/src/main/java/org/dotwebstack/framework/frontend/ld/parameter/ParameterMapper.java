package org.dotwebstack.framework.frontend.ld.parameter;

import java.util.Map;
import javax.ws.rs.container.ContainerRequestContext;
import org.dotwebstack.framework.frontend.ld.representation.Representation;

public interface ParameterMapper {

  public Map<String, Object> map(Representation representation,
      ContainerRequestContext containerRequestContext);

}
