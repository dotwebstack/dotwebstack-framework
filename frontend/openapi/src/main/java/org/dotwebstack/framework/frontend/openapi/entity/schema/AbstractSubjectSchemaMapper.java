package org.dotwebstack.framework.frontend.openapi.entity.schema;

import io.swagger.v3.oas.models.media.Schema;
import java.util.Set;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntity;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;

abstract class AbstractSubjectSchemaMapper<S extends Schema, T> extends AbstractSchemaMapper<S, T> {

  protected static boolean hasSubjectVendorExtension(@NonNull Schema schema) {
    return hasVendorExtension(schema, OpenApiSpecificationExtensions.SUBJECT)
        && (boolean) schema.getExtensions().get(OpenApiSpecificationExtensions.SUBJECT);
  }

  /**
   * @return Returns the single subject, or {@code null} if no subject can be found.
   * @throws SchemaMapperRuntimeException If the property is required, and no subject can be found.
   * @throws SchemaMapperRuntimeException If more than one subject has been found.
   */
  protected static Value getSubject(@NonNull GraphEntity graphEntity, boolean required) {
    Set<Resource> subjects = graphEntity.getSubjects();

    if (subjects.isEmpty()) {
      if (required) {
        throwException(subjects.size());
      }

      return null;
    }

    if (subjects.size() > 1) {
      throwException(subjects.size());
    }

    return subjects.iterator().next();
  }

  private static void throwException(int size) {
    String message = "Expected a single subject, but subject query yielded " + size + " results.";
    throw new SchemaMapperRuntimeException(message);
  }

}
