package org.dotwebstack.framework.frontend.openapi.entity.schema;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntity;
import org.dotwebstack.framework.frontend.openapi.entity.LdPathExecutor;
import org.dotwebstack.framework.frontend.openapi.entity.TupleEntity;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.springframework.stereotype.Service;

@Service
public class ArraySchemaMapper extends AbstractSubjectSchemaMapper<ArraySchema, Object> {

  @Override
  protected Set<String> getSupportedVendorExtensions() {
    return new HashSet<>();
  }

  @Override
  public Object mapTupleValue(@NonNull ArraySchema schema, @NonNull TupleEntity entity,
      @NonNull ValueContext valueContext) {
    return SchemaMapperUtils.castLiteralValue(valueContext.getValue()).integerValue();
  }

  @Override
  public Object mapGraphValue(@NonNull ArraySchema schema, boolean required,
      @NonNull GraphEntity graphEntity, @NonNull ValueContext valueContext,
      @NonNull SchemaMapperAdapter schemaMapperAdapter) {
    ImmutableList.Builder<Object> builder = ImmutableList.builder();

    if (hasSubjectVendorExtension(schema)) {
      Set<Resource> subjects = graphEntity.getSubjects();

      subjects.forEach(subject -> {
        ValueContext subjectContext = valueContext.toBuilder().value(subject).build();

        builder.add(schemaMapperAdapter.mapGraphValue(schema.getItems(), false, graphEntity,
            subjectContext, schemaMapperAdapter));
      });
    } else if (valueContext.getValue() != null) {
      if (schema.getExtensions() == null ? false
          : schema.getExtensions().containsKey(OpenApiSpecificationExtensions.LDPATH)) {
        queryAndValidate(schema, required, graphEntity, valueContext, schemaMapperAdapter, builder);
      } else {
        throw new SchemaMapperRuntimeException(String.format(
            "ArraySchema must have a '%s' attribute", OpenApiSpecificationExtensions.LDPATH));
      }
    }
    return builder.build();
  }

  private void queryAndValidate(ArraySchema schema, boolean required, GraphEntity graphEntity,
      ValueContext valueContext, SchemaMapperAdapter schemaMapperAdapter,
      ImmutableList.Builder<Object> builder) {
    LdPathExecutor ldPathExecutor = graphEntity.getLdPathExecutor();
    Collection<Value> queryResult = ldPathExecutor.ldPathQuery(valueContext.getValue(),
        (String) schema.getExtensions().get(OpenApiSpecificationExtensions.LDPATH));

    validateMinItems(schema, queryResult);
    validateMaxItems(schema, queryResult);

    queryResult.forEach(valueNext -> {
      ValueContext newValueContext = valueContext.toBuilder().value(valueNext).build();
      Optional innerPropertySolved =
          Optional.fromNullable(schemaMapperAdapter.mapGraphValue(schema.getItems(), false,
              graphEntity, newValueContext, schemaMapperAdapter));
      builder.add(innerPropertySolved);

    });
  }

  private static void validateMinItems(ArraySchema arraySchema, Collection<Value> queryResult) {
    Integer minItems = arraySchema.getMinItems();
    if (minItems != null && minItems > queryResult.size()) {
      throw new SchemaMapperRuntimeException(String.format(
          "Mapping for property yielded %d elements, which is less than 'minItems' (%d)"
              + " specified in the OpenAPI specification.",
          queryResult.size(), minItems));
    }
  }

  private static void validateMaxItems(ArraySchema arraySchema, Collection<Value> queryResult) {
    Integer maxItems = arraySchema.getMaxItems();
    if (maxItems != null && maxItems < queryResult.size()) {
      throw new SchemaMapperRuntimeException(String.format(
          "Mapping for property yielded %d elements, which is more than 'maxItems' (%d)"
              + " specified in the OpenAPI specification.",
          queryResult.size(), maxItems));
    }
  }


  @Override
  protected Object convertLiteralToType(Literal literal) {
    return literal.integerValue();
  }

  @Override
  public boolean supports(@NonNull Schema schema) {
    return schema instanceof ArraySchema;
  }

  @Override
  protected Set<IRI> getSupportedDataTypes() {
    return ImmutableSet.of();
  }

}
