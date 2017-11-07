package org.dotwebstack.framework.frontend.openapi.entity.properties;


import com.google.common.collect.ImmutableMap;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.Property;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.dotwebstack.framework.frontend.openapi.entity.builder.EntityBuilderContext;
import org.dotwebstack.framework.frontend.openapi.entity.builder.OasVendorExtensions;
import org.eclipse.rdf4j.model.Value;
import org.springframework.stereotype.Service;

@Service
public class ObjectPropertyHandler extends AbstractPropertyHandler<ObjectProperty> {

  @Override
  public Object handle(ObjectProperty property, EntityBuilderContext entityBuilderContext,
      PropertyHandlerRegistry registry, Value context) {

    Object ldPathObj = property.getVendorExtensions().get(OasVendorExtensions.LDPATH);
    if (ldPathObj != null) {
      return handleLdPathVendorExtension(property, entityBuilderContext, registry, context,
          ldPathObj.toString());
    }

    return handleProperties(property, entityBuilderContext, registry, context);
  }

  private Map<String, Object> handleLdPathVendorExtension(ObjectProperty property,
      EntityBuilderContext entityBuilderContext, PropertyHandlerRegistry registry, Value context,
      String ldPathQuery) {

    LdPathExecutor ldPathExecutor = entityBuilderContext.getLdPathExecutor();
    Collection<Value> queryResult = ldPathExecutor.ldPathQuery(context, ldPathQuery);

    if (queryResult.isEmpty()) {
      if (!property.getRequired()) {
        return null;
      }
      throw new PropertyHandlerRuntimeException(String.format(
          "LDPath expression for a required object property ('%s') yielded no result.",
          ldPathQuery));
    }

    if (queryResult.size() > 1) {
      throw new PropertyHandlerRuntimeException(String.format(
          "LDPath expression for object property ('%s') yielded multiple elements.", ldPathQuery));
    }

    return handleProperties(property, entityBuilderContext, registry,
        queryResult.iterator().next());
  }

  private Map<String, Object> handleProperties(ObjectProperty property,
      EntityBuilderContext entityBuilderContext, PropertyHandlerRegistry registry, Value context) {
    ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();

    boolean excludeEmptyAndNullValueProperties =
        Boolean.TRUE.equals(property.getVendorExtensions().get(
            OasVendorExtensions.EXCLUDE_EMPTY_AND_NULL_VALUE_PROPERTIES));

    property.getProperties().forEach((propKey, propValue) -> {
      Object propertyResult = registry.handle(propValue, entityBuilderContext, context);

      if (excludeEmptyAndNullValueProperties) {
        if (propertyResult == null) {
          return;
        }
        if (propertyResult instanceof Map<?, ?> && ((Map<?, ?>) propertyResult).size() == 0) {
          return;
        }
      }

      if (excludeEmptyAndNullValueProperties && (propValue instanceof ArrayProperty)
          && ((List<?>) propertyResult).isEmpty()) {
        return;
      }

      builder.put(propKey, Optional.ofNullable(propertyResult));
    });
    return builder.build();
  }

  @Override
  public boolean supports(Property property) {
    return ObjectProperty.class.isInstance(property);
  }

}
