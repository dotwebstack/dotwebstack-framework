package org.dotwebstack.framework.frontend.openapi.schema;

import com.google.common.collect.ImmutableList;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.Property;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntityContext;
import org.dotwebstack.framework.frontend.openapi.entity.LdPathExecutor;
import org.eclipse.rdf4j.model.Value;
import org.springframework.stereotype.Service;

@Service
public class ArraySchemaMapper extends AbstractSchemaMapper
    implements SchemaMapper<ArrayProperty, Object> {

  @Override
  public Object mapTupleValue(@NonNull ArrayProperty schema, @NonNull Value value) {
    return SchemaMapperUtils.castLiteralValue(value).integerValue();
  }

  @Override
  public Object mapGraphValue(ArrayProperty schema, GraphEntityContext graphEntityContext,
      SchemaMapperAdapter schemaMapperAdapter, Value value) {
    return handle(schema, graphEntityContext, schemaMapperAdapter, value);
  }



  public Object handle(ArrayProperty property, GraphEntityContext entityBuilderContext,
      SchemaMapperAdapter schemaMapperAdapter, Value context) {

    Property itemProperty = property.getItems();
    ImmutableList.Builder<Object> builder = ImmutableList.builder();

    if ("collection".equals(
        property.getVendorExtensions().get(OpenApiSpecificationExtensions.RESULT_REF))) {
      for (Value subject : entityBuilderContext.getSubjects()) {

        Map<String, Object> resource =
            mapGraphResource(itemProperty, entityBuilderContext, schemaMapperAdapter, subject);

        builder.add(resource);
      }
    } else if (property.getVendorExtensions().containsKey(OpenApiSpecificationExtensions.LDPATH)) {

      LdPathExecutor ldPathExecutor = entityBuilderContext.getLdPathExecutor();
      Collection<Value> queryResult = ldPathExecutor.ldPathQuery(context,
          (String) property.getVendorExtensions().get(OpenApiSpecificationExtensions.LDPATH));


      validateMinItems(property, queryResult);
      validateMaxItems(property, queryResult);

      queryResult.forEach(
          value -> builder.add(Optional.ofNullable(schemaMapperAdapter.mapGraphValue(itemProperty,
              entityBuilderContext, schemaMapperAdapter, value))));

    } else {
      throw new SchemaMapperRuntimeException(
          String.format("ArrayProperty must have either a '%s', of a '%s' attribute",
              OpenApiSpecificationExtensions.LDPATH, OpenApiSpecificationExtensions.RESULT_REF));
    }

    return builder.build();
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

}
