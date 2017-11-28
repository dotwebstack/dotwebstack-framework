package org.dotwebstack.framework.frontend.openapi.entity.schema;

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

// XXX (PvH) Waarom een LdPathSchemaMapper als de enige subklasse de AbstractSchemaMapper is?
// (samenvoegen?)
abstract class LdPathSchemaMapper {

  // XXX (PvH) @NonNull annotaties ontbreken
  // XXX (PvH) Miss goed om te doccen wanneer je wat terug geeft (null of een lege Set)
  Set<Resource> applySubjectFilterIfPossible(Property property,
      GraphEntityContext graphEntityContext) {
    if (property.getVendorExtensions().containsKey(OpenApiSpecificationExtensions.SUBJECT_FILTER)) {

      // XXX (PvH) Waarom verwijs je naar de LinkedHashMap implementatie, en niet naar de Map
      // interface?
      LinkedHashMap subjectFilter = (LinkedHashMap) property.getVendorExtensions().get(
          OpenApiSpecificationExtensions.SUBJECT_FILTER);

      String predicate =
          (String) subjectFilter.get(OpenApiSpecificationExtensions.SUBJECT_FILTER_PREDICATE);
      String object =
          (String) subjectFilter.get(OpenApiSpecificationExtensions.SUBJECT_FILTER_OBJECT);
      // XXX (PvH) Wat als predicate en object niet zijn ingevuld?

      ValueFactory vf = SimpleValueFactory.getInstance();

      final IRI predicateIri = vf.createIRI(predicate);
      // XXX (PvH) Object hoeft niet een literal te zijn (variabele naam)
      final IRI objectLiteral = vf.createIRI(object);
      Model filteredModel = graphEntityContext.getModel().filter(null, predicateIri, objectLiteral);

      return filteredModel.subjects();
    }

    // XXX (PvH) Conditie niet getest?
    return null;
  }
}
