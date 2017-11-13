package org.dotwebstack.framework.frontend.openapi.entity.properties;

import com.google.common.collect.ImmutableMap;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.Property;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.dotwebstack.framework.frontend.openapi.entity.LdPathExecutor;
import org.dotwebstack.framework.frontend.openapi.entity.builder.EntityBuilderContext;
import org.dotwebstack.framework.frontend.openapi.entity.builder.OasVendorExtensions;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.springframework.stereotype.Service;

@Service
public class ObjectPropertyHandler extends AbstractPropertyHandler<ObjectProperty> {

  @Override
  public Object handle(ObjectProperty property, EntityBuilderContext entityBuilderContext,
      PropertyHandlerRegistry registry, Value context) {

    Value contextNew = context;
    if (property.getVendorExtensions().containsKey(OasVendorExtensions.SUBJECT_FILTER)) {
      LinkedHashMap subjectFilter =
          (LinkedHashMap) property.getVendorExtensions().get(OasVendorExtensions.SUBJECT_FILTER);

      String predicate = (String) subjectFilter.get(OasVendorExtensions.SUBJECT_FILTER_PREDICATE);
      String object = (String) subjectFilter.get(OasVendorExtensions.SUBJECT_FILTER_OBJECT);

      ValueFactory vf = SimpleValueFactory.getInstance();

      final IRI predicateIri = vf.createIRI(predicate);
      final IRI objectLiteral = vf.createIRI(object);

      Model filteredModel = entityBuilderContext.getQueryResult().getModel().filter(null,
          predicateIri, objectLiteral);

      if (filteredModel.subjects().iterator().hasNext()) {
        if (filteredModel.subjects().size() > 1) {
          throw new PropertyHandlerRuntimeException(
              String.format("More entrypoint subjects found for ('%s,%s'). Only one is needed.",
                  predicate, object));
        }
        contextNew = filteredModel.subjects().iterator().next();
      } else {
        throw new PropertyHandlerRuntimeException(
            String.format("No entrypoint subject found for ('%s,%s')", predicate, object));
      }
    }

    if (property.getVendorExtensions().containsKey(OasVendorExtensions.LDPATH)) {
      String ldPath = property.getVendorExtensions().get(OasVendorExtensions.LDPATH).toString();
      return handleLdPathVendorExtension(property, entityBuilderContext, registry, contextNew,
          ldPath);
    }

    return handleProperties(property, entityBuilderContext, registry, contextNew);
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


    property.getProperties().forEach((propKey, propValue) -> {
      Object propertyResult = registry.handle(propValue, entityBuilderContext, context);

      if (propertyResult == null) {
        return;
      }
      if (propertyResult instanceof Map<?, ?> && ((Map<?, ?>) propertyResult).size() == 0) {
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
