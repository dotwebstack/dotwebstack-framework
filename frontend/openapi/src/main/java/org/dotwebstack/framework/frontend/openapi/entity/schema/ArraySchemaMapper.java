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
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntity;
import org.dotwebstack.framework.frontend.openapi.entity.LdPathExecutor;
import org.dotwebstack.framework.frontend.openapi.entity.TupleEntity;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.springframework.stereotype.Service;

@Service
public class ArraySchemaMapper extends AbstractSubjectSchemaMapper<ArrayProperty, Object> {

  @Override
  public Object mapTupleValue(@NonNull ArrayProperty schema, @NonNull TupleEntity entity,
      @NonNull ValueContext valueContext) {
    return SchemaMapperUtils.castLiteralValue(valueContext.getValue()).integerValue();
  }

  @Override
  public Object mapGraphValue(@NonNull ArrayProperty schema, @NonNull GraphEntity entity,
      @NonNull ValueContext valueContext, @NonNull SchemaMapperAdapter schemaMapperAdapter) {
    ImmutableList.Builder<Object> builder = ImmutableList.builder();

    if (hasSubjectVendorExtension(schema)) {
      Set<Resource> subjects = entity.getSubjects();

      subjects.forEach(subject -> {
        ValueContext subjectContext = valueContext.toBuilder().value(subject).build();

        builder.add(schemaMapperAdapter.mapGraphValue(schema.getItems(), entity, subjectContext,
            schemaMapperAdapter));
      });
    } else if (valueContext.getValue() != null) {
      if (schema.getVendorExtensions().containsKey(OpenApiSpecificationExtensions.LDPATH)) {
        queryAndValidate(schema, entity, valueContext, schemaMapperAdapter, builder);
      } else {
        throw new SchemaMapperRuntimeException(String.format(
            "ArrayProperty must have a '%s' attribute", OpenApiSpecificationExtensions.LDPATH));
      }
    }

    return builder.build();
  }

  private void queryAndValidate(ArrayProperty property, GraphEntity graphEntity,
      ValueContext valueContext, SchemaMapperAdapter schemaMapperAdapter,
      ImmutableList.Builder<Object> builder) {
    LdPathExecutor ldPathExecutor = graphEntity.getLdPathExecutor();
    Collection<Value> queryResult = ldPathExecutor.ldPathQuery(valueContext.getValue(),
        (String) property.getVendorExtensions().get(OpenApiSpecificationExtensions.LDPATH));

    validateMinItems(property, queryResult);
    validateMaxItems(property, queryResult);

    queryResult.forEach(valueNext -> {
      ValueContext newValueContext = valueContext.toBuilder().value(valueNext).build();
      Optional innerPropertySolved =
          Optional.fromNullable(schemaMapperAdapter.mapGraphValue(property.getItems(), graphEntity,
              newValueContext, schemaMapperAdapter));
      builder.add(innerPropertySolved);

    });

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
