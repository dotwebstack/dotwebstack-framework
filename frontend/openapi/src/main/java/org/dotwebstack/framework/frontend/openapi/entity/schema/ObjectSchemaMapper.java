package org.dotwebstack.framework.frontend.openapi.entity.schema;

import com.google.common.base.Optional;
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
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.springframework.stereotype.Service;

@Service
public class ObjectSchemaMapper extends AbstractSubjectFilterSchemaMapper<ObjectProperty, Object> {

  @Override
  public Object mapTupleValue(@NonNull ObjectProperty schema, @NonNull ValueContext valueContext) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object mapGraphValue(@NonNull ObjectProperty property,
      @NonNull GraphEntityContext graphEntityContext, @NonNull ValueContext valueContext,
      @NonNull SchemaMapperAdapter schemaMapperAdapter) {
    ValueContext newValueContext = populateValueContextWithVendorExtensions(property, valueContext);

    return handleProperty(property, graphEntityContext, newValueContext, schemaMapperAdapter);
  }

  private static ValueContext populateValueContextWithVendorExtensions(@NonNull Property property,
      @NonNull ValueContext valueContext) {
    ValueContext.ValueContextBuilder builder = valueContext.toBuilder();

    if (hasVendorExtension(property,
        OpenApiSpecificationExtensions.EXCLUDE_PROPERTIES_WHEN_EMPTY_OR_NULL)) {
      builder.isExcludedWhenEmptyOrNull(
          hasVendorExtensionExcludePropertiesWhenEmptyOrNull(property));
    }

    return builder.build();
  }

  private static boolean hasVendorExtensionExcludePropertiesWhenEmptyOrNull(Property propValue) {
    return hasVendorExtensionWithValue(propValue,
        OpenApiSpecificationExtensions.EXCLUDE_PROPERTIES_WHEN_EMPTY_OR_NULL, true);

  }

  private Object handleProperty(ObjectProperty property, GraphEntityContext graphEntityContext,
      ValueContext valueContext, SchemaMapperAdapter schemaMapperAdapter) {
    ValueContext.ValueContextBuilder builder = valueContext.toBuilder();

    if (hasSubjectFilterVendorExtension(property)) {
      Value value = getSubject(property, graphEntityContext);

      if (value == null) {
        return null;
      }

      builder.value(value);
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

      if (!isExcludedWhenEmptyOrNull(valueContext, propValue, propertyResult)) {
        builder.put(propKey, Optional.fromNullable(propertyResult));
      }
    });
    return builder.build();
  }

  private static boolean isExcludedWhenEmptyOrNull(@NonNull ValueContext context,
      @NonNull Property property, Object value) {
    return context.isExcludedWhenEmptyOrNull()
        && (value == null || (property instanceof ArrayProperty && ((Collection) value).isEmpty()));
  }

  @Override
  public boolean supports(@NonNull Property schema) {
    return schema instanceof ObjectProperty
        && !schema.getVendorExtensions().containsKey(OpenApiSpecificationExtensions.TYPE);
  }

  @Override
  protected Set<IRI> getSupportedDataTypes() {
    return ImmutableSet.of();
  }

}
