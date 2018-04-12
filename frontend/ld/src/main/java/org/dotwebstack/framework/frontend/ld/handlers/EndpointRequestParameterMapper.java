package org.dotwebstack.framework.frontend.ld.handlers;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.ws.rs.container.ContainerRequestContext;
import lombok.NonNull;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.dotwebstack.framework.param.Parameter;
import org.dotwebstack.framework.transaction.Transaction;
import org.springframework.stereotype.Service;

@Service
public class EndpointRequestParameterMapper {

  Map<String, String> map(@NonNull InformationProduct informationProduct,
      @NonNull ContainerRequestContext context) {
    return getParameters(context, informationProduct.getParameters());
  }

  Map<String, String> map(@NonNull Transaction transaction,
      @NonNull ContainerRequestContext context) {
    return getParameters(context, transaction.getParameters());
  }

  private Map<String, String> getParameters(@NonNull ContainerRequestContext context,
      Collection<Parameter> parameters) {
    Map<String, String> result = new HashMap<>();

    for (Parameter<?> parameter : parameters) {

      Optional<String> parameterValue = Optional.ofNullable(
          context.getUriInfo().getQueryParameters().getFirst(parameter.getName()));

      parameterValue.ifPresent(value -> result.put(parameter.getName(), value));
    }

    return result;
  }

}
