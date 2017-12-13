package org.dotwebstack.framework.frontend.openapi.entity.schema;

import com.google.common.collect.ImmutableSet;
import io.swagger.models.properties.Property;
import java.util.Set;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntityContext;
import org.dotwebstack.framework.frontend.openapi.entity.schema.ValueContext.ValueContextBuilder;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
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
