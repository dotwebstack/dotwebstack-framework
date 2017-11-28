package org.dotwebstack.framework.frontend.openapi.entity.schema;

import com.google.common.collect.ImmutableSet;
import io.swagger.models.properties.Property;
import java.util.LinkedHashMap;
import java.util.Set;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntityContext;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

abstract class LdPathSchemaMapper {

  Set<Resource> applySubjectFilterIfPossible(Property property,
      GraphEntityContext graphEntityContext) {
    if (property.getVendorExtensions().containsKey(OpenApiSpecificationExtensions.SUBJECT_FILTER)) {
      LinkedHashMap subjectFilter = (LinkedHashMap) property.getVendorExtensions().get(
          OpenApiSpecificationExtensions.SUBJECT_FILTER);

      String predicate =
          (String) subjectFilter.get(OpenApiSpecificationExtensions.SUBJECT_FILTER_PREDICATE);
      String object =
          (String) subjectFilter.get(OpenApiSpecificationExtensions.SUBJECT_FILTER_OBJECT);

      ValueFactory vf = SimpleValueFactory.getInstance();

      final IRI predicateIri = vf.createIRI(predicate);
      final IRI objectLiteral = vf.createIRI(object);
      Model filteredModel = graphEntityContext.getModel().filter(null, predicateIri, objectLiteral);

      return filteredModel.subjects();
    }
    return ImmutableSet.of();
  }
}
