package org.dotwebstack.framework.frontend.openapi.entity.builder;

import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.Property;
import java.util.Map;
import org.dotwebstack.framework.frontend.openapi.entity.properties.PropertyHandlerRegistry;
import org.dotwebstack.framework.frontend.openapi.entity.properties.PropertyHandlerRuntimeException;
import org.eclipse.rdf4j.model.Value;
import org.springframework.stereotype.Service;

@Service
public class EntityBuilder {

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

  @SuppressWarnings("unchecked")
  private Map<String, Object> buildResource(Property schemaProperty,
      EntityBuilderContext entityBuilderContext, PropertyHandlerRegistry registry,
      Value ldPathContext) {
    return (Map<String, Object>) registry.handle(schemaProperty, entityBuilderContext,
        ldPathContext);
  }

}
