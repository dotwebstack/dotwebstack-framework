package org.dotwebstack.framework.frontend.openapi.properties;

import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.Property;
import java.util.Collection;
import lombok.NonNull;
import org.eclipse.rdf4j.model.Value;
import org.springframework.stereotype.Service;

@Service
public class ArrayPropertyHandler implements PropertyHandler<ArrayProperty, Collection<Object>> {

  @Override
  public Collection<Object> handle(@NonNull ArrayProperty property, @NonNull Value value) {
    throw new PropertyHandlerRuntimeException("Array property is not supported for tuple results.");
  }

  @Override
  public boolean supports(@NonNull Property property) {
    return ArrayProperty.class.isInstance(property);
  }

}
