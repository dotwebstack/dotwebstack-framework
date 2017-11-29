package org.dotwebstack.framework.frontend.openapi.entity.schema;

import com.google.common.collect.ImmutableSet;
import io.swagger.models.properties.Property;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntityContext;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public interface LdPathSchemaMapper {

  /**
   * Apply subject filter if possible.
   * 
   * @param property property with subject filter
   * @param graphEntityContext context of the entity
   * @return non empty set when no results could be found.
   */
  default Set<Resource> applySubjectFilterIfPossible(@NonNull Property property,
      @NonNull GraphEntityContext graphEntityContext) {
    if (property.getVendorExtensions().containsKey(OpenApiSpecificationExtensions.SUBJECT_FILTER)) {

      Map subjectFilter = (LinkedHashMap) property.getVendorExtensions().get(
          OpenApiSpecificationExtensions.SUBJECT_FILTER);

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
    return ImmutableSet.of();
  }
}
