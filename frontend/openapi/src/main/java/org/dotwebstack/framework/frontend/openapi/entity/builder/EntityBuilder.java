package org.dotwebstack.framework.frontend.openapi.entity.builder;

import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.Property;
import java.util.Map;
import org.dotwebstack.framework.frontend.openapi.entity.properties.PropertyHandlerRegistry;
import org.dotwebstack.framework.frontend.openapi.entity.properties.PropertyHandlerRuntimeException;
import org.eclipse.rdf4j.model.Value;
import org.springframework.stereotype.Service;

@Service
public class EntityBuilder {

  static final String EMBEDDED = "_embedded";

  /**
   * Build an entity according to a given OAS schema.
   *
   * <p>
   * Depending on the type of property the 'root' of the schema, the endpoint is considered either a
   * single-resource endpoint or a collection endpoint:
   * </p>
   * <ul>
   * <li>A "$ref" property is considered a single-resource endpoint.</li>
   * <li>An "object" property is considered a collection endpoint.</li>
   * </ul>
   *
   * @param schemaProperty the schema for which to build the entity.
   * @param entityBuilderContext contains the OAS, search result and request parameters.
   */
  public Map<String, Object> build(Property schemaProperty, PropertyHandlerRegistry registry,
      EntityBuilderContext entityBuilderContext) {

    if (schemaProperty instanceof ObjectProperty) {
      return processResourceEndpoint(schemaProperty, entityBuilderContext, registry);
    }
    throw new PropertyHandlerRuntimeException(
        String.format("Property type '%s' is not supported.", schemaProperty.getClass().getName()));
  }



  private Map<String, Object> processResourceEndpoint(Property schemaProperty,
      EntityBuilderContext entityBuilderContext, PropertyHandlerRegistry registry) {


    return buildResource(schemaProperty, entityBuilderContext, registry, null);
  }

  private void validateCollectionEndpointSchema(ObjectProperty schemaProperty) {
    Map<String, Property> properties = schemaProperty.getProperties();

    if (!properties.containsKey(EMBEDDED)) {
      throw new PropertyHandlerRuntimeException(
          String.format("Object requires '%s' property.", EMBEDDED));
    }

    if (!(properties.get(EMBEDDED) instanceof ObjectProperty)) {
      throw new PropertyHandlerRuntimeException(
          String.format("'%s' property should be of type 'object'.", EMBEDDED));
    }
    Map<String, Property> embedProperties =
        ((ObjectProperty) properties.get(EMBEDDED)).getProperties();

    if (embedProperties.size() != 1) {
      throw new PropertyHandlerRuntimeException(String.format(
          "Object property '%s' should contain exactly one property item.", EMBEDDED));
    }

    Property embedProperty = embedProperties.get(embedProperties.keySet().iterator().next());

    if (!(embedProperty instanceof ArrayProperty)) {
      throw new PropertyHandlerRuntimeException(String.format(
          "Properties within the '%s' property must always be array properties.", EMBEDDED));
    }
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> buildResource(Property schemaProperty,
      EntityBuilderContext entityBuilderContext, PropertyHandlerRegistry registry,
      Value ldPathContext) {
    return (Map<String, Object>) registry.handle(schemaProperty, entityBuilderContext,
        ldPathContext);
  }

}
