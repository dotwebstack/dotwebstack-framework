package org.dotwebstack.framework.frontend.ld.handlers;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.ws.rs.container.ContainerRequestContext;
import lombok.NonNull;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.dotwebstack.framework.param.Parameter;
import org.springframework.stereotype.Service;

@Service
public class RepresentationRequestParameterMapper {

  Map<String, String> map(@NonNull InformationProduct informationProduct,
      @NonNull ContainerRequestContext context) {
    Map<String, String> result = new HashMap<>();

    for (Parameter<?> parameter : informationProduct.getParameters()) {

      Optional<String> parameterValue = Optional.ofNullable(
          context.getUriInfo().getQueryParameters().getFirst(parameter.getName()));

      parameterValue.ifPresent(value -> result.put(parameter.getName(), value));
    }

    return result;
  }

}
