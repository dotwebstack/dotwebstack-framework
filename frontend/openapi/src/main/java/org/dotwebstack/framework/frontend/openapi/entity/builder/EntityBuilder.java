package org.dotwebstack.framework.frontend.openapi.entity.builder;

import com.google.common.collect.ImmutableList;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import org.dotwebstack.framework.frontend.openapi.entity.properties.PropertyHandlerRegistry;
import org.dotwebstack.framework.frontend.openapi.entity.properties.PropertyHandlerRuntimeException;
import org.eclipse.rdf4j.model.Resource;
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

//    if (schemaProperty instanceof ObjectProperty) {
//
//      //validateCollectionEndpointSchema((ObjectProperty) schemaProperty);
//
//      return buildResource(schemaProperty, entityBuilderContext, registry, null);
//
//    }
    if (schemaProperty instanceof ArrayProperty) {
      return buildArray(schemaProperty, entityBuilderContext, registry,entityBuilderContext.getQueryResult().getSubjects().get(0));

    }

    throw new PropertyHandlerRuntimeException(
        String.format("Property type '%s' is not supported.", schemaProperty.getClass().getName()));
  }

  private Map<String, Object> buildArray(Property schemaProperty, EntityBuilderContext entityBuilderContext, PropertyHandlerRegistry registry, Resource resource) {
    ImmutableList itmes= (ImmutableList) registry.handle(schemaProperty, entityBuilderContext,
            resource);
    return null;
  }

  private Map<String, Object> processResourceEndpoint(Property schemaProperty,
      EntityBuilderContext entityBuilderContext, PropertyHandlerRegistry registry) {
    LinkedHashMap subjectFilter= (LinkedHashMap) schemaProperty.getVendorExtensions().get(0);
    LinkedHashMap predicateObjects= (LinkedHashMap) subjectFilter.get("x-dotwebstack-subject-filter");
    String predicate = (String) predicateObjects.get("predicate");
    String object = (String) predicateObjects.get("object");

    ImmutableList<Resource> subjects = entityBuilderContext.getQueryResult().getSubjects();
    int size = subjects.size();

    if (size == 0) {
      throw new NotFoundException(
          String.format("Resource '%s' was not found.", entityBuilderContext.getEndpoint()));
    }

//    if (size > 1) {
//      throw new InternalServerErrorException(String.format(
//          "There has to be exactly one query result for instance resources. Got: %s", size));
//    }

    return buildResource(schemaProperty, entityBuilderContext, registry, subjects.get(0));
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
