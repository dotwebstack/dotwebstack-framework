package org.dotwebstack.framework.frontend.openapi.entity.schema;

import io.swagger.models.properties.Property;
import java.util.Set;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.Rdf4jUtils;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntity;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.QueryEvaluationException;

abstract class AbstractSubjectQuerySchemaMapper<S extends Property, T>
    extends AbstractSchemaMapper<S, T> {

  protected boolean hasSubjectQueryVendorExtension(@NonNull Property property) {
    return hasVendorExtension(property, OpenApiSpecificationExtensions.SUBJECT_QUERY);
  }

  /**
   * Apply the subject (SPARQL) query and returns the resulting subjects.
   * 
   * @throws IllegalStateException If the property does not have the
   *         {@link OpenApiSpecificationExtensions#SUBJECT_QUERY} vendor extension defined. Please
   *         call {@link #hasSubjectQueryVendorExtension(Property)} before calling this method.
   * @throws SchemaMapperRuntimeException If the subject query has &gt; 1 binding defined. Or if the
   *         result contains a non {@link Resource}.
   */
  protected final Set<Resource> getSubjects(@NonNull Property property,
      @NonNull GraphEntity entity) {
    if (!hasSubjectQueryVendorExtension(property)) {
      throw new IllegalStateException(String.format(
          "Vendor extension '%s' not defined, "
              + "please call hasSubjectQueryVendorExtension() before calling this method",
          OpenApiSpecificationExtensions.SUBJECT_QUERY));
    }

    try {
      String query =
          (String) property.getVendorExtensions().get(OpenApiSpecificationExtensions.SUBJECT_QUERY);

      return Rdf4jUtils.evaluateSingleBindingSelectQuery(entity.getRepository(), query);
    } catch (QueryEvaluationException e) {
      throw new SchemaMapperRuntimeException(e);
    }
  }

  /**
   * @return Applies the subject query and returns the single subject, or {@code null} if no subject
   *         can be found.
   * @throws SchemaMapperRuntimeException If the property is required, and no subject can be found.
   * @throws SchemaMapperRuntimeException If more than one subject has been found.
   * @see #getSubjects(Property, GraphEntity)
   */
  protected final Value getSubject(@NonNull Property property, @NonNull GraphEntity graphEntity) {
    Set<Resource> subjects = getSubjects(property, graphEntity);

    if (subjects.isEmpty()) {
      if (property.getRequired()) {
        throw new SchemaMapperRuntimeException(
            "Subject query for a required object property yielded no result.");
      }

      return null;
    }

    if (subjects.size() > 1) {
      throw new SchemaMapperRuntimeException(
          "More entrypoint subjects found. Only one is required.");
    }

    return subjects.iterator().next();
  }

}
