package org.dotwebstack.framework.frontend.ld.parameter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.ws.rs.container.ContainerRequestContext;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.ld.representation.Representation;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.dotwebstack.framework.param.Parameter;
import org.springframework.stereotype.Service;

@Service
public class QueryParameterMapper implements ParameterMapper {

  public Map<String, Object> map(@NonNull Representation representation,
      @NonNull ContainerRequestContext context) {
    Map<String, Object> result = new HashMap<>();

    InformationProduct informationProduct = representation.getInformationProduct();

    for (Parameter<?> parameter : informationProduct.getParameters()) {

      Optional<String> parameterValue = Optional.ofNullable(
          context.getUriInfo().getQueryParameters().getFirst(parameter.getName()));

      parameterValue.ifPresent(value -> result.put(parameter.getName(), value));
    }

    return result;
  }

}
