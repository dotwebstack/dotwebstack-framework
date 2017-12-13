package org.dotwebstack.framework.frontend.openapi.entity.schema;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.Property;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntityContext;
import org.dotwebstack.framework.frontend.openapi.entity.LdPathExecutor;
import org.dotwebstack.framework.frontend.openapi.entity.schema.ValueContext.ValueContextBuilder;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.springframework.stereotype.Service;

@Service
class ObjectSchemaMapper extends AbstractSubjectFilterSchemaMapper<ObjectProperty, Object> {

  // XXX (PvH) @NonNull bewust verwijderd?
  @Override
  public Object mapTupleValue(ObjectProperty schema, ValueContext valueContext) {
    throw new UnsupportedOperationException();
  }

  // XXX (PvH) @NonNull bewust verwijderd?
  @Override
  public Object mapGraphValue(ObjectProperty property, GraphEntityContext graphEntityContext,
      ValueContext valueContext, SchemaMapperAdapter schemaMapperAdapter) {

    ValueContext newValueContext = processPropagationsInitial(property, valueContext);

    Object result =
        handleProperty(property, graphEntityContext, newValueContext, schemaMapperAdapter);
    if (!isExcludedWhenNull(newValueContext, property, result)) {
      return result;
    }
    // XXX (PvH) Geen unit test voor deze branch
    return null;
  }

  private Object handleProperty(ObjectProperty property, GraphEntityContext graphEntityContext,
      ValueContext valueContext, SchemaMapperAdapter schemaMapperAdapter) {
    ValueContextBuilder builder = valueContext.toBuilder();

    if (hasSubjectFilterVendorExtension(property)) {
      Set<Resource> subjects = filterSubjects(property, graphEntityContext);

      if (subjects.isEmpty()) {
        if (property.getRequired()) {
          throw new SchemaMapperRuntimeException(
              "Subject filter for a required object property yielded no result.");
        }

        return null;
      }

      if (subjects.size() > 1) {
        throw new SchemaMapperRuntimeException(
            "More entrypoint subjects found. Only one is required.");
      }

      builder.value(subjects.iterator().next());
    }

    ValueContext newValueContext = builder.build();

    if (hasVendorExtension(property, OpenApiSpecificationExtensions.LDPATH)) {
      String ldPath =
          property.getVendorExtensions().get(OpenApiSpecificationExtensions.LDPATH).toString();
      return handleLdPathVendorExtension(property, graphEntityContext, newValueContext, ldPath,
          schemaMapperAdapter);
    }

    return handleProperties(property, graphEntityContext, newValueContext, schemaMapperAdapter);
  }


  private Map<String, Object> handleLdPathVendorExtension(ObjectProperty property,
      GraphEntityContext entityBuilderContext, ValueContext valueContext, String ldPathQuery,
      SchemaMapperAdapter schemaMapperAdapter) {

    LdPathExecutor ldPathExecutor = entityBuilderContext.getLdPathExecutor();
    Collection<Value> queryResult =
        ldPathExecutor.ldPathQuery(valueContext.getValue(), ldPathQuery);

    if (queryResult.isEmpty()) {
      if (!property.getRequired()) {
        return null;
      }
      throw new SchemaMapperRuntimeException(String.format(
          "LDPath expression for a required object property ('%s') yielded no result.",
          ldPathQuery));
    }

    if (queryResult.size() > 1) {
      throw new SchemaMapperRuntimeException(String.format(
          "LDPath expression for object property ('%s') yielded multiple elements.", ldPathQuery));
    }

    ValueContext newValueContext =
        valueContext.toBuilder().value(queryResult.iterator().next()).build();

    return handleProperties(property, entityBuilderContext, newValueContext, schemaMapperAdapter);
  }



  private Map<String, Object> handleProperties(ObjectProperty property,
      GraphEntityContext entityBuilderContext, ValueContext valueContext,
      SchemaMapperAdapter schemaMapperAdapter) {
    ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
    property.getProperties().forEach((propKey, propValue) -> {
      Object propertyResult = schemaMapperAdapter.mapGraphValue(propValue, entityBuilderContext,
          valueContext, schemaMapperAdapter);

      // XXX (PvH) Zit de !(propValue instanceof ArrayProperty) check ook niet in de
      // isExcludedWhenNull?
      if (!(propValue instanceof ArrayProperty)
          && (!isExcludedWhenNull(valueContext, propValue, propertyResult))) {

        builder.put(propKey, com.google.common.base.Optional.fromNullable(propertyResult));
      }
      // XXX (PvH) Zit de (propValue instanceof ArrayProperty) check ook niet in de
      // isExcludedWhenEmpty?
      if (((propValue instanceof ArrayProperty)
          && !isExcludedWhenEmpty(valueContext, propValue, propertyResult))) {

        builder.put(propKey, com.google.common.base.Optional.fromNullable(propertyResult));
      }
    });
    return builder.build();
  }



  @Override
  public boolean supports(@NonNull Property schema) {
    return schema instanceof ObjectProperty;
  }

  @Override
  protected Set<IRI> getSupportedDataTypes() {
    return ImmutableSet.of();
  }

}
