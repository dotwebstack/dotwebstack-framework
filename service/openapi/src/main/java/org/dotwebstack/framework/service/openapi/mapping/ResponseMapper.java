package org.dotwebstack.framework.service.openapi.mapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.NonNull;
import org.dotwebstack.framework.service.openapi.response.ResponseObject;

public class ResponseMapper {

  @SuppressWarnings("unchecked")
  public Object mapResponse(@NonNull ResponseObject responseObject, Object data) {
    switch (responseObject.getType()) {
      case "array":
        ResponseObject childResponseObject = responseObject.getItems()
            .get(0);
        return ((List<Object>) data).stream()
            .map(object -> mapResponse(childResponseObject, object))
            .collect(Collectors.toList());
      case "object":
        Map<String, Object> result = new HashMap<>();

        responseObject.getChildren()
            .forEach(child -> {
              Object object = mapResponse(child, ((Map<String, Object>) data).get(child.getIdentifier()));
              if (!child.isRequired() && object == null) {
                // property is not required and not returned: don't add to response.
              } else if (child.isRequired() && child.isNillable() && object == null) {
                result.put(child.getIdentifier(), null);
              } else if (child.isRequired() && !child.isNillable() && object == null) {
                throw new MappingException(String.format("Could not map GraphQL response: Required and non-nillable "
                    + "property '%s' was not return in GraphQL response.", child.getIdentifier()));
              } else {
                result.put(child.getIdentifier(), object);
              }
            });
        return result;
      default:
        return data;
    }
  }
}
