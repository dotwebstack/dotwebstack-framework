package org.dotwebstack.framework.frontend.openapi.entity.schema;

import com.google.common.collect.ImmutableSet;
import io.swagger.models.properties.Property;
import java.util.Set;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntityContext;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.springframework.stereotype.Service;

@Service
class ResponseSchemaMapper extends AbstractSubjectFilterSchemaMapper<ResponseProperty, Object> {

  @Override
  public Object mapTupleValue(@NonNull ResponseProperty schema, Value value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object mapGraphValue(@NonNull ResponseProperty property,
      @NonNull GraphEntityContext graphEntityContext,
      @NonNull SchemaMapperAdapter schemaMapperAdapter, Value context) {
    Value newContext = context;

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

      newContext = subjects.iterator().next();
    }

    return schemaMapperAdapter.mapGraphValue(property.getSchema(), graphEntityContext,
        schemaMapperAdapter, newContext);
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
