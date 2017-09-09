package org.dotwebstack.framework.frontend.openapi.properties;

import com.google.common.collect.ImmutableList;
import io.swagger.models.properties.Property;
import java.util.Collection;
import lombok.NonNull;
import org.eclipse.rdf4j.model.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PropertyHandlerAdapter {

  private ImmutableList<PropertyHandler<? extends Property, ?>> propertyHandlers;

  @Autowired
  public PropertyHandlerAdapter(
      @NonNull Collection<PropertyHandler<? extends Property, ?>> propertyHandlers) {
    this.propertyHandlers = ImmutableList.copyOf(propertyHandlers);
  }

  @SuppressWarnings("unchecked")
  public <P extends Property> Object handle(@NonNull P property, @NonNull Value value) {
    for (PropertyHandler<? extends Property, ?> handler : propertyHandlers) {
      if (handler.supports(property)) {
        return ((PropertyHandler<P, ?>) handler).handle(property, value);
      }
    }

    throw new PropertyHandlerRuntimeException(
        String.format("No property handler available for '%s'.", property.getClass().getName()));
  }

}
