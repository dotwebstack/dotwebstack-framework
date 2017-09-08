package org.dotwebstack.framework.frontend.openapi.properties;

import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import lombok.NonNull;
import org.eclipse.rdf4j.model.Value;

public class RefPropertyHandler implements PropertyHandler<RefProperty, Object> {

  @Override
  public Object handle(@NonNull RefProperty property, @NonNull Value value) {
    throw new PropertyHandlerRuntimeException("Ref property is not supported for tuple results.");
  }

  @Override
  public boolean supports(@NonNull Property property) {
    return RefProperty.class.isInstance(property);
  }

}
