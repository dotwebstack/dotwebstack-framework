package org.dotwebstack.framework.frontend.openapi.entity.schema;

import com.google.common.collect.ImmutableSet;
import io.swagger.models.properties.Property;
import java.util.Set;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntityContext;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.springframework.stereotype.Service;

@Service
class ResponseSchemaMapper extends AbstractSubjectFilterSchemaMapper<ResponseProperty, Object> {

  @Override
  public Object mapTupleValue(@NonNull ResponseProperty schema, @NonNull ValueContext value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object mapGraphValue(@NonNull ResponseProperty property,
      @NonNull GraphEntityContext graphEntityContext, @NonNull ValueContext valueContext,
      @NonNull SchemaMapperAdapter schemaMapperAdapter) {
    ValueContext.ValueContextBuilder builder = valueContext.toBuilder();

    if (hasSubjectFilterVendorExtension(property)) {
      Value value = getSubject(property, graphEntityContext);

      if (value == null) {
        return null;
      }

      builder.value(value);
    }

    return schemaMapperAdapter.mapGraphValue(property.getSchema(), graphEntityContext,
        builder.build(), schemaMapperAdapter);
  }

  @Override
  public boolean supports(@NonNull Property schema) {
    return schema instanceof ResponseProperty;
  }

  @Override
  protected Set<IRI> getSupportedDataTypes() {
    return ImmutableSet.of();
  }

}
