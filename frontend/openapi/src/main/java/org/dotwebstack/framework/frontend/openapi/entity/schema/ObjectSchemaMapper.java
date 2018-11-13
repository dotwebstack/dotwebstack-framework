package org.dotwebstack.framework.frontend.openapi.entity.schema;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntity;
import org.dotwebstack.framework.frontend.openapi.entity.LdPathExecutor;
import org.dotwebstack.framework.frontend.openapi.entity.TupleEntity;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.springframework.stereotype.Service;

@Service
public class ObjectSchemaMapper extends AbstractSubjectSchemaMapper<ObjectSchema, Object> {

  @Override
  protected Set<String> getSupportedVendorExtensions() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object mapTupleValue(@NonNull ObjectSchema schema, @NonNull TupleEntity entity,
      @NonNull ValueContext valueContext) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object mapGraphValue(@NonNull ObjectSchema schema, boolean required,
      @NonNull GraphEntity graphEntity, @NonNull ValueContext valueContext,
      @NonNull SchemaMapperAdapter schemaMapperAdapter) {
    ValueContext newValueContext = populateValueContextWithVendorExtensions(schema, valueContext);

    return handleProperty(schema, required, graphEntity, newValueContext, schemaMapperAdapter);
  }

  private static ValueContext populateValueContextWithVendorExtensions(@NonNull Schema property,
      @NonNull ValueContext valueContext) {
    ValueContext.ValueContextBuilder builder = valueContext.toBuilder();

    if (hasVendorExtension(property,
        OpenApiSpecificationExtensions.EXCLUDE_PROPERTIES_WHEN_EMPTY_OR_NULL)) {
      builder.isExcludedWhenEmptyOrNull(
          hasVendorExtensionExcludePropertiesWhenEmptyOrNull(property));
    }

    return builder.build();
  }

  private static boolean hasVendorExtensionExcludePropertiesWhenEmptyOrNull(Schema propValue) {
    return hasVendorExtensionWithValue(propValue,
        OpenApiSpecificationExtensions.EXCLUDE_PROPERTIES_WHEN_EMPTY_OR_NULL, true);
  }

  private Object handleProperty(ObjectSchema property, boolean required, GraphEntity graphEntity,
      ValueContext valueContext, SchemaMapperAdapter schemaMapperAdapter) {
    ValueContext.ValueContextBuilder builder = valueContext.toBuilder();

    if (hasSubjectVendorExtension(property)) {
      Value value = getSubject(property, required, graphEntity);

      if (value == null) {
        return null;
      }

      builder.value(value);
    }

    ValueContext newValueContext = builder.build();

    if (hasVendorExtension(property, OpenApiSpecificationExtensions.LDPATH)) {
      String ldPath =
          property.getExtensions().get(OpenApiSpecificationExtensions.LDPATH).toString();
      return handleLdPathVendorExtension(property, required, graphEntity, newValueContext, ldPath,
          schemaMapperAdapter);
    }

    return handleProperties(property, graphEntity, newValueContext, schemaMapperAdapter);
  }

  private Map<String, Object> handleLdPathVendorExtension(ObjectSchema schema, boolean required,
      GraphEntity graphEntity, ValueContext valueContext, String ldPathQuery,
      SchemaMapperAdapter schemaMapperAdapter) {

    LdPathExecutor ldPathExecutor = graphEntity.getLdPathExecutor();
    Collection<Value> queryResult =
        ldPathExecutor.ldPathQuery(valueContext.getValue(), ldPathQuery);

    if (queryResult.isEmpty()) {
      if (!required) {
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

    return handleProperties(schema, graphEntity, newValueContext, schemaMapperAdapter);
  }

  private Map<String, Object> handleProperties(ObjectSchema schema,
      GraphEntity entityBuilderContext, ValueContext valueContext,
      SchemaMapperAdapter schemaMapperAdapter) {
    ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();

    schema.getProperties().forEach((propKey, propValue) -> {
      Object propertyResult = schemaMapperAdapter.mapGraphValue(propValue,
          schema.getRequired() != null && schema.getRequired().contains(propKey),
          entityBuilderContext, valueContext, schemaMapperAdapter);

      if (!isExcludedWhenEmptyOrNull(valueContext, propValue, propertyResult)) {
        builder.put(propKey, Optional.fromNullable(propertyResult));
      }
    });

    return builder.build();
  }

  private static boolean isExcludedWhenEmptyOrNull(@NonNull ValueContext context,
      @NonNull Schema property, Object value) {
    return context.isExcludedWhenEmptyOrNull()
        && (value == null || (property instanceof ArraySchema && ((Collection) value).isEmpty()));
  }

  @Override
  public boolean supports(@NonNull Schema schema) {
    return schema instanceof ObjectSchema && !(schema.getExtensions() != null
        && schema.getExtensions().containsKey(OpenApiSpecificationExtensions.TYPE));
  }

  @Override
  protected Object convertLiteralToType(Literal literal) {
    return literal;
  }

  @Override
  protected Set<IRI> getSupportedDataTypes() {
    return ImmutableSet.of();
  }

}
