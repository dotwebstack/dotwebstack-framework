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
public class ArraySchemaMapper extends AbstractSubjectFilterSchemaMapper<ArrayProperty, Object> {

  @Override
  public Object mapTupleValue(@NonNull ArrayProperty schema,
      @NonNull SchemaMapperContext schemaMapperContext) {
    return SchemaMapperUtils.castLiteralValue(schemaMapperContext.getValue()).integerValue();
  }

  @Override
  public Object mapGraphValue(@NonNull ArrayProperty property,
      @NonNull GraphEntityContext graphEntityContext,
      @NonNull SchemaMapperContext schemaMapperContext,
      @NonNull SchemaMapperAdapter schemaMapperAdapter) {
    ImmutableList.Builder<Object> builder = ImmutableList.builder();

    if (hasSubjectFilterVendorExtension(property)) {
      Set<Resource> subjects = filterSubjects(property, graphEntityContext);

      subjects.forEach(subject -> builder.add(schemaMapperAdapter.mapGraphValue(property.getItems(),
          graphEntityContext, schemaMapperContext, schemaMapperAdapter)));
    } else if (schemaMapperContext.getValue() != null) {
      if (property.getVendorExtensions().containsKey(OpenApiSpecificationExtensions.LDPATH)) {
        queryAndValidate(property, graphEntityContext, schemaMapperContext, schemaMapperAdapter,
            builder);
      } else {
        throw new SchemaMapperRuntimeException(String.format(
            "ArrayProperty must have a '%s' attribute", OpenApiSpecificationExtensions.LDPATH));
      }
    }

    return builder.build();
  }

  private void queryAndValidate(ArrayProperty property, GraphEntityContext graphEntityContext,
      SchemaMapperContext schemaMapperContext, SchemaMapperAdapter schemaMapperAdapter,
      ImmutableList.Builder<Object> builder) {
    LdPathExecutor ldPathExecutor = graphEntityContext.getLdPathExecutor();
    Collection<Value> queryResult = ldPathExecutor.ldPathQuery(schemaMapperContext.getValue(),
        (String) property.getVendorExtensions().get(OpenApiSpecificationExtensions.LDPATH));

    validateMinItems(property, queryResult);
    validateMaxItems(property, queryResult);


    queryResult.forEach(valueNext -> {
      schemaMapperContext.setValue(valueNext);
      processPropagations(property, schemaMapperContext.getValue(), schemaMapperContext);
      Optional innerPropertySolved =
          Optional.fromNullable(schemaMapperAdapter.mapGraphValue(property.getItems(),
              graphEntityContext, schemaMapperContext, schemaMapperAdapter));
      if (schemaMapperContext.isExcludedWhenEmpty()) {
        builder.add(innerPropertySolved);
      }
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
