package org.dotwebstack.framework.frontend.openapi.entity.schema;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.Property;
import java.util.Collection;
import java.util.Set;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntityContext;
import org.dotwebstack.framework.frontend.openapi.entity.LdPathExecutor;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.springframework.stereotype.Service;

@Service
public class ArraySchemaMapper extends AbstractLdPathSchemaMapper<ArrayProperty, Object> {

  @Override
  public Object mapTupleValue(@NonNull ArrayProperty schema, @NonNull Value value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object mapGraphValue(@NonNull ArrayProperty property,
      @NonNull GraphEntityContext graphEntityContext,
      @NonNull SchemaMapperAdapter schemaMapperAdapter, Value context) {
    ImmutableList.Builder<Object> builder = ImmutableList.builder();

    Set<Resource> subjects = applySubjectFilterIfPossible(property, graphEntityContext);

    if (!subjects.isEmpty()) {
      if (OpenApiSpecificationExtensions.RESULT_REF_COLLECTION.equals(
          property.getVendorExtensions().get(OpenApiSpecificationExtensions.RESULT_REF))) {
        subjects.forEach(
            subject -> builder.add(schemaMapperAdapter.mapGraphValue(property.getItems(),
                graphEntityContext, schemaMapperAdapter, subject)));
      } else if (property.getVendorExtensions().containsKey(
          OpenApiSpecificationExtensions.LDPATH)) {
        subjects.forEach(subject -> queryAndValidate(property, graphEntityContext,
            schemaMapperAdapter, subject, builder));
      }
    } else {
      if (context != null) {
        if (property.getVendorExtensions().containsKey(OpenApiSpecificationExtensions.LDPATH)) {
          queryAndValidate(property, graphEntityContext, schemaMapperAdapter, context, builder);
        } else {
          throw new SchemaMapperRuntimeException(
              String.format("ArrayProperty must have either a '%s' attribute",
                  OpenApiSpecificationExtensions.LDPATH));
        }
      }
    }

    return builder.build();
  }

  private void queryAndValidate(ArrayProperty property, GraphEntityContext graphEntityContext,
      SchemaMapperAdapter schemaMapperAdapter, Value context,
      ImmutableList.Builder<Object> builder) {
    LdPathExecutor ldPathExecutor = graphEntityContext.getLdPathExecutor();
    Collection<Value> queryResult = ldPathExecutor.ldPathQuery(context,
        (String) property.getVendorExtensions().get(OpenApiSpecificationExtensions.LDPATH));

    validateMinItems(property, queryResult);
    validateMaxItems(property, queryResult);

    queryResult.forEach(valueNext -> builder.add(
        Optional.fromNullable(schemaMapperAdapter.mapGraphValue(property.getItems(),
            graphEntityContext, schemaMapperAdapter, valueNext))));
  }

  private static void validateMinItems(ArrayProperty arrayProperty, Collection<Value> queryResult) {
    Integer minItems = arrayProperty.getMinItems();
    if (minItems != null && minItems > queryResult.size()) {
      throw new SchemaMapperRuntimeException(String.format(
          "Mapping for property yielded %d elements, which is less than 'minItems' (%d)"
              + " specified in the OpenAPI specification.",
          queryResult.size(), minItems));
    }
  }

  private static void validateMaxItems(ArrayProperty arrayProperty, Collection<Value> queryResult) {
    Integer maxItems = arrayProperty.getMaxItems();
    if (maxItems != null && maxItems < queryResult.size()) {
      throw new SchemaMapperRuntimeException(String.format(
          "Mapping for property yielded %d elements, which is more than 'maxItems' (%d)"
              + " specified in the OpenAPI specification.",
          queryResult.size(), maxItems));
    }
  }

  @Override
  public boolean supports(@NonNull Property schema) {
    return schema instanceof ArrayProperty;
  }

  @Override
  protected Set<IRI> getSupportedDataTypes() {
    return ImmutableSet.of();
  }

}
