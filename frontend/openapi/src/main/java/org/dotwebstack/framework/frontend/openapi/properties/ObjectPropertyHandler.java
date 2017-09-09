package org.dotwebstack.framework.frontend.openapi.properties;

import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.Property;
import java.util.Map;
import lombok.NonNull;
import org.eclipse.rdf4j.model.Value;
import org.springframework.stereotype.Service;

@Service
public class ObjectPropertyHandler implements PropertyHandler<ObjectProperty, Map<String, Object>> {

  @Override
  public Map<String, Object> handle(@NonNull ObjectProperty property, @NonNull Value value) {
    throw new PropertyHandlerRuntimeException(
        "Object property is not supported for tuple results.");
  }

  @Override
  public boolean supports(@NonNull Property property) {
    return ObjectProperty.class.isInstance(property);
  }

}
