package org.dotwebstack.framework.frontend.openapi.entity.schema;

import io.swagger.models.properties.Property;
import java.util.Map;
import java.util.Set;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntityContext;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

abstract class AbstractSubjectFilterSchemaMapper<S extends Property, T>
    extends AbstractSchemaMapper<S, T> {

  protected boolean hasSubjectFilterVendorExtension(@NonNull Property property) {
    return hasVendorExtension(property, OpenApiSpecificationExtensions.SUBJECT_FILTER);
  }

  /**
   * Apply the subject filter and returns the filtered subjects.
   * 
   * @param property property with subject filter
   * @param graphEntityContext context of the entity
   * @return non empty set when no results could be found.
   * @throws IllegalStateException If the property does not have the
   *         {@link OpenApiSpecificationExtensions#SUBJECT_FILTER} vendor extension defined. Please
   *         call {@link #hasSubjectFilterVendorExtension(Property)} before calling this method.
   */
  protected final Set<Resource> getSubjects(@NonNull Property property,
      @NonNull GraphEntityContext graphEntityContext) {
    if (!hasSubjectFilterVendorExtension(property)) {
      throw new IllegalStateException(String.format(
          "Vendor extension '%s' not defined, "
              + "please call hasSubjectFilterVendorExtension() before calling this method",
          OpenApiSpecificationExtensions.SUBJECT_FILTER));
    }

    Map subjectFilter =
        (Map) property.getVendorExtensions().get(OpenApiSpecificationExtensions.SUBJECT_FILTER);

    String predicate =
        (String) subjectFilter.get(OpenApiSpecificationExtensions.SUBJECT_FILTER_PREDICATE);
    String object =
        (String) subjectFilter.get(OpenApiSpecificationExtensions.SUBJECT_FILTER_OBJECT);

    if (predicate == null || object == null) {
      throw new SchemaMapperRuntimeException(
          "Subject filter cannot work without missing predicate or object.");
    }

    ValueFactory vf = SimpleValueFactory.getInstance();

    final IRI predicateIri = vf.createIRI(predicate);
    final IRI objectIri = vf.createIRI(object);
    Model filteredModel = graphEntityContext.getModel().filter(null, predicateIri, objectIri);

    return filteredModel.subjects();
  }

  /**
   * @return Applies the subject filter and returns the single subject, or {@code null} if no
   *         subject can be found.
   * @throws IllegalStateException If the property does not have the
   *         {@link OpenApiSpecificationExtensions#SUBJECT_FILTER} vendor extension defined. Please
   *         call {@link #hasSubjectFilterVendorExtension(Property)} before calling this method.
   * @throws SchemaMapperRuntimeException If no predicate and object have been defined for the
   *         subject filter.
   * @throws SchemaMapperRuntimeException If the property is required, and no subject can be found.
   * @throws SchemaMapperRuntimeException If more than one subject has been found.
   */
  protected final Value getSubject(@NonNull Property property,
      @NonNull GraphEntityContext graphEntityContext) {
    Set<Resource> subjects = getSubjects(property, graphEntityContext);

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

    return subjects.iterator().next();
  }

}
