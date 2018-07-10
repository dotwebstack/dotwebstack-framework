package org.dotwebstack.framework.frontend.openapi.entity.schema;

import io.swagger.v3.oas.models.media.Schema;
import java.util.Set;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntity;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;

abstract class AbstractSubjectSchemaMapper<S extends Schema, T>
    extends AbstractSchemaMapper<S, T> {

  protected static boolean hasSubjectVendorExtension(@NonNull Schema schema) {
    return hasVendorExtension(schema, OpenApiSpecificationExtensions.SUBJECT)
        && (boolean) schema.getExtensions().get(OpenApiSpecificationExtensions.SUBJECT);
  }

  /**
   * @return Returns the single subject, or {@code null} if no subject can be found.
   * @throws SchemaMapperRuntimeException If the property is required, and no subject can be found.
   * @throws SchemaMapperRuntimeException If more than one subject has been found.
   */
  protected static Value getSubject(@NonNull Schema schema, @NonNull GraphEntity graphEntity) {
    Set<Resource> subjects = graphEntity.getSubjects();

    if (subjects.isEmpty()) {
      // TODO: Fix required check
      // if (schema.getRequired()) {
      // throw new SchemaMapperRuntimeException(
      // "Expected a single subject, but subject query yielded no results.");
      // }

      return null;
    }

    if (subjects.size() > 1) {
      throw new SchemaMapperRuntimeException(
          "Expected a single subject, but subject query yielded multiple results.");
    }

    return subjects.iterator().next();
  }

}
