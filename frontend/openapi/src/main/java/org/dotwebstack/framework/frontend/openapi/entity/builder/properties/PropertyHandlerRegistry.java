package org.dotwebstack.framework.frontend.openapi.entity.builder.properties;

import com.google.common.collect.ImmutableList;
import io.swagger.models.properties.Property;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import org.dotwebstack.framework.frontend.openapi.entity.builder.EntityBuilderContext;
import org.dotwebstack.framework.frontend.openapi.entity.builder.OasVendorExtensions;
import org.eclipse.rdf4j.model.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PropertyHandlerRegistry {

  private List<PropertyHandler<? extends Property>> propertyHandlers;

  @Autowired
  public void setPropertyHandlers(
      Collection<PropertyHandler<? extends Property>> propertyHandlers) {
    this.propertyHandlers = ImmutableList.copyOf(propertyHandlers);
  }

  public Object handle(Property property, EntityBuilderContext builderContext, Value ctx) {
    PropertyHandler<Property> propertyHandler = handlerFor(property);
    return propertyHandler.handle(property, builderContext, this, ctx);
  }

  @SuppressWarnings("unchecked")
  <P extends Property> PropertyHandler<P> handlerFor(Property property) {

    String extendedType = getExtendedType(property);
    for (PropertyHandler<? extends Property> handler : propertyHandlers) {
      XTypePropertyHandler extendedHandler = extractExtendedHandler(handler);

      if (extendedType == null && extendedHandler == null) {
        if (handler.supports(property)) {
          return (PropertyHandler<P>) handler;
        }
      } else {
        if (extendedHandler != null && extendedHandler.value().equals(extendedType)
            && handler.supports(property)) {
          return (PropertyHandler<P>) handler;
        }
      }
    }

    throw new PropertyHandlerRuntimeException(
        String.format("No property handler available for property '%s' (extended type: '%s').",
            property.getClass().getName(), extendedType));
  }

  private String getExtendedType(@Nonnull Property prop) {
    Map<String, Object> vendorExtensions = prop.getVendorExtensions();
    if (vendorExtensions == null || vendorExtensions.isEmpty()) {
      return null;
    }

    Object extendedType = vendorExtensions.get(OasVendorExtensions.TYPE);
    return extendedType == null ? null : extendedType.toString();
  }

  private XTypePropertyHandler extractExtendedHandler(PropertyHandler<? extends Property> handler) {
    return handler.getClass().getAnnotation(XTypePropertyHandler.class);
  }

}
