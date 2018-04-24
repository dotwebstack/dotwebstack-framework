package org.dotwebstack.framework.frontend.openapi.entity.schema;

import com.google.common.collect.ImmutableSet;
import io.swagger.models.properties.Property;
import java.util.HashSet;
import java.util.Set;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntity;
import org.dotwebstack.framework.frontend.openapi.entity.TupleEntity;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.springframework.stereotype.Service;

@Service
class ResponseSchemaMapper extends AbstractSubjectSchemaMapper<ResponseProperty, Object> {

  @Override
  protected Set<String> getSupportedVendorExtensions() {
    return new HashSet<>();
  }

  @Override
  public Object mapTupleValue(@NonNull ResponseProperty schema, @NonNull TupleEntity entity,
      @NonNull ValueContext value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object mapGraphValue(@NonNull ResponseProperty property, @NonNull GraphEntity graphEntity,
      @NonNull ValueContext valueContext, @NonNull SchemaMapperAdapter schemaMapperAdapter) {
    ValueContext.ValueContextBuilder builder = valueContext.toBuilder();

    if (hasSubjectVendorExtension(property)) {
      Value value = getSubject(property, graphEntity);

      if (value == null) {
        return null;
      }

      builder.value(value);
    }

    return schemaMapperAdapter.mapGraphValue(property.getSchema(), graphEntity, builder.build(),
        schemaMapperAdapter);
  }

  @Override
  public boolean supports(@NonNull Property schema) {
    return schema instanceof ResponseProperty;
  }

  @Override
  protected Object convertLiteralToType(Literal literal) {
    throw new UnsupportedOperationException();
  }

  @Override
  protected Set<IRI> getSupportedDataTypes() {
    return ImmutableSet.of();
  }

}
